package com.example.testfoodplanner.planner.view;

import com.example.testfoodplanner.base.BaseView;
import com.example.testfoodplanner.data.model.PlannedMeal;

import java.util.List;

public interface PlannerView extends BaseView {
    void showPlannedMeals(List<PlannedMeal> meals);

    void showEmptyPlanner();

    void onMealRemoved();
}
