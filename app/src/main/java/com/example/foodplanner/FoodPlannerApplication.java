package com.example.foodplanner;

import android.app.Application;

import com.example.foodplanner.data.meal.local.AppDatabase;

public class FoodPlannerApplication extends Application {

    private static FoodPlannerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize database
        com.example.foodplanner.data.meal.local.AppDatabase.getInstance(this);
    }

    public static FoodPlannerApplication getInstance() {
        return instance;
    }
}
