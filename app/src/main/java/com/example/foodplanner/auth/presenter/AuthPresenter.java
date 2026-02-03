package com.example.foodplanner.auth.presenter;

import android.content.Context;

import com.example.foodplanner.auth.view.AuthView;
import com.example.foodplanner.data.local.AppDatabase;
import com.example.foodplanner.data.local.UserDao;
import com.example.foodplanner.data.model.User;
import com.example.foodplanner.utils.SessionManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AuthPresenter {

    private AuthView view;
    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public AuthPresenter(Context context) {
        this.userDao = AppDatabase.getInstance(context).userDao();
        this.sessionManager = new SessionManager(context);
    }

    public void attachView(AuthView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        disposables.clear();
    }

    public void login(String email, String password) {
        if (!validateInput(email, password)) {
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        String passwordHash = User.hashPassword(password);

        disposables.add(
                userDao.authenticate(email, passwordHash)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                user -> {
                                    if (view != null) {
                                        view.hideLoading();
                                    }
                                    // Save session
                                    sessionManager.saveUserSession(user.getId(), user.getEmail(), user.getName());
                                    if (view != null) {
                                        view.onLoginSuccess();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.onError("Invalid email or password");
                                    }
                                }));
    }

    public void signUp(String name, String email, String password, String confirmPassword) {
        if (!validateSignUpInput(name, email, password, confirmPassword)) {
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        // First check if email already exists
        disposables.add(
                userDao.checkEmailExists(email)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                count -> {
                                    if (count > 0) {
                                        if (view != null) {
                                            view.hideLoading();
                                            view.onError("Email already registered");
                                        }
                                    } else {
                                        // Create new user
                                        createUser(name, email, password);
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.onError("Sign up failed: " + error.getMessage());
                                    }
                                }));
    }

    private void createUser(String name, String email, String password) {
        User newUser = new User(name, email, password);

        disposables.add(
                userDao.insertUser(newUser)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.hideLoading();
                                    }
                                    // Save session
                                    sessionManager.saveUserSession(newUser.getId(), email, name);
                                    if (view != null) {
                                        view.onSignUpSuccess();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.onError("Sign up failed: " + error.getMessage());
                                    }
                                }));
    }

    public void continueAsGuest() {
        sessionManager.saveGuestSession();
        if (view != null) {
            view.onGuestMode();
        }
    }

    public void logout() {
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
