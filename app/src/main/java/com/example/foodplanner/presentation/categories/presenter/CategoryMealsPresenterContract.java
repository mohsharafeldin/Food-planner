package com.example.foodplanner.presentation.categories.presenter;

import com.example.foodplanner.presentation.categories.view.CategoryMealsView;

/**
 * Contract interface for Category Meals Presenter (MVP pattern).
 */
public interface CategoryMealsPresenterContract {
    void attachView(CategoryMealsView view);

    void detachView();

    void loadMealsByCategory(String categoryName);
}
