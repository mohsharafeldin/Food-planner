package com.example.testfoodplanner.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.testfoodplanner.data.model.FavoriteMeal;
import com.example.testfoodplanner.data.model.PlannedMeal;

@Database(entities = { FavoriteMeal.class, PlannedMeal.class }, version = 1, exportSchema = false)
public abstract class MealDatabase extends RoomDatabase {

    private static volatile MealDatabase INSTANCE;
    private static final String DATABASE_NAME = "food_planner_db";

    public abstract FavoriteDao favoriteDao();

    public abstract PlannedMealDAO plannedMealDao();

    public static MealDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MealDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MealDatabase.class,
                            DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
