package com.example.foodplanner.favorites.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.favorites.view.FavoritesView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FavoritesPresenter extends BasePresenter<FavoritesView> {

    private final MealRepository repository;
    private final String userId;

    public FavoritesPresenter(MealRepository repository, String userId) {
        this.repository = repository;
        this.userId = userId;
    }

    private java.util.List<FavoriteMeal> allFavorites;

    public void loadFavorites() {
        if (userId == null) {
            if (view != null) {
                view.showEmptyFavorites();
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        addDisposable(
                repository.getAllFavorites(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                favorites -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (favorites != null && !favorites.isEmpty()) {
                                            allFavorites = favorites;
                                            view.showFavorites(favorites);
                                        } else {
                                            allFavorites = new java.util.ArrayList<>();
                                            view.showEmptyFavorites();
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

    public void search(String query) {
        if (allFavorites == null || allFavorites.isEmpty()) {
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            if (view != null) {
                view.showFavorites(allFavorites);
            }
            return;
        }

        java.util.List<FavoriteMeal> filteredList = new java.util.ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (FavoriteMeal meal : allFavorites) {
            if (meal.getStrMeal() != null && meal.getStrMeal().toLowerCase().contains(lowerQuery)) {
                filteredList.add(meal);
            }
        }

        if (view != null) {
            if (filteredList.isEmpty()) {
                // Optionally show a specific "no match" state, or just empty
                view.showFavorites(filteredList); // Adapter handles empty list or we can show empty state
            } else {
                view.showFavorites(filteredList);
            }
        }
    }

    public void removeFavorite(FavoriteMeal favorite) {
        addDisposable(
                repository.removeFavorite(favorite)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.onFavoriteRemoved();
                                        // Update local list
                                        if (allFavorites != null) {
                                            allFavorites.remove(favorite);
                                        }
                                        loadFavorites();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError(error.getMessage());
                                    }
                                }));
    }
}
