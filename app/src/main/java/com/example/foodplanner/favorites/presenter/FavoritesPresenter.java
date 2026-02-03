package com.example.testfoodplanner.favorites.presenter;

import com.example.testfoodplanner.base.BasePresenter;
import com.example.testfoodplanner.data.model.FavoriteMeal;
import com.example.testfoodplanner.data.repository.MealRepository;
import com.example.testfoodplanner.favorites.view.FavoritesView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class FavoritesPresenter extends BasePresenter<FavoritesView> {

    private final MealRepository repository;
    private final String userId;

    public FavoritesPresenter(MealRepository repository, String userId) {
        this.repository = repository;
        this.userId = userId;
    }

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
                                            view.showFavorites(favorites);
                                        } else {
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

    public void removeFavorite(FavoriteMeal favorite) {
        addDisposable(
                repository.removeFavorite(favorite)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.onFavoriteRemoved();
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
