package com.example.testfoodplanner.search.view;

import com.example.testfoodplanner.base.BaseView;
import com.example.testfoodplanner.data.model.Meal;

import java.util.List;

public interface SearchView extends BaseView {
    void showSearchResults(List<Meal> meals);

    void showEmptyResults();

    void showSuggestions(List<String> suggestions);
}
