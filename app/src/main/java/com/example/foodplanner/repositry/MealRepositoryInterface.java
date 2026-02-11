package com.example.foodplanner.repositry;

import com.example.foodplanner.data.model.AreaResponse;
import com.example.foodplanner.data.model.CategoryResponse;
import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.data.model.IngredientResponse;
import com.example.foodplanner.data.model.Meal;
import com.example.foodplanner.data.model.MealResponse;
import com.example.foodplanner.data.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Repository interface for meal operations.
 * Abstracts data access for dependency inversion (SOLID - D).
 */
public interface MealRepositoryInterface {

    // ============ Remote API Methods ============

    Single<MealResponse> getRandomMeal();

    Single<MealResponse> searchMealByName(String name);

    Single<MealResponse> getMealById(String id);

    Single<CategoryResponse> getAllCategories();

    Single<AreaResponse> getAllAreas();

    Single<IngredientResponse> getAllIngredients();

    Single<MealResponse> filterByCategory(String category);

    Single<MealResponse> filterByArea(String area);

    Single<MealResponse> filterByIngredient(String ingredient);

    Single<MealResponse> searchMealsByName(String name);

    // ============ Favorite Meals Methods ============

    Completable addFavorite(Meal meal, String userId);

    Completable removeFavorite(FavoriteMeal meal);

    Completable removeFavoriteById(String mealId, String userId);

    Flowable<List<FavoriteMeal>> getAllFavorites(String userId);

    Single<Boolean> isFavorite(String mealId, String userId);

    // ============ Planned Meals Methods ============

    Completable addPlannedMeal(Meal meal, String date, String mealType, String userId);

    Completable removePlannedMeal(PlannedMeal meal);

    Flowable<List<PlannedMeal>> getAllPlannedMeals(String userId);

    Flowable<List<PlannedMeal>> getPlannedMealsByDate(String date, String userId);

    Flowable<List<PlannedMeal>> getPlannedMealsForWeek(String startDate, String endDate, String userId);

    Flowable<List<PlannedMeal>> getPlannedMealsByDateRange(String startDate, String endDate, String userId);

    // ============ Backup/Sync Methods ============

    Completable deleteAllFavorites(String userId);

    Completable insertAllFavorites(List<FavoriteMeal> meals);

    Completable deleteAllPlannedMeals(String userId);

    Completable insertAllPlannedMeals(List<PlannedMeal> meals);

    List<PlannedMeal> getAllPlannedMealsSync(String userId);

    Completable insertPlannedMealDirectly(PlannedMeal meal);

    Completable addFavoriteDirectly(FavoriteMeal meal);

    Completable migrateAllData(String newUserId);

    Completable restoreAllData(String userId);

    /**
     * Clear all local data for a user (favorites and planned meals).
     * Used on logout to prevent data leakage.
     */
    Completable clearUserData(String userId);
}
