package com.example.foodplanner.data.meal.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.foodplanner.data.meal.model.FavoriteMeal;
import com.example.foodplanner.data.meal.model.PlannedMeal;
import com.example.foodplanner.data.meal.model.User;

@Database(entities = { FavoriteMeal.class, PlannedMeal.class, User.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "food_planner_db";
    private static volatile AppDatabase instance;

    public abstract FavoriteMealDAO favoriteMealDAO();

    public abstract PlannedMealDAO plannedMealDAO();

    public abstract UserDao userDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
