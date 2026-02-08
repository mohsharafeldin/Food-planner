package com.example.foodplanner.auth.presenter;

import android.app.Activity;
import android.content.Context;

import com.example.foodplanner.auth.view.AuthView;
import com.example.foodplanner.data.auth.firebase.FirebaseAuthHelper;
import com.example.foodplanner.data.auth.firebase.FirebaseSyncHelper;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthPresenter {

    private AuthView view;
    private final FirebaseAuthHelper authHelper;
    private final FirebaseSyncHelper syncHelper;
    private final MealRepository repository;
    private final SessionManager sessionManager;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Context context;

    private static final String WEB_CLIENT_ID = "464510016637-hqrhqf8ko91tsh32donukb8fob415jsg.apps.googleusercontent.com";

    public AuthPresenter(Context context) {
        this.context = context;
        this.authHelper = FirebaseAuthHelper.getInstance();
        this.syncHelper = FirebaseSyncHelper.getInstance();
        this.repository = MealRepository.getInstance(context);
        this.sessionManager = new SessionManager(context);
    }

    public void attachView(AuthView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        disposables.clear();
    }

    /**
     * Check if user is already logged in (auto-login)
     */
    public boolean isLoggedIn() {
        return authHelper.isLoggedIn() || sessionManager.isLoggedIn();
    }

    /**
     * Login with email and password using Firebase
     */
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

    /**
     * Sign up with email and password using Firebase
     */
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
                // Save session with Firebase UID
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

    /**
     * Sign in with Google
     */
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

    /**
     * Handle successful login - save session and sync data
     */
    private void handleLoginSuccess(FirebaseUser user) {
        saveFirebaseSession(user);

        // Restore data from Firestore and merge with local
        String userId = user.getUid();
        syncDataFromFirestore(userId);
    }

    /**
     * Sync data from Firestore to local Room database
     */
    private void syncDataFromFirestore(String userId) {
        // Restore favorites
        disposables.add(
                syncHelper.restoreFavorites(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                favorites -> {
                                    // Save to local Room database
                                    for (var meal : favorites) {
                                        meal.setUserId(userId);
                                        disposables.add(
                                                repository.addFavoriteDirectly(meal)
                                                        .subscribeOn(Schedulers.io())
                                                        .subscribe(() -> {
                                                        }, error -> {
                                                        }));
                                    }

                                    // Now restore planned meals
                                    restorePlannedMeals(userId);
                                },
                                error -> {
                                    // Even if restore fails, continue to login
                                    restorePlannedMeals(userId);
                                }));
    }

    private void restorePlannedMeals(String userId) {
        disposables.add(
                syncHelper.restorePlannedMeals(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                plannedMeals -> {
                                    // Save to local Room database
                                    for (var meal : plannedMeals) {
                                        meal.setUserId(userId);
                                        disposables.add(
                                                repository.insertPlannedMealDirectly(meal)
                                                        .subscribeOn(Schedulers.io())
                                                        .subscribe(() -> {
                                                        }, error -> {
                                                        }));
                                    }
                                    completeLogin();
                                },
                                error -> {
                                    // Even if restore fails, continue to login
                                    completeLogin();
                                }));
    }

    private void completeLogin() {
        if (view != null) {
            view.hideLoading();
            view.onLoginSuccess();
        }
    }

    /**
     * Save Firebase user session to SharedPreferences
     */
    private void saveFirebaseSession(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName() != null ? user.getDisplayName() : "User";
            String email = user.getEmail() != null ? user.getEmail() : "";
            // Use Firebase UID as the user ID
            sessionManager.saveFirebaseSession(user.getUid(), email, name);
        }
    }

    /**
     * Continue as guest (no Firebase auth)
     */
    public void continueAsGuest() {
        sessionManager.saveGuestSession();
        if (view != null) {
            view.onGuestMode();
        }
    }

    /**
     * Logout from Firebase and clear session
     */
    public void logout() {
        authHelper.signOut();
        sessionManager.logout();
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
