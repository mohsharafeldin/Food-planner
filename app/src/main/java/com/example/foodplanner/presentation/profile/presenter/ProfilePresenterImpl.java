package com.example.foodplanner.presentation.profile.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.firebase.FirebaseAuthHelper;
import com.example.foodplanner.data.meal.repositry.MealRepository;
import com.example.foodplanner.presentation.profile.view.ProfileView;
import com.example.foodplanner.utils.SessionManager;

public class ProfilePresenterImpl extends BasePresenter<ProfileView> implements ProfilePresenterContract {

    private final MealRepository repository;
    private final FirebaseAuthHelper authHelper;
    private final SessionManager sessionManager;
    private final String userId;

    public ProfilePresenterImpl(MealRepository repository, SessionManager sessionManager, String userId) {
        this.repository = repository;
        this.sessionManager = sessionManager;
        this.userId = userId;
        this.authHelper = FirebaseAuthHelper.getInstance();
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
