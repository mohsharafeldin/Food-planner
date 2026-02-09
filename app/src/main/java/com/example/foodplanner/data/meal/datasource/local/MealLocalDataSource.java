package com.example.foodplanner.data.meal.datasource.local;

import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Local data source interface for meal persistence operations.
 * Abstracts Room database access for dependency inversion (SOLID - D).
 */
public interface MealLocalDataSource {

    // ============ Favorite Meals ============

    Completable insertFavorite(FavoriteMeal meal);

    Completable deleteFavorite(FavoriteMeal meal);

    Completable deleteFavoriteById(String mealId, String userId);

    Flowable<List<FavoriteMeal>> getAllFavorites(String userId);

    Single<Boolean> isFavorite(String mealId, String userId);

    Completable deleteAllFavorites(String userId);

    Completable insertAllFavorites(List<FavoriteMeal> meals);

    Completable migrateAllFavorites(String newUserId);

    Completable deleteYieldedFavorites();

    // ============ Planned Meals ============

    Completable insertPlannedMeal(PlannedMeal meal);

    Completable deletePlannedMeal(PlannedMeal meal);

    Flowable<List<PlannedMeal>> getAllPlannedMeals(String userId);

    Flowable<List<PlannedMeal>> getPlannedMealsByDate(String date, String userId);

    Flowable<List<PlannedMeal>> getPlannedMealsForWeek(String startDate, String endDate, String userId);

    Completable deleteAllPlannedMeals(String userId);

    Completable insertAllPlannedMeals(List<PlannedMeal> meals);

    List<PlannedMeal> getAllPlannedMealsSync(String userId);

    Completable migrateAllPlannedMeals(String newUserId);

    Completable deduplicatePlannedMeals();
}
