package com.example.foodplanner.presentation.categories.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.presentation.categories.view.CategoryMealsView;
import com.example.foodplanner.data.meal.repositry.MealRepositoryInterface;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Presenter implementation for Category Meals screen (MVP pattern).
 */
public class CategoryMealsPresenterImpl extends BasePresenter<CategoryMealsView>
        implements CategoryMealsPresenterContract {

    private final MealRepositoryInterface repository;

    public CategoryMealsPresenterImpl(MealRepositoryInterface repository) {
        this.repository = repository;
    }

    @Override
    public void loadMealsByCategory(String categoryName) {
        if (!isViewAttached() || categoryName == null)
            return;

        view.showLoading();

        addDisposable(
                repository.filterByCategory(categoryName)
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
