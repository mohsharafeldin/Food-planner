package com.example.foodplanner.data.auth.firebase;

import android.util.Log;

import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.model.PlannedMeal;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Helper class for Firebase Realtime Database data synchronization.
 * Handles backup and restore of favorites and planned meals.
 */
public class FirebaseSyncHelper {

    private static final String TAG = "FirebaseSyncHelper";
    private static final String NODE_USERS = "users";
    private static final String NODE_FAVORITES = "favorites";
    private static final String NODE_PLANNED_MEALS = "planned_meals";

    private static FirebaseSyncHelper instance;
    private final FirebaseDatabase database;

    private FirebaseSyncHelper() {
        database = FirebaseDatabase.getInstance();
        // Enable offline persistence if needed
        // database.setPersistenceEnabled(true);
    }

    public static synchronized FirebaseSyncHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseSyncHelper();
        }
        return instance;
    }

    /**
     * Backup all favorites to Realtime Database
     */
    public Completable backupFavorites(String userId, List<FavoriteMeal> favorites) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            if (favorites == null || favorites.isEmpty()) {
                emitter.onComplete();
                return;
            }

            DatabaseReference favoritesRef = database.getReference(NODE_USERS)
                    .child(userId)
                    .child(NODE_FAVORITES);

            for (FavoriteMeal meal : favorites) {
                favoritesRef.child(meal.getIdMeal()).setValue(meal)
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to backup favorite: " + meal.getIdMeal(), e));
            }

            emitter.onComplete();
        });
    }

    /**
     * Backup all planned meals to Realtime Database
     */
    public Completable backupPlannedMeals(String userId, List<PlannedMeal> plannedMeals) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            if (plannedMeals == null || plannedMeals.isEmpty()) {
                emitter.onComplete();
                return;
            }

            DatabaseReference plansRef = database.getReference(NODE_USERS)
                    .child(userId)
                    .child(NODE_PLANNED_MEALS);

            for (PlannedMeal meal : plannedMeals) {
                // Composite key for uniqueness
                String nodeKey = meal.getMealId() + "_" + meal.getDate() + "_" + meal.getMealType();
                plansRef.child(nodeKey).setValue(meal)
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to backup plan: " + nodeKey, e));
            }

            emitter.onComplete();
        });
    }

    /**
     * Restore favorites from Realtime Database
     */
    public Single<List<FavoriteMeal>> restoreFavorites(String userId) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            database.getReference(NODE_USERS)
                    .child(userId)
                    .child(NODE_FAVORITES)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<FavoriteMeal> favorites = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                FavoriteMeal meal = child.getValue(FavoriteMeal.class);
                                if (meal != null) {
                                    // Ensure ID is set (key) if missing in value
                                    if (meal.getIdMeal() == null) {
                                        meal.setIdMeal(child.getKey());
                                    }
                                    meal.setUserId(userId);
                                    favorites.add(meal);
                                }
                            }
                            Log.d(TAG, "Favorites restore successful: " + favorites.size() + " items");
                            emitter.onSuccess(favorites);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            emitter.onError(error.toException());
                        }
                    });
        });
    }

    /**
     * Restore planned meals from Realtime Database
     */
    public Single<List<PlannedMeal>> restorePlannedMeals(String userId) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            database.getReference(NODE_USERS)
                    .child(userId)
                    .child(NODE_PLANNED_MEALS)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<PlannedMeal> plannedMeals = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                PlannedMeal meal = child.getValue(PlannedMeal.class);
                                if (meal != null) {
                                    meal.setUserId(userId);
                                    plannedMeals.add(meal);
                                }
                            }
                            Log.d(TAG, "Planned meals restore successful: " + plannedMeals.size() + " items");
                            emitter.onSuccess(plannedMeals);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            emitter.onError(error.toException());
                        }
                    });
        });
    }

    /**
     * Delete a favorite from Realtime Database
     */
    public Completable deleteFavorite(String userId, String mealId) {
        return Completable.create(emitter -> {
            database.getReference(NODE_USERS)
                    .child(userId)
                    .child(NODE_FAVORITES)
                    .child(mealId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Delete a planned meal from Realtime Database
     */
    public Completable deletePlannedMeal(String userId, PlannedMeal meal) {
        return Completable.create(emitter -> {
            String nodeKey = meal.getMealId() + "_" + meal.getDate() + "_" + meal.getMealType();
            database.getReference(NODE_USERS)
                    .child(userId)
                    .child(NODE_PLANNED_MEALS)
                    .child(nodeKey)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Clear all user data from Realtime Database
     */
    public Completable clearAllData(String userId) {
        return Completable.create(emitter -> {
            database.getReference(NODE_USERS)
                    .child(userId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }
}
