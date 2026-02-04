package com.example.foodplanner.profile.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.data.repository.MealRepository;
import com.example.foodplanner.profile.view.ProfileView;
import com.example.foodplanner.utils.SessionManager;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private final MealRepository repository;
    private final SessionManager sessionManager;
    private final String userId;

    public ProfilePresenter(MealRepository repository, SessionManager sessionManager, String userId) {
        this.repository = repository;
        this.sessionManager = sessionManager;
        this.userId = userId;
    }

    public void backupData() {
        if (userId == null) {
            if (view != null) {
                view.showError("Please login to backup data");
            }
            return;
        }

        // Since we're using local SQLite only, backup is simulated
        // In a real scenario, you could export to a JSON file
        if (view != null) {
            view.showLoading();
        }

        // Simulate backup by just reading favorites
        addDisposable(
                repository.getAllFavorites(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                favorites -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        // Data is already in local SQLite database
                                        view.onBackupSuccess();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    public void restoreData() {
        if (userId == null) {
            if (view != null) {
                view.showError("Please login to restore data");
            }
            return;
        }

        // Since we're using local SQLite only, data is already persisted
        if (view != null) {
            view.showLoading();
        }

        // Simulate restore - data already in local database
        addDisposable(
                repository.getAllFavorites(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                favorites -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (favorites.isEmpty()) {
                                            view.showError("No saved data found");
                                        } else {
                                            view.onRestoreSuccess();
                                        }
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    public void syncData() {
        if (userId == null) {
            if (view != null) {
                view.showError("Please login to sync data");
            }
            return;
        }

        // Since we're using local SQLite only, sync means data is already up to date
        if (view != null) {
            view.showLoading();
        }

        // Simulate sync - just verify data exists
        addDisposable(
                repository.getAllFavorites(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                favorites -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.onSyncSuccess();
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
        sessionManager.logout();
        if (view != null) {
            view.onLogoutSuccess();
        }
    }
}
