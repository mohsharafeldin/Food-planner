package com.example.foodplanner.profile.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.foodplanner.R;
import com.example.foodplanner.auth.view.AuthActivity;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.profile.presenter.ProfilePresenter;
import com.example.foodplanner.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements ProfileView {

    private CircleImageView ivProfile;
    private TextView tvUserName, tvUserEmail;
    private CardView cardGuestPrompt;
    private MaterialButton btnLogin, btnLogout;
    private ProgressBar progressBar;

    private ProfilePresenter presenter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initSessionManager();
        initPresenter();
        setupUI();
        setupListeners();
    }

    private void initViews(View view) {
        ivProfile = view.findViewById(R.id.iv_profile);
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        cardGuestPrompt = view.findViewById(R.id.card_guest_prompt);
        btnLogin = view.findViewById(R.id.btn_login);
        btnLogout = view.findViewById(R.id.btn_logout);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void initSessionManager() {
        sessionManager = new SessionManager(requireContext());
    }

    private void initPresenter() {
        MealRepository repository = MealRepository.getInstance(requireContext());
        String userId = sessionManager.getUserId();
        presenter = new ProfilePresenter(repository, sessionManager, userId);
        presenter.attachView(this);
    }

    private void setupUI() {
        boolean isLoggedIn = sessionManager.isLoggedIn();
        boolean isGuest = sessionManager.isGuest();

        // Display user info
        if (isLoggedIn && !isGuest) {
            String userName = sessionManager.getUserName();
            String userEmail = sessionManager.getUserEmail();

            tvUserName.setText(userName != null && !userName.isEmpty() ? userName : "User");
            tvUserEmail.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "");

            // Hide guest prompt
            cardGuestPrompt.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            // Guest mode
            tvUserName.setText("Guest");
            tvUserEmail.setText("Login to sync your data");

            // Show guest prompt
            cardGuestPrompt.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setText("Exit Guest Mode");
        }
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> navigateToAuth());
        btnLogout.setOnClickListener(v -> presenter.logout());
    }

    private void navigateToAuth() {
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNetworkError() {
        Toast.makeText(requireContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLogoutSuccess() {
        navigateToAuth();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
