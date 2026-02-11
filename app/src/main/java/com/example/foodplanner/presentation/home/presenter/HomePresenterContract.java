package com.example.foodplanner.presentation.home.presenter;

import com.example.foodplanner.presentation.home.view.HomeView;

/**
 * Contract interface for Home Presenter following MVP pattern.
 */
public interface HomePresenterContract {
    void attachView(HomeView view);

    void detachView();

    void loadMealOfTheDay();

    void loadCategories();

    void loadCountries();
}
