package com.example.foodplanner.presentation.auth.presenter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.example.foodplanner.presentation.auth.view.AuthView;
import com.example.foodplanner.presentation.base.BasePresenter;
import com.example.foodplanner.data.firebase.FirebaseAuthHelper;
import com.example.foodplanner.data.firebase.FirebaseSyncHelper;
import com.example.foodplanner.repositry.MealRepository;
import com.example.foodplanner.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthPresenterImpl extends BasePresenter<AuthView> implements AuthPresenterContract {

    private static final String TAG = "AuthPresenterImpl";

    private final FirebaseAuthHelper authHelper;
    private final FirebaseSyncHelper syncHelper;
    private final MealRepository repository;
    private final SessionManager sessionManager;
    private final Context context;

    private static final String WEB_CLIENT_ID = "464510016637-hqrhqf8ko91tsh32donukb8fob415jsg.apps.googleusercontent.com";

    public AuthPresenterImpl(Context context) {
        this.context = context;
        this.authHelper = FirebaseAuthHelper.getInstance();
        this.syncHelper = FirebaseSyncHelper.getInstance();
        this.repository = MealRepository.getInstance(context);
        this.sessionManager = SessionManager.getInstance();
    }

    @Override
    public boolean isLoggedIn() {
        return authHelper.isLoggedIn() || sessionManager.isLoggedIn();
    }

    @Override
    public void login(String email, String password) {
        if (!validateInput(email, password)) {
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        authHelper.signIn(email, password, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                handleLoginSuccess(user);
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.onError(message);
                }
            }
        });
    }

    @Override
    public void signUp(String name, String email, String password, String confirmPassword) {
        if (!validateSignUpInput(name, email, password, confirmPassword)) {
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        authHelper.signUp(email, password, name, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                if (view != null) {
                    view.hideLoading();
                }
                saveFirebaseSession(user);
                if (view != null) {
                    view.onSignUpSuccess();
                }
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.onError(message);
                }
            }
        });
    }

    @Override
    public void signInWithGoogle(Activity activity) {
        if (view != null) {
            view.showLoading();
        }

        authHelper.signInWithGoogle(activity, WEB_CLIENT_ID, new FirebaseAuthHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                handleLoginSuccess(user);
            }

            @Override
            public void onError(String message) {
                if (view != null) {
                    view.hideLoading();
                    view.onError(message);
                }
            }
        });
    }

    private void handleLoginSuccess(FirebaseUser user) {
        saveFirebaseSession(user);

        String userId = user.getUid();

        // Step 1: Migrate guest data to this user
        // Step 2: Check if Firebase has data for this user
        // Step 3: If Firebase has data -> restore it (replaces local)
        // If Firebase empty -> backup local data to Firebase
        addDisposable(
                repository.migrateAllData(userId)
                        .andThen(syncHelper.hasUserData(userId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                hasFirebaseData -> {
                                    if (hasFirebaseData) {
                                        // Firebase has data, restore it
                                        restoreFromFirebase(userId);
                                    } else {
                                        // Firebase is empty, backup local data
                                        backupToFirebase(userId);
                                    }
                                },
                                error -> {
                                    // On error, just complete login with local data
                                    Log.e(TAG, "Error checking Firebase data", error);
                                    completeLogin();
                                }));
    }

    private void restoreFromFirebase(String userId) {
        addDisposable(
                repository.restoreAllData(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> completeLogin(),
                                error -> {
                                    Log.e(TAG, "Error restoring data", error);
                                    completeLogin();
                                }));
    }

    private void backupToFirebase(String userId) {
        addDisposable(
                repository.getAllFavorites(userId).firstOrError()
                        .subscribeOn(Schedulers.io())
                        .flatMapCompletable(favorites -> syncHelper.backupFavorites(userId, favorites))
                        .andThen(repository.getAllPlannedMeals(userId).firstOrError())
                        .flatMapCompletable(meals -> syncHelper.backupPlannedMeals(userId, meals))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> completeLogin(),
                                error -> {
                                    // Backup failed (offline?), just complete with local data
                                    Log.e(TAG, "Error backing up data", error);
                                    completeLogin();
                                }));
    }

    private void completeLogin() {
        if (view != null) {
            view.hideLoading();
            view.onLoginSuccess();
        }
    }

    private void saveFirebaseSession(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
            String email = user.getEmail() != null ? user.getEmail() : "";
            sessionManager.saveFirebaseSession(user.getUid(), email, name);
        }
    }

    @Override
    public void continueAsGuest() {
        sessionManager.saveGuestSession();
        if (view != null) {
            view.onGuestMode();
        }
    }

    @Override
    public void logout() {
        // Clear local data for current user before signing out
        String userId = sessionManager.getUserId();
        if (userId != null && !userId.isEmpty()) {
            addDisposable(
                    repository.clearUserData(userId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> {
                                        authHelper.signOut();
                                        sessionManager.logout();
                                    },
                                    error -> {
                                        // Even on error, proceed with logout
                                        authHelper.signOut();
                                        sessionManager.logout();
                                    }));
        } else {
            authHelper.signOut();
            sessionManager.logout();
        }
    }

    private boolean validateInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            if (view != null) {
                view.onError("Please enter your email");
            }
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (view != null) {
                view.onError("Please enter a valid email");
            }
            return false;
        }

        if (password == null || password.length() < 6) {
            if (view != null) {
                view.onError("Password must be at least 6 characters");
            }
            return false;
        }

        return true;
    }

    private boolean validateSignUpInput(String name, String email, String password, String confirmPassword) {
        if (name == null || name.trim().isEmpty()) {
            if (view != null) {
                view.onError("Please enter your name");
            }
            return false;
        }

        if (!validateInput(email, password)) {
            return false;
        }

        if (!password.equals(confirmPassword)) {
            if (view != null) {
                view.onError("Passwords do not match");
            }
            return false;
        }

        return true;
    }
}
