package com.example.foodplanner.auth.view;

public interface AuthView {
    void showLoading();

    void hideLoading();

    void onLoginSuccess();

    void onSignUpSuccess();

    void onError(String message);

    void onGuestMode();
}
