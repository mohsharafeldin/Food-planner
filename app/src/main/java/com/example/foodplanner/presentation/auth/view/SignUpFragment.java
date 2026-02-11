package com.example.foodplanner.presentation.auth.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class SignUpFragment extends Fragment implements AuthView {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private ProgressBar progressBar;
    private TextView tvLogin;
    private ImageButton btnBack;

    private AuthPresenterContract presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPresenter();
        setupListeners();
    }

    private void initViews(View view) {
        etName = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnSignUp = view.findViewById(R.id.btn_signup);
        progressBar = view.findViewById(R.id.progress_bar);
        tvLogin = view.findViewById(R.id.tv_login);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void initPresenter() {
        presenter = new AuthPresenterImpl(requireContext());
        presenter.attachView(this);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim()
                    : "";

            presenter.signUp(name, email, password, confirmPassword);
        });

        tvLogin.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        }
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnSignUp.setEnabled(false);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
        btnSignUp.setEnabled(true);
    }

    @Override
    public void onLoginSuccess() {
        // Not used in signup fragment
    }

    @Override
    public void onSignUpSuccess() {
        if (getView() != null) {
            SnackbarUtils.showSuccess(getView(), "Account created successfully!");
        }
        navigateToMain();
    }

    @Override
    public void onError(String message) {
        if (getView() != null) {
            SnackbarUtils.showError(getView(), message);
        }
    }

    @Override
    public void onGuestMode() {
        // Not used in signup fragment
    }

    @Override
    public void showError(String message) {
        if (getView() != null) {
            SnackbarUtils.showError(getView(), message);
        }
    }

    @Override
    public void showNetworkError() {
        if (getView() != null) {
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
