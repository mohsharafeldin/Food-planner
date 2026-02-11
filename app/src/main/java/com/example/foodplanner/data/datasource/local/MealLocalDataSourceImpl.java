package com.example.foodplanner.data.datasource.local;

import com.example.foodplanner.data.model.FavoriteMeal;
import com.example.foodplanner.data.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * Implementation of MealLocalDataSource using Room DAOs.
 */
public class MealLocalDataSourceImpl implements MealLocalDataSource {

    private final FavoriteMealDAO favoriteMealDAO;
    private final PlannedMealDAO plannedMealDAO;

    public MealLocalDataSourceImpl(FavoriteMealDAO favoriteMealDAO, PlannedMealDAO plannedMealDAO) {
        this.favoriteMealDAO = favoriteMealDAO;
        this.plannedMealDAO = plannedMealDAO;
    }

    // ============ Favorite Meals ============

    @Override
    public Completable insertFavorite(FavoriteMeal meal) {
        return favoriteMealDAO.insertFavorite(meal);
    }

    @Override
    public Completable deleteFavorite(FavoriteMeal meal) {
        return favoriteMealDAO.deleteFavorite(meal);
    }

    @Override
    public Completable deleteFavoriteById(String mealId, String userId) {
        return favoriteMealDAO.deleteFavoriteById(mealId, userId);
    }

    @Override
    public Flowable<List<FavoriteMeal>> getAllFavorites(String userId) {
        return favoriteMealDAO.getAllFavorites(userId);
    }

    @Override
    public Single<Boolean> isFavorite(String mealId, String userId) {
        return favoriteMealDAO.isFavorite(mealId, userId);
    }

    @Override
    public Completable deleteAllFavorites(String userId) {
        return favoriteMealDAO.deleteAllFavorites(userId);
    }

    @Override
    public Completable insertAllFavorites(List<FavoriteMeal> meals) {
        return favoriteMealDAO.insertAllFavorites(meals);
    }

    @Override
    public Completable migrateAllFavorites(String newUserId) {
        return favoriteMealDAO.migrateAllFavorites(newUserId);
    }

    @Override
    public Completable deleteYieldedFavorites() {
        return favoriteMealDAO.deleteYieldedFavorites();
    }

    // ============ Planned Meals ============

    @Override
    public Completable insertPlannedMeal(PlannedMeal meal) {
        return plannedMealDAO.insertPlannedMeal(meal);
    }

    @Override
    public Completable deletePlannedMeal(PlannedMeal meal) {
        return plannedMealDAO.deletePlannedMeal(meal);
    }

    @Override
    public Flowable<List<PlannedMeal>> getAllPlannedMeals(String userId) {
        return plannedMealDAO.getAllPlannedMeals(userId);
    }

    @Override
    public Flowable<List<PlannedMeal>> getPlannedMealsByDate(String date, String userId) {
        return plannedMealDAO.getPlannedMealsByDate(date, userId);
    }

    @Override
    public Flowable<List<PlannedMeal>> getPlannedMealsForWeek(String startDate, String endDate, String userId) {
        return plannedMealDAO.getPlannedMealsForWeek(startDate, endDate, userId);
    }

    @Override
    public Completable deleteAllPlannedMeals(String userId) {
        return plannedMealDAO.deleteAllPlannedMeals(userId);
    }

    @Override
    public Completable insertAllPlannedMeals(List<PlannedMeal> meals) {
        return plannedMealDAO.insertAllPlannedMeals(meals);
    }

    @Override
    public List<PlannedMeal> getAllPlannedMealsSync(String userId) {
        return plannedMealDAO.getAllPlannedMealsSync(userId);
    }

    @Override
    public Completable migrateAllPlannedMeals(String newUserId) {
        return plannedMealDAO.migrateAllPlannedMeals(newUserId);
    }

    @Override
    public Completable deduplicatePlannedMeals() {
        return plannedMealDAO.deduplicatePlannedMeals();
    }
}
