package com.example.testfoodplanner.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.testfoodplanner.data.model.FavoriteMeal;

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

    @Query("DELETE FROM favorite_meals WHERE idMeal = :mealId")
    Completable deleteFavoriteById(String mealId);

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
}
