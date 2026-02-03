package com.example.foodplanner.base;

public interface BaseView {
    void showLoading();

    void hideLoading();

    void showError(String message);

    void showNetworkError();
}
