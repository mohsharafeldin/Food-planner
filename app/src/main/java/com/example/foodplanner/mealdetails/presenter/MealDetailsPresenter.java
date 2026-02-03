package com.example.testfoodplanner.mealdetails.presenter;

import com.example.testfoodplanner.base.BasePresenter;
import com.example.testfoodplanner.data.model.Meal;
import com.example.testfoodplanner.data.repository.MealRepository;
import com.example.testfoodplanner.mealdetails.view.MealDetailsView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsPresenter extends BasePresenter<MealDetailsView> {

    private final MealRepository repository;
    private Meal currentMeal;
    private final String userId;

    public MealDetailsPresenter(MealRepository repository, String userId) {
        this.repository = repository;
        this.userId = userId;
    }

    public void loadMealDetails(String mealId) {
        if (view != null) {
            view.showLoading();
        }

        addDisposable(
                repository.getMealById(mealId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                            currentMeal = response.getMeals().get(0);
                                            view.showMealDetails(currentMeal);
                                            checkFavoriteStatus(mealId);
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

    private void checkFavoriteStatus(String mealId) {
        if (userId == null)
            return;

        addDisposable(
                repository.isFavorite(mealId, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                isFavorite -> {
                                    if (view != null) {
                                        view.showFavoriteStatus(isFavorite);
                                    }
                                },
                                error -> {
                                    // Ignore error, default to not favorite
                                }));
    }

    public void toggleFavorite(boolean currentlyFavorite) {
        if (currentMeal == null || userId == null) {
            if (view != null) {
                view.showError("Please login to add favorites");
            }
            return;
        }

        if (currentlyFavorite) {
            removeFavorite();
        } else {
            addFavorite();
        }
    }

    private void addFavorite() {
        addDisposable(
                repository.addFavorite(currentMeal, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.onFavoriteAdded();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    private void removeFavorite() {
        addDisposable(
                repository.removeFavoriteById(currentMeal.getIdMeal())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.onFavoriteRemoved();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    public void addToPlan(String date, String mealType) {
        if (currentMeal == null || userId == null) {
            if (view != null) {
                view.showError("Please login to add to plan");
            }
            return;
        }

        addDisposable(
                repository.addPlannedMeal(currentMeal, date, mealType, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.onAddedToPlan();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError(error.getMessage());
                                    }
                                }));
    }

    public Meal getCurrentMeal() {
        return currentMeal;
    }
}
