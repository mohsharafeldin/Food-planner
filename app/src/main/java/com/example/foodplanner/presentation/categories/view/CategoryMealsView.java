package com.example.foodplanner.presentation.categories.view;

import com.example.foodplanner.presentation.base.BaseView;
import com.example.foodplanner.data.model.Meal;

import java.util.List;

/**
 * View interface for Category Meals screen (MVP pattern).
 */
public interface CategoryMealsView extends BaseView {
    void showMeals(List<Meal> meals);

    void showEmpty();
}
