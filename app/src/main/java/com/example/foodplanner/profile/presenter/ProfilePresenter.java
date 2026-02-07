package com.example.foodplanner.profile.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.auth.firebase.FirebaseAuthHelper;
import com.example.foodplanner.data.auth.firebase.FirebaseSyncHelper;
import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.model.PlannedMeal;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.profile.view.ProfileView;
import com.example.foodplanner.utils.SessionManager;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private final MealRepository repository;
    private final FirebaseAuthHelper authHelper;
    private final FirebaseSyncHelper syncHelper;
    private final SessionManager sessionManager;
    private final String userId;

    public ProfilePresenter(MealRepository repository, SessionManager sessionManager, String userId) {
        this.repository = repository;
        this.sessionManager = sessionManager;
        this.userId = userId;
        this.authHelper = FirebaseAuthHelper.getInstance();
        this.syncHelper = FirebaseSyncHelper.getInstance();
    }

    /**
     * Get the Firebase UID for sync operations
     */
    private String getFirebaseUid() {
        String firebaseUid = sessionManager.getFirebaseUid();
        return (firebaseUid != null && !firebaseUid.isEmpty()) ? firebaseUid : userId;
    }

    /**
     * Backup local data to Firebase Firestore
     */
    public void backupData() {
        String firebaseUid = getFirebaseUid();
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            if (view != null) {
                view.showError("Please login to backup data");
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        // Get all favorites from local Room database
        addDisposable(
                repository.getAllFavorites(userId)
                        .firstOrError()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                favorites -> backupFavoritesToFirestore(firebaseUid, favorites),
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Backup failed: " + error.getMessage());
                                    }
                                }));
    }

    private void backupFavoritesToFirestore(String firebaseUid, List<FavoriteMeal> favorites) {
        addDisposable(
                syncHelper.backupFavorites(firebaseUid, favorites)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    // Now backup planned meals
                                    backupPlannedMealsFromLocal(firebaseUid);
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Backup failed: " + error.getMessage());
                                    }
                                }));
    }

    private void backupPlannedMealsFromLocal(String firebaseUid) {
        addDisposable(
                repository.getAllPlannedMeals(userId)
                        .firstOrError()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                plannedMeals -> backupPlannedMealsToFirestore(firebaseUid, plannedMeals),
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Backup failed: " + error.getMessage());
                                    }
                                }));
    }

    private void backupPlannedMealsToFirestore(String firebaseUid, List<PlannedMeal> plannedMeals) {
        addDisposable(
                syncHelper.backupPlannedMeals(firebaseUid, plannedMeals)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.onBackupSuccess();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Backup failed: " + error.getMessage());
                                    }
                                }));
    }

    /**
     * Restore data from Firebase Firestore to local Room database
     */
    public void restoreData() {
        String firebaseUid = getFirebaseUid();
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            if (view != null) {
                view.showError("Please login to restore data");
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        // Restore favorites from Firestore
        addDisposable(
                syncHelper.restoreFavorites(firebaseUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::saveFavoritesToLocal,
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Restore failed: " + error.getMessage());
                                    }
                                }));
    }

    private void saveFavoritesToLocal(List<FavoriteMeal> favorites) {
        // Save each favorite to local Room database
        for (FavoriteMeal meal : favorites) {
            meal.setUserId(userId);
            addDisposable(
                    repository.addFavorite(meal)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                            }, error -> {
                            }));
        }

        // Now restore planned meals
        restorePlannedMealsFromFirestore();
    }

    private void restorePlannedMealsFromFirestore() {
        String firebaseUid = getFirebaseUid();
        addDisposable(
                syncHelper.restorePlannedMeals(firebaseUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::savePlannedMealsToLocal,
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Restore failed: " + error.getMessage());
                                    }
                                }));
    }

    private void savePlannedMealsToLocal(List<PlannedMeal> plannedMeals) {
        // Save each planned meal to local Room database
        for (PlannedMeal meal : plannedMeals) {
            meal.setUserId(userId);
            addDisposable(
                    repository.insertPlannedMealDirectly(meal)
                            .subscribeOn(Schedulers.io())
                            .subscribe(() -> {
                            }, error -> {
                            }));
        }

        if (view != null) {
            view.hideLoading();
            view.onRestoreSuccess();
        }
    }

    /**
     * Sync local data with Firestore (backup then restore)
     */
    public void syncData() {
        String firebaseUid = getFirebaseUid();
        if (firebaseUid == null || firebaseUid.isEmpty()) {
            if (view != null)
                view.showError("Please login to sync data");
            return;
        }

        if (view != null)
            view.showLoading();

        // First backup local to Firestore, then restore Firestore to local
        addDisposable(
                repository.getAllFavorites(userId)
                        .firstOrError()
                        .subscribeOn(Schedulers.io())
                        .flatMapCompletable(favorites -> syncHelper.backupFavorites(firebaseUid, favorites))
                        .andThen(repository.getAllPlannedMeals(userId).firstOrError())
                        .flatMapCompletable(meals -> syncHelper.backupPlannedMeals(firebaseUid, meals))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (view instanceof ProfileView) {
                                            ((ProfileView) view).onSyncSuccess();
                                        }
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Sync failed: " + error.getMessage());
                                    }
                                }));
    }

    public void logout() {
        // Sign out from Firebase
        authHelper.signOut();
        // Clear local session
        sessionManager.logout();

        if (view != null) {
            view.onLogoutSuccess();
        }
    }
}
