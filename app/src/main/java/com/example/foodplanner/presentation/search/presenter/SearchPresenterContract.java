package com.example.foodplanner.presentation.search.presenter;

import com.example.foodplanner.presentation.search.view.SearchView;

/**
 * Contract interface for Search Presenter following MVP pattern.
 */
public interface SearchPresenterContract {
    void attachView(SearchView view);

    void detachView();

    void search(String query, String searchType);

    void loadSuggestions(String searchType);
}
