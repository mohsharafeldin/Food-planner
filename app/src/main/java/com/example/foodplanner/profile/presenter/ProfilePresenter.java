package com.example.testfoodplanner.profile.presenter;

import com.example.testfoodplanner.base.BasePresenter;
import com.example.testfoodplanner.data.model.FavoriteMeal;
import com.example.testfoodplanner.data.model.PlannedMeal;
import com.example.testfoodplanner.data.repository.MealRepository;
import com.example.testfoodplanner.profile.view.ProfileView;
import com.example.testfoodplanner.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private final MealRepository repository;
    private final SessionManager sessionManager;
    private final FirebaseFirestore firestore;
    private final String userId;

    public ProfilePresenter(MealRepository repository, SessionManager sessionManager, String userId) {
        this.repository = repository;
        this.sessionManager = sessionManager;
        this.userId = userId;
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void backupData() {
        if (userId == null) {
            if (view != null) {
                view.showError("Please login to backup data");
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        // First get all favorites
        addDisposable(
                repository.getAllFavorites(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                favorites -> backupFavorites(favorites),
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    private void backupFavorites(List<FavoriteMeal> favorites) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("favorites", favorites);

        firestore.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    if (view != null) {
                        view.hideLoading();
                        view.onBackupSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (view != null) {
                        view.hideLoading();
                        view.showError(e.getMessage());
                    }
                });
    }

    public void restoreData() {
        if (userId == null) {
            if (view != null) {
                view.showError("Please login to restore data");
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> favoritesData = (List<Map<String, Object>>) documentSnapshot
                                .get("favorites");

                        if (favoritesData != null) {
                            for (Map<String, Object> data : favoritesData) {
                                FavoriteMeal favorite = new FavoriteMeal();
                                favorite.setIdMeal((String) data.get("idMeal"));
                                favorite.setStrMeal((String) data.get("strMeal"));
                                favorite.setStrMealThumb((String) data.get("strMealThumb"));
                                favorite.setStrCategory((String) data.get("strCategory"));
                                favorite.setStrArea((String) data.get("strArea"));
                                favorite.setUserId(userId);

                                addDisposable(
                                        repository.addFavoriteDirectly(favorite)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(
                                                        () -> {
                                                        },
                                                        error -> {
                                                        }));
                            }
                        }

                        if (view != null) {
                            view.hideLoading();
                            view.onRestoreSuccess();
                        }
                    } else {
                        if (view != null) {
                            view.hideLoading();
                            view.showError("No backup found");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (view != null) {
                        view.hideLoading();
                        view.showError(e.getMessage());
                    }
                });
    }

    public void syncData() {
        if (userId == null) {
            if (view != null)
                view.showError("Please login to sync data");
            return;
        }

        if (view != null)
            view.showLoading();

        // 1. Pull from Cloud (Restore)
        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> favoritesData = (List<Map<String, Object>>) documentSnapshot
                                .get("favorites");
                        if (favoritesData != null) {
                            // Convert Map to objects logic here or generic?
                            // Simpler to just proceed to 2 if empty, or process if not.
                            // For simplicity, let's just trigger backup (Push) if nothing to restore,
                            // OR process restore then backup.

                            // To properly sync, we should insert cloud items to local first.
                            // Since we don't have a direct "Map list to List<FavoriteMeal>" helper visible,
                            // I'll stick to the existing loop pattern but chained.
                            processSyncRestore(favoritesData);
                        } else {
                            // Nothing to restore, just backup
                            performFullBackup();
                        }
                    } else {
                        // No cloud data, just backup
                        performFullBackup();
                    }
                })
                .addOnFailureListener(e -> {
                    if (view != null) {
                        view.hideLoading();
                        view.showError("Sync failed: " + e.getMessage());
                    }
                });
    }

    private void processSyncRestore(List<Map<String, Object>> favoritesData) {
        // Quick & Dirty: Loop insert (since we know it uses OnConflictStrategy.REPLACE)
        // Ideally use insertAll, but need to map manualy.
        // I will use RxJava concat/merge to wait for all inserts.

        io.reactivex.rxjava3.core.Observable.fromIterable(favoritesData)
                .map(data -> {
                    FavoriteMeal favorite = new FavoriteMeal();
                    favorite.setIdMeal((String) data.get("idMeal"));
                    favorite.setStrMeal((String) data.get("strMeal"));
                    favorite.setStrMealThumb((String) data.get("strMealThumb"));
                    favorite.setStrCategory((String) data.get("strCategory"));
                    favorite.setStrArea((String) data.get("strArea"));
                    favorite.setUserId(userId);
                    return favorite;
                })
                .flatMapCompletable(meal -> repository.addFavoriteDirectly(meal).subscribeOn(Schedulers.io()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> performFullBackup(), // 2. Push to Cloud (Backup)
                        error -> {
                            if (view != null) {
                                view.hideLoading();
                                view.showError("Sync Restore failed: " + error.getMessage());
                            }
                        });
    }

    private void performFullBackup() {
        // Fetch all local and push to cloud
        addDisposable(repository.getAllFavorites(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        favorites -> {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("favorites", favorites);

                            firestore.collection("users").document(userId).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        if (view != null) {
                                            view.hideLoading();
                                            if (view instanceof ProfileView) {
                                                ((ProfileView) view).onSyncSuccess();
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (view != null) {
                                            view.hideLoading();
                                            view.showError("Sync Backup failed: " + e.getMessage());
                                        }
                                    });
                        },
                        error -> {
                            if (view != null) {
                                view.hideLoading();
                                view.showError("Sync Local Read failed: " + error.getMessage());
                            }
                        }));
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        sessionManager.logout();
        if (view != null) {
            view.onLogoutSuccess();
        }
    }
}
