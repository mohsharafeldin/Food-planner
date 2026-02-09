package com.example.foodplanner.data.meal.datasource.remote;

import com.example.foodplanner.data.meal.model.AreaResponse;
import com.example.foodplanner.data.meal.model.CategoryResponse;
import com.example.foodplanner.data.meal.model.IngredientResponse;
import com.example.foodplanner.data.meal.model.MealResponse;

import io.reactivex.rxjava3.core.Single;

/**
 * Implementation of MealRemoteDataSource using Retrofit API service.
 */
public class MealRemoteDataSourceImpl implements MealRemoteDataSource {

    private final MealApiService apiService;

    public MealRemoteDataSourceImpl(MealApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public Single<MealResponse> getRandomMeal() {
        return apiService.getRandomMeal();
    }

    @Override
    public Single<MealResponse> searchMealByName(String name) {
        return apiService.searchMealByName(name);
    }

    @Override
    public Single<MealResponse> getMealById(String id) {
        return apiService.getMealById(id);
    }

    @Override
    public Single<CategoryResponse> getAllCategories() {
        return apiService.getAllCategories();
    }

    @Override
    public Single<AreaResponse> getAllAreas() {
        return apiService.getAllAreas();
    }

    @Override
    public Single<IngredientResponse> getAllIngredients() {
        return apiService.getAllIngredients();
    }

    @Override
    public Single<MealResponse> filterByCategory(String category) {
        return apiService.filterByCategory(category);
    }

    @Override
    public Single<MealResponse> filterByArea(String area) {
        return apiService.filterByArea(area);
    }

    @Override
    public Single<MealResponse> filterByIngredient(String ingredient) {
        return apiService.filterByIngredient(ingredient);
    }
}
