package com.example.foodplanner.data.meal.remote;

import com.example.foodplanner.data.meal.model.AreaResponse;
import com.example.foodplanner.data.meal.model.CategoryResponse;
import com.example.foodplanner.data.meal.model.IngredientResponse;
import com.example.foodplanner.data.meal.model.MealResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealApiService {

    // Get random meal for "Meal of the Day"
    @GET("random.php")
    Single<MealResponse> getRandomMeal();

    // Search meal by name
    @GET("search.php")
    Single<MealResponse> searchMealByName(@Query("s") String name);

    // Search meals by first letter
    @GET("search.php")
    Single<MealResponse> searchMealByFirstLetter(@Query("f") String letter);

    // Lookup meal details by ID
    @GET("lookup.php")
    Single<MealResponse> getMealById(@Query("i") String id);

    // List all categories
    @GET("categories.php")
    Single<CategoryResponse> getAllCategories();

    // List all areas/countries
    @GET("list.php?a=list")
    Single<AreaResponse> getAllAreas();

    // List all ingredients
    @GET("list.php?i=list")
    Single<IngredientResponse> getAllIngredients();

    // Filter by category
    @GET("filter.php")
    Single<MealResponse> filterByCategory(@Query("c") String category);

    // Filter by area/country
    @GET("filter.php")
    Single<MealResponse> filterByArea(@Query("a") String area);

    // Filter by main ingredient
    @GET("filter.php")
    Single<MealResponse> filterByIngredient(@Query("i") String ingredient);
}
