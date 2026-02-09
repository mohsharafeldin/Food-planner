package com.example.foodplanner.presentation.mealdetails.presenter;

import com.example.foodplanner.data.meal.model.Meal;
import com.example.foodplanner.presentation.mealdetails.view.MealDetailsView;

/**
 * Contract interface for MealDetails Presenter following MVP pattern.
 */
public interface MealDetailsPresenterContract {
    void attachView(MealDetailsView view);

    void detachView();

    void loadMealDetails(String mealId);

    void toggleFavorite(boolean currentlyFavorite);

    void addToPlan(String date, String mealType);

    Meal getCurrentMeal();
}
