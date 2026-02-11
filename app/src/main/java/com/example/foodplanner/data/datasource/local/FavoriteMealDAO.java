package com.example.foodplanner.data.datasource.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.foodplanner.data.model.FavoriteMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface FavoriteMealDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFavorite(FavoriteMeal meal);

    @Delete
    Completable deleteFavorite(FavoriteMeal meal);

    @Query("DELETE FROM favorite_meals WHERE idMeal = :mealId AND userId = :userId")
    Completable deleteFavoriteById(String mealId, String userId);

    @Query("SELECT * FROM favorite_meals WHERE userId = :userId")
    Flowable<List<FavoriteMeal>> getAllFavorites(String userId);

    @Query("SELECT * FROM favorite_meals WHERE idMeal = :mealId AND userId = :userId")
    Single<FavoriteMeal> getFavoriteById(String mealId, String userId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_meals WHERE idMeal = :mealId AND userId = :userId)")
    Single<Boolean> isFavorite(String mealId, String userId);

    @Query("DELETE FROM favorite_meals WHERE userId = :userId")
    Completable deleteAllFavorites(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertAllFavorites(List<FavoriteMeal> meals);

    @Query("UPDATE OR IGNORE favorite_meals SET userId = :newUserId WHERE userId = ''")
    Completable migrateAllFavorites(String newUserId);

    @Query("DELETE FROM favorite_meals WHERE userId = ''")
    Completable deleteYieldedFavorites();
}
