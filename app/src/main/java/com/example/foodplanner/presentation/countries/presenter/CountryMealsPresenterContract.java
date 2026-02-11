package com.example.foodplanner.presentation.countries.presenter;

import com.example.foodplanner.presentation.countries.view.CountryMealsView;

/**
 * Contract interface for Country Meals Presenter (MVP pattern).
 */
public interface CountryMealsPresenterContract {
    void attachView(CountryMealsView view);

    void detachView();

    void loadMealsByCountry(String countryName);
}
