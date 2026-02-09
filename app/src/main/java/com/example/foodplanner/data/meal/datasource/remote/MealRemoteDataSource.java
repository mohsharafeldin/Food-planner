package com.example.foodplanner.data.meal.datasource.remote;

import com.example.foodplanner.data.meal.model.AreaResponse;
import com.example.foodplanner.data.meal.model.CategoryResponse;
import com.example.foodplanner.data.meal.model.IngredientResponse;
import com.example.foodplanner.data.meal.model.MealResponse;

import io.reactivex.rxjava3.core.Single;

/**
 * Remote data source interface for meal API operations.
 * Abstracts network data access for dependency inversion (SOLID - D).
 */
public interface MealRemoteDataSource {

    Single<MealResponse> getRandomMeal();

    Single<MealResponse> searchMealByName(String name);

    Single<MealResponse> getMealById(String id);

    Single<CategoryResponse> getAllCategories();

    Single<AreaResponse> getAllAreas();

    Single<IngredientResponse> getAllIngredients();

    Single<MealResponse> filterByCategory(String category);

    Single<MealResponse> filterByArea(String area);

    Single<MealResponse> filterByIngredient(String ingredient);
}
