package com.example.foodplanner.presentation.countries.view;

import com.example.foodplanner.base.BaseView;
import com.example.foodplanner.data.meal.model.Meal;

import java.util.List;

/**
 * View interface for Country Meals screen (MVP pattern).
 */
public interface CountryMealsView extends BaseView {
    void showMeals(List<Meal> meals);

    void showEmpty();
}
