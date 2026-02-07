package com.example.foodplanner.mealdetails.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.meal.model.Meal;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.mealdetails.view.MealDetailsView;

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
                repository.removeFavoriteById(currentMeal.getIdMeal(), userId)
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
        if (userId == null) {
            if (view != null) {
                view.showError("Please login to add to plan");
            }
            return;
        }

        if (currentMeal == null) {
            if (view != null) {
                view.showError("Meal data is still loading...");
            }
            return;
        }

        if (!isValidCategory(mealType, currentMeal.getStrCategory())) {
            if (view != null) {
                view.showError("Cannot add " + currentMeal.getStrCategory() + " to " + mealType);
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

    private boolean isValidCategory(String mealType, String category) {
        if (category == null)
            return true;

        switch (mealType) {
            case "Breakfast":
                return category.equalsIgnoreCase("Breakfast");
            case "Lunch":
            case "Dinner":
                return isMainCourse(category);
            case "Dessert":
                return isDessert(category);
            default:
                return true;
        }
    }

    private boolean isMainCourse(String category) {
        String[] mains = { "Beef", "Chicken", "Lamb", "Pasta", "Pork", "Seafood", "Vegan", "Vegetarian", "Goat",
                "Miscellaneous", "Side", "Starter" };
        for (String type : mains) {
            if (type.equalsIgnoreCase(category))
                return true;
        }
        return false;
    }

    private boolean isDessert(String category) {
        String[] snacks = { "Dessert", "Side", "Starter", "Miscellaneous", "Breakfast" };
        // Note: Some users might eat breakfast items as snacks, but strictly:
        // Let's stick to Dessert, Side, Starter.
        // User asked for logic to prevent inappropriate.
        // I will include Dessert, Side, Starter.
        String[] validSnacks = { "Dessert", "Side", "Starter", "Miscellaneous" };
        for (String type : validSnacks) {
            if (type.equalsIgnoreCase(category))
                return true;
        }
        return false;
    }

    public Meal getCurrentMeal() {
        return currentMeal;
    }
}
