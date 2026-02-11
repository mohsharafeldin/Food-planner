package com.example.foodplanner.presentation.planner.view;

import com.example.foodplanner.presentation.base.BaseView;
import com.example.foodplanner.data.model.PlannedMeal;

import java.util.List;

public interface PlannerView extends BaseView {
    void showPlannedMeals(List<PlannedMeal> meals);

    void showEmptyPlanner();

    void onMealRemoved();
}
