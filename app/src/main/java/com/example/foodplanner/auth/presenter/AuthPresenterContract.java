package com.example.foodplanner.auth.presenter;

import android.app.Activity;

import com.example.foodplanner.auth.view.AuthView;

/**
 * Contract interface for Auth Presenter following MVP pattern.
 */
public interface AuthPresenterContract {
    void attachView(AuthView view);

    void detachView();

    boolean isLoggedIn();

    void login(String email, String password);

    void signUp(String name, String email, String password, String confirmPassword);

    void signInWithGoogle(Activity activity);

    void continueAsGuest();

    void logout();
}
