package com.example.foodplanner.presentation.profile.presenter;

import com.example.foodplanner.presentation.profile.view.ProfileView;

/**
 * Contract interface for Profile Presenter following MVP pattern.
 */
public interface ProfilePresenterContract {
    void attachView(ProfileView view);

    void detachView();

    void logout();
}
