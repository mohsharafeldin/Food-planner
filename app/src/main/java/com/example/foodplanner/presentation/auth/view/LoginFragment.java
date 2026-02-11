package com.example.foodplanner.presentation.auth.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.foodplanner.utils.SnackbarUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.foodplanner.MainActivity;
import com.example.foodplanner.R;
import com.example.foodplanner.presentation.auth.presenter.AuthPresenterContract;
import com.example.foodplanner.presentation.auth.presenter.AuthPresenterImpl;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment implements AuthView {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnGuest, btnGoogleSignIn;
    private ProgressBar progressBar;
    private TextView tvSignUp;

    private AuthPresenterContract presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();

        // Check if already logged in (auto-login)
        if (presenter.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setupListeners();
    }

    private void initViews(View view) {
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        btnGuest = view.findViewById(R.id.btn_guest);
        btnGoogleSignIn = view.findViewById(R.id.btn_google_signin);
        progressBar = view.findViewById(R.id.progress_bar);
        tvSignUp = view.findViewById(R.id.tv_sign_up);
    }

    private void initPresenter() {
        presenter = new AuthPresenterImpl(requireContext());
        presenter.attachView(this);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            presenter.login(email, password);
        });

        btnGuest.setOnClickListener(v -> presenter.continueAsGuest());

        tvSignUp.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_login_to_signup));

        // Google Sign-In button
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                presenter.signInWithGoogle(requireActivity());
            });
        }
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnGuest.setEnabled(false);
        if (btnGoogleSignIn != null)
            btnGoogleSignIn.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
        btnGuest.setEnabled(true);
        if (btnGoogleSignIn != null)
            btnGoogleSignIn.setEnabled(true);
    }

    @Override
    public void onLoginSuccess() {
        navigateToMain();
    }

    @Override
    public void onSignUpSuccess() {
        // Not used in login fragment
    }

    @Override
    public void onError(String message) {
        if (isAdded() && getView() != null) {
            SnackbarUtils.showError(getView(), message);
        }
    }

    @Override
    public void onGuestMode() {
        navigateToMain();
    }

    @Override
    public void showError(String message) {
        if (isAdded() && getView() != null) {
            SnackbarUtils.showError(getView(), message);
        }
    }

    @Override
    public void showNetworkError() {
        if (isAdded() && getView() != null) {
            SnackbarUtils.showError(getView(), getString(R.string.network_error));
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
