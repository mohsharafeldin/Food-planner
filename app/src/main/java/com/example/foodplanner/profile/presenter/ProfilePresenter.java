package com.example.foodplanner.profile.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.auth.firebase.FirebaseAuthHelper;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.profile.view.ProfileView;
import com.example.foodplanner.utils.SessionManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfilePresenter extends BasePresenter<ProfileView> {

    private final MealRepository repository;
    private final FirebaseAuthHelper authHelper;
    private final SessionManager sessionManager;
    private final String userId;

    public ProfilePresenter(MealRepository repository, SessionManager sessionManager, String userId) {
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
