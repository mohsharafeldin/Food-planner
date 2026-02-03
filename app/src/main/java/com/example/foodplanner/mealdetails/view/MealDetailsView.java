package com.example.testfoodplanner.mealdetails.view;

import com.example.testfoodplanner.base.BaseView;
import com.example.testfoodplanner.data.model.Meal;

public interface MealDetailsView extends BaseView {
    void showMealDetails(Meal meal);

    void showFavoriteStatus(boolean isFavorite);

    void onFavoriteAdded();

    void onFavoriteRemoved();

    void onAddedToPlan();
}
