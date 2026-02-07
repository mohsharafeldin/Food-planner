package com.example.foodplanner.search.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.data.meal.repository.MealRepository;
import com.example.foodplanner.search.view.SearchView;
import com.example.foodplanner.utils.Constants;

import com.example.foodplanner.data.meal.model.Area;
import com.example.foodplanner.data.meal.model.Category;
import com.example.foodplanner.data.meal.model.Ingredient;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

public class SearchPresenter extends BasePresenter<SearchView> {

    private final MealRepository repository;
    private Disposable searchDisposable;

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

        // Dispose previous search if active
        if (searchDisposable != null && !searchDisposable.isDisposed()) {
            searchDisposable.dispose();
        }

        switch (searchType) {
            case Constants.SEARCH_BY_NAME:
                searchDisposable = searchByName(query);
                break;
            case Constants.SEARCH_BY_CATEGORY:
                searchDisposable = searchByCategory(query);
                break;
            case Constants.SEARCH_BY_COUNTRY:
                searchDisposable = searchByCountry(query);
                break;
            case Constants.SEARCH_BY_INGREDIENT:
                searchDisposable = searchByIngredient(query);
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

    private Disposable searchByName(String query) {
        String formattedQuery = capitalizeFirstLetter(query);
        return repository.searchMealsByName(formattedQuery)
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
                        });
    }

    private Disposable searchByCategory(String category) {
        String formattedCategory = capitalizeFirstLetter(category);
        return repository.filterByCategory(formattedCategory)
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
                        });
    }

    private Disposable searchByCountry(String country) {
        String formattedCountry = capitalizeFirstLetter(country);
        return repository.filterByArea(formattedCountry)
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
                        });
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private Disposable searchByIngredient(String ingredient) {
        String formattedIngredient = capitalizeFirstLetter(ingredient);
        return repository.filterByIngredient(formattedIngredient)
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
                        });
    }

    @Override
    public void detachView() {
        super.detachView();
        if (searchDisposable != null && !searchDisposable.isDisposed()) {
            searchDisposable.dispose();
        }
    }
}
