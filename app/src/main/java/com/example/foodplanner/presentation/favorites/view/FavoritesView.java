package com.example.foodplanner.presentation.favorites.view;

import com.example.foodplanner.base.BaseView;
import com.example.foodplanner.data.meal.model.FavoriteMeal;

import java.util.List;

public interface FavoritesView extends BaseView {
    void showFavorites(List<FavoriteMeal> favorites);

    void showEmptyFavorites();

    void onFavoriteRemoved();
}
