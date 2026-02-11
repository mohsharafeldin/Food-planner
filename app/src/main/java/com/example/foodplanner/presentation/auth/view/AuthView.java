package com.example.foodplanner.presentation.auth.view;

import com.example.foodplanner.presentation.base.BaseView;

/**
 * View interface for Auth screens following MVP pattern.
 * Extends BaseView for common loading/error operations.
 */
public interface AuthView extends BaseView {
    void onLoginSuccess();

    void onSignUpSuccess();

    void onError(String message);

    void onGuestMode();
}
