package com.example.foodplanner.presentation.favorites.presenter;

import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.presentation.favorites.view.FavoritesView;

/**
 * Contract interface for Favorites Presenter following MVP pattern.
 * Defines the operations that the Favorites presenter must implement.
 */
public interface FavoritesPresenterContract {
    void attachView(FavoritesView view);

    void detachView();

    void loadFavorites();

    void search(String query);

    void removeFavorite(FavoriteMeal favorite);
}
