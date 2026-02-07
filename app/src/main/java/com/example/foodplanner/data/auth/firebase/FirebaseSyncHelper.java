package com.example.foodplanner.data.auth.firebase;

import android.util.Log;

import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.model.PlannedMeal;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * Helper class for Firebase Firestore data synchronization.
 * Handles backup and restore of favorites and planned meals.
 */
public class FirebaseSyncHelper {

    private static final String TAG = "FirebaseSyncHelper";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_FAVORITES = "favorites";
    private static final String COLLECTION_PLANNED_MEALS = "planned_meals";

    private static FirebaseSyncHelper instance;
    private final FirebaseFirestore firestore;

    private FirebaseSyncHelper() {
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseSyncHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseSyncHelper();
        }
        return instance;
    }

    /**
     * Backup all favorites to Firestore
     */
    public Completable backupFavorites(String userId, List<FavoriteMeal> favorites) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            WriteBatch batch = firestore.batch();

            for (FavoriteMeal meal : favorites) {
                Map<String, Object> data = new HashMap<>();
                data.put("idMeal", meal.getIdMeal());
                data.put("strMeal", meal.getStrMeal());
                data.put("strMealThumb", meal.getStrMealThumb());
                data.put("strCategory", meal.getStrCategory());
                data.put("strArea", meal.getStrArea());
                data.put("strInstructions", meal.getStrInstructions());
                data.put("strYoutube", meal.getStrYoutube());
                data.put("strTags", meal.getStrTags());
                data.put("ingredients", meal.getIngredients());
                data.put("measures", meal.getMeasures());
                data.put("userId", userId);

                batch.set(
                        firestore.collection(COLLECTION_USERS)
                                .document(userId)
                                .collection(COLLECTION_FAVORITES)
                                .document(meal.getIdMeal()),
                        data);
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Favorites backup successful: " + favorites.size() + " items");
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Backup all planned meals to Firestore
     */
    public Completable backupPlannedMeals(String userId, List<PlannedMeal> plannedMeals) {
        return Completable.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            WriteBatch batch = firestore.batch();

            for (PlannedMeal meal : plannedMeals) {
                Map<String, Object> data = new HashMap<>();
                data.put("mealId", meal.getMealId());
                data.put("mealName", meal.getMealName());
                data.put("mealThumb", meal.getMealThumb());
                data.put("mealCategory", meal.getMealCategory());
                data.put("mealArea", meal.getMealArea());
                data.put("date", meal.getDate());
                data.put("mealType", meal.getMealType());
                data.put("userId", userId);

                // Use composite key for planned meal document
                String docId = meal.getMealId() + "_" + meal.getDate() + "_" + meal.getMealType();
                batch.set(
                        firestore.collection(COLLECTION_USERS)
                                .document(userId)
                                .collection(COLLECTION_PLANNED_MEALS)
                                .document(docId),
                        data);
            }

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Planned meals backup successful: " + plannedMeals.size() + " items");
                        emitter.onComplete();
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Restore favorites from Firestore
     */
    public Single<List<FavoriteMeal>> restoreFavorites(String userId) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_FAVORITES)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<FavoriteMeal> favorites = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            FavoriteMeal meal = new FavoriteMeal();
                            String idMeal = doc.getString("idMeal");
                            if (idMeal != null) {
                                meal.setIdMeal(idMeal);
                            }
                            meal.setStrMeal(doc.getString("strMeal"));
                            meal.setStrMealThumb(doc.getString("strMealThumb"));
                            meal.setStrCategory(doc.getString("strCategory"));
                            meal.setStrArea(doc.getString("strArea"));
                            meal.setStrInstructions(doc.getString("strInstructions"));
                            meal.setStrYoutube(doc.getString("strYoutube"));
                            meal.setStrTags(doc.getString("strTags"));
                            meal.setIngredients(doc.getString("ingredients"));
                            meal.setMeasures(doc.getString("measures"));
                            meal.setUserId(doc.getString("userId"));
                            favorites.add(meal);
                        }
                        Log.d(TAG, "Favorites restore successful: " + favorites.size() + " items");
                        emitter.onSuccess(favorites);
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Restore planned meals from Firestore
     */
    public Single<List<PlannedMeal>> restorePlannedMeals(String userId) {
        return Single.create(emitter -> {
            if (userId == null || userId.isEmpty()) {
                emitter.onError(new Exception("User ID is required"));
                return;
            }

            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_PLANNED_MEALS)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        List<PlannedMeal> plannedMeals = new ArrayList<>();
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            PlannedMeal meal = new PlannedMeal();
                            String mealId = doc.getString("mealId");
                            String date = doc.getString("date");
                            if (mealId != null) {
                                meal.setMealId(mealId);
                            }
                            if (date != null) {
                                meal.setDate(date);
                            }
                            meal.setMealName(doc.getString("mealName"));
                            meal.setMealThumb(doc.getString("mealThumb"));
                            meal.setMealCategory(doc.getString("mealCategory"));
                            meal.setMealArea(doc.getString("mealArea"));
                            meal.setMealType(doc.getString("mealType"));
                            meal.setUserId(doc.getString("userId"));
                            plannedMeals.add(meal);
                        }
                        Log.d(TAG, "Planned meals restore successful: " + plannedMeals.size() + " items");
                        emitter.onSuccess(plannedMeals);
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Delete a favorite from Firestore
     */
    public Completable deleteFavorite(String userId, String mealId) {
        return Completable.create(emitter -> {
            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_FAVORITES)
                    .document(mealId)
                    .delete()
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Delete a planned meal from Firestore
     */
    public Completable deletePlannedMeal(String userId, PlannedMeal meal) {
        return Completable.create(emitter -> {
            String docId = meal.getMealId() + "_" + meal.getDate() + "_" + meal.getMealType();
            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_PLANNED_MEALS)
                    .document(docId)
                    .delete()
                    .addOnSuccessListener(aVoid -> emitter.onComplete())
                    .addOnFailureListener(emitter::onError);
        });
    }

    /**
     * Clear all user data from Firestore
     */
    public Completable clearAllData(String userId) {
        return Completable.create(emitter -> {
            // Delete favorites
            firestore.collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_FAVORITES)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        WriteBatch batch = firestore.batch();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit().addOnSuccessListener(aVoid -> {
                            // Now delete planned meals
                            deletePlannedMealsCollection(userId, emitter);
                        }).addOnFailureListener(emitter::onError);
                    })
                    .addOnFailureListener(emitter::onError);
        });
    }

    private void deletePlannedMealsCollection(String userId, io.reactivex.rxjava3.core.CompletableEmitter emitter) {
        firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PLANNED_MEALS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    WriteBatch batch = firestore.batch();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit()
                            .addOnSuccessListener(aVoid -> emitter.onComplete())
                            .addOnFailureListener(emitter::onError);
                })
                .addOnFailureListener(emitter::onError);
    }
}
