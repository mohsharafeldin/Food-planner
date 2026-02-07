package com.example.foodplanner;

import android.app.Application;

import com.example.foodplanner.data.meal.local.MealDatabase;

public class FoodPlannerApplication extends Application {

    private static FoodPlannerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize database
        MealDatabase.getInstance(this);
    }

    public static FoodPlannerApplication getInstance() {
        return instance;
    }
}
