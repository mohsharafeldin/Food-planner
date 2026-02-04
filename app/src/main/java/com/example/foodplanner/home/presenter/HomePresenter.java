package com.example.foodplanner.home.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.repository.MealRepository;
import com.example.foodplanner.home.view.HomeView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomePresenter extends BasePresenter<HomeView> {

    private final MealRepository repository;

    public HomePresenter(MealRepository repository) {
        this.repository = repository;
    }

    public void loadMealOfTheDay() {
        if (view != null) {
            view.showLoading();
        }

        addDisposable(
                repository.getRandomMeal()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                            view.showMealOfTheDay(response.getMeals().get(0));
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

    public void loadCategories() {
        addDisposable(
                repository.getAllCategories()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null && response.getCategories() != null) {
                                        view.showCategories(response.getCategories());
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    public void loadCountries() {
        addDisposable(
                repository.getAllAreas()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null && response.getAreas() != null) {
                                        view.showCountries(response.getAreas());
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError(error.getMessage());
                                    }
                                }));
    }
}
