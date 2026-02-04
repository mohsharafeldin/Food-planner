package com.example.foodplanner.search.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.model.Area;
import com.example.foodplanner.data.model.Category;
import com.example.foodplanner.data.model.Ingredient;
import com.example.foodplanner.data.repository.MealRepository;
import com.example.foodplanner.search.view.SearchView;
import com.example.foodplanner.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SearchPresenter extends BasePresenter<SearchView> {

    private final MealRepository repository;

    public SearchPresenter(MealRepository repository) {
        this.repository = repository;
    }

    public void search(String query, String searchType) {
        if (query == null || query.trim().isEmpty()) {
            if (view != null) {
                view.showEmptyResults();
            }
            return;
        }

        if (view != null) {
            view.showLoading();
        }

        switch (searchType) {
            case Constants.SEARCH_BY_NAME:
                searchByName(query);
                break;
            case Constants.SEARCH_BY_CATEGORY:
                searchByCategory(query);
                break;
            case Constants.SEARCH_BY_COUNTRY:
                searchByCountry(query);
                break;
            case Constants.SEARCH_BY_INGREDIENT:
                searchByIngredient(query);
                break;
        }
    }

    public void loadSuggestions(String searchType) {
        switch (searchType) {
            case Constants.SEARCH_BY_CATEGORY:
                loadCategories();
                break;
            case Constants.SEARCH_BY_COUNTRY:
                loadCountries();
                break;
            case Constants.SEARCH_BY_INGREDIENT:
                loadIngredients();
                break;
        }
    }

    private void loadCategories() {
        addDisposable(
                repository.getAllCategories()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null && response.getCategories() != null) {
                                        List<String> suggestions = new ArrayList<>();
                                        for (Category category : response.getCategories()) {
                                            suggestions.add(category.getStrCategory());
                                        }
                                        view.showSuggestions(suggestions);
                                    }
                                },
                                error -> {
                                    // Fail silently for suggestions
                                }));
    }

    private void loadCountries() {
        addDisposable(
                repository.getAllAreas()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null && response.getAreas() != null) {
                                        List<String> suggestions = new ArrayList<>();
                                        for (Area area : response.getAreas()) {
                                            suggestions.add(area.getStrArea());
                                        }
                                        view.showSuggestions(suggestions);
                                    }
                                },
                                error -> {
                                    // Fail silently for suggestions
                                }));
    }

    private void loadIngredients() {
        addDisposable(
                repository.getAllIngredients()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null && response.getIngredients() != null) {
                                        List<String> suggestions = new ArrayList<>();
                                        for (Ingredient ingredient : response.getIngredients()) {
                                            suggestions.add(ingredient.getStrIngredient());
                                        }
                                        view.showSuggestions(suggestions);
                                    }
                                },
                                error -> {
                                    // Fail silently for suggestions
                                }));
    }

    private void searchByName(String query) {
        String formattedQuery = capitalizeFirstLetter(query);
        addDisposable(
                repository.searchMealsByName(formattedQuery)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                            view.showSearchResults(response.getMeals());
                                        } else {
                                            view.showEmptyResults();
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

    private void searchByCategory(String category) {
        String formattedCategory = capitalizeFirstLetter(category);
        addDisposable(
                repository.filterByCategory(formattedCategory)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                            view.showSearchResults(response.getMeals());
                                        } else {
                                            view.showEmptyResults();
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

    private void searchByCountry(String country) {
        String formattedCountry = capitalizeFirstLetter(country);
        addDisposable(
                repository.filterByArea(formattedCountry)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                            view.showSearchResults(response.getMeals());
                                        } else {
                                            view.showEmptyResults();
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

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private void searchByIngredient(String ingredient) {
        String formattedIngredient = capitalizeFirstLetter(ingredient);
        addDisposable(
                repository.filterByIngredient(formattedIngredient)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                                            view.showSearchResults(response.getMeals());
                                        } else {
                                            view.showEmptyResults();
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
}
