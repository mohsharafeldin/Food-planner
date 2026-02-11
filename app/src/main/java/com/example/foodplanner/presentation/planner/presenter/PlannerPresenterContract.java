package com.example.foodplanner.presentation.planner.presenter;

import com.example.foodplanner.data.model.PlannedMeal;
import com.example.foodplanner.presentation.planner.view.PlannerView;

/**
 * Contract interface for Planner Presenter following MVP pattern.
 */
public interface PlannerPresenterContract {
    void attachView(PlannerView view);

    void detachView();

    void loadPlannedMeals(String date);

    void loadWeeklyMeals(String startDate, String endDate);

    void removePlannedMeal(PlannedMeal meal);
}
