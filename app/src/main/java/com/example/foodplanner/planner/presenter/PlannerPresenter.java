package com.example.testfoodplanner.planner.presenter;

import com.example.testfoodplanner.base.BasePresenter;
import com.example.testfoodplanner.data.model.PlannedMeal;
import com.example.testfoodplanner.data.repository.MealRepository;
import com.example.testfoodplanner.planner.view.PlannerView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PlannerPresenter extends BasePresenter<PlannerView> {

    private final MealRepository repository;
    private final String userId;

    public PlannerPresenter(MealRepository repository, String userId) {
        this.repository = repository;
        this.userId = userId;
    }

    public void loadPlannedMeals(String date) {
        if (userId == null) {
            if (view != null) {
                view.showEmptyPlanner();
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        addDisposable(
                repository.getPlannedMealsByDate(date, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                meals -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (meals != null && !meals.isEmpty()) {
                                            view.showPlannedMeals(meals);
                                        } else {
                                            view.showEmptyPlanner();
                                        }
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    public void loadWeeklyMeals(String startDate, String endDate) {
        if (userId == null) {
            if (view != null) {
                view.showEmptyPlanner();
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        addDisposable(
                repository.getPlannedMealsByDateRange(startDate, endDate, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                meals -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (meals != null && !meals.isEmpty()) {
                                            view.showPlannedMeals(meals);
                                        } else {
                                            view.showEmptyPlanner();
                                        }
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    public void removePlannedMeal(PlannedMeal meal) {
        addDisposable(
                repository.removePlannedMeal(meal)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.onMealRemoved();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError(error.getMessage());
                                    }
                                }));
    }
}
