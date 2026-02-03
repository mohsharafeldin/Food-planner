package com.example.testfoodplanner.favorites.view;

import com.example.testfoodplanner.base.BaseView;
import com.example.testfoodplanner.data.model.FavoriteMeal;

import java.util.List;

public interface FavoritesView extends BaseView {
    void showFavorites(List<FavoriteMeal> favorites);

    void showEmptyFavorites();

    void onFavoriteRemoved();
}
