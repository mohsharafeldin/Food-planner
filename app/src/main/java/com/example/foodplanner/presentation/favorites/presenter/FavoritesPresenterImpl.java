package com.example.foodplanner.presentation.favorites.presenter;

import com.example.foodplanner.presentation.base.BasePresenter;
import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.repositry.MealRepository;
import com.example.foodplanner.presentation.favorites.view.FavoritesView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

public class FavoritesPresenterImpl extends BasePresenter<FavoritesView> implements FavoritesPresenterContract {

    private final MealRepository repository;
    private final String userId;
    private List<FavoriteMeal> allFavorites;

    public FavoritesPresenterImpl(MealRepository repository, String userId) {
        this.repository = repository;
        this.userId = userId;
    }

    @Override
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
                                            allFavorites = new ArrayList<>();
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

    @Override
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

        List<FavoriteMeal> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (FavoriteMeal meal : allFavorites) {
            if (meal.getStrMeal() != null && meal.getStrMeal().toLowerCase().contains(lowerQuery)) {
                filteredList.add(meal);
            }
        }

        if (view != null) {
            view.showFavorites(filteredList);
        }
    }

    @Override
    public void removeFavorite(FavoriteMeal favorite) {
        addDisposable(
                repository.removeFavorite(favorite)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.onFavoriteRemoved();
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
