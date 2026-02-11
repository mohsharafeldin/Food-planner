package com.example.foodplanner.presentation.mealdetails.view;

import com.example.foodplanner.presentation.base.BaseView;
import com.example.foodplanner.data.model.Meal;

public interface MealDetailsView extends BaseView {
    void showMealDetails(Meal meal);

    void showFavoriteStatus(boolean isFavorite);

    void onFavoriteAdded();

    void onFavoriteRemoved();

    void onAddedToPlan();
}
