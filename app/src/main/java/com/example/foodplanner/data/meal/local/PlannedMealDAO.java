package com.example.foodplanner.data.meal.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.foodplanner.data.meal.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface PlannedMealDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertPlannedMeal(PlannedMeal meal);

    @Delete
    Completable deletePlannedMeal(PlannedMeal meal);

    @Query("DELETE FROM planned_meals WHERE id = :id")
    Completable deletePlannedMealById(int id);

    @Query("SELECT * FROM planned_meals WHERE userId = :userId ORDER BY date ASC")
    Flowable<List<PlannedMeal>> getAllPlannedMeals(String userId);

    @Query("SELECT * FROM planned_meals WHERE date = :date AND userId = :userId")
    Flowable<List<PlannedMeal>> getPlannedMealsByDate(String date, String userId);

    @Query("SELECT * FROM planned_meals WHERE date BETWEEN :startDate AND :endDate AND userId = :userId ORDER BY date ASC")
    Flowable<List<PlannedMeal>> getPlannedMealsForWeek(String startDate, String endDate, String userId);

    @Query("DELETE FROM planned_meals WHERE userId = :userId")
    Completable deleteAllPlannedMeals(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllPlannedMeals(List<PlannedMeal> meals);

    @Query("SELECT * FROM planned_meals WHERE userId = :userId")
    List<PlannedMeal> getAllPlannedMealsSync(String userId);

    @Query("UPDATE planned_meals SET userId = :newUserId")
    Completable migrateAllPlannedMeals(String newUserId);

    // Keep only one entry per meal-slot-date-user combination
    @Query("DELETE FROM planned_meals WHERE id NOT IN (SELECT MIN(id) FROM planned_meals GROUP BY mealId, date, mealType, userId)")
    Completable deduplicatePlannedMeals();
}
