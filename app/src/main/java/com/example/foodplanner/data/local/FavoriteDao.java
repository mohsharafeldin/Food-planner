package com.example.foodplanner.data.local;

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
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertFavorite(FavoriteMeal favorite);

    @Delete
    Completable deleteFavorite(FavoriteMeal favorite);

    @Query("SELECT * FROM favorite_meals WHERE userId = :userId")
    Flowable<List<FavoriteMeal>> getAllFavorites(String userId);

    @Query("SELECT * FROM favorite_meals WHERE idMeal = :mealId AND userId = :userId LIMIT 1")
    Single<FavoriteMeal> getFavoriteById(String mealId, String userId);

    @Query("SELECT COUNT(*) FROM favorite_meals WHERE idMeal = :mealId AND userId = :userId")
    Single<Integer> isFavorite(String mealId, String userId);

    @Query("DELETE FROM favorite_meals WHERE idMeal = :mealId AND userId = :userId")
    Completable deleteFavoriteById(String mealId, String userId);
}
