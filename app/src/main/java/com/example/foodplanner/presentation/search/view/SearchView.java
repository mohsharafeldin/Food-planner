package com.example.foodplanner.presentation.search.view;

import com.example.foodplanner.base.BaseView;
import com.example.foodplanner.data.meal.model.Meal;

import java.util.List;

public interface SearchView extends BaseView {
    void showSearchResults(List<Meal> meals);

    void showEmptyResults();

    void showSuggestions(List<String> suggestions);
}
