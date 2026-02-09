package com.example.foodplanner.presentation.categories.view;

import com.example.foodplanner.base.BaseView;
import com.example.foodplanner.data.meal.model.Meal;

import java.util.List;

/**
 * View interface for Category Meals screen (MVP pattern).
 */
public interface CategoryMealsView extends BaseView {
    void showMeals(List<Meal> meals);

    void showEmpty();
}
