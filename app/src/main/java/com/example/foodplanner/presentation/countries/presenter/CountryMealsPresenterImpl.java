package com.example.foodplanner.presentation.countries.presenter;

import com.example.foodplanner.presentation.base.BasePresenter;
import com.example.foodplanner.presentation.countries.view.CountryMealsView;
import com.example.foodplanner.repositry.MealRepositoryInterface;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Presenter implementation for Country Meals screen (MVP pattern).
 */
public class CountryMealsPresenterImpl extends BasePresenter<CountryMealsView>
        implements CountryMealsPresenterContract {

    private final MealRepositoryInterface repository;

    public CountryMealsPresenterImpl(MealRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public void loadMealsByCountry(String countryName) {
        if (!isViewAttached() || countryName == null)
            return;

        view.showLoading();

        addDisposable(
                repository.filterByArea(countryName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (!isViewAttached())
                                        return;
                                    view.hideLoading();
                                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                        view.showMeals(response.getMeals());
                                    } else {
                                        view.showEmpty();
                                    }
                                },
                                error -> {
                                    if (!isViewAttached())
                                        return;
                                    view.hideLoading();
                                    view.showError(error.getMessage());
                                }));
    }
}
