package com.example.foodplanner;

import android.app.Application;

import com.example.foodplanner.data.db.AppDatabase;
import com.example.foodplanner.utils.SessionManager;

public class FoodPlannerApplication extends Application {

    private static FoodPlannerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize database
        AppDatabase.getInstance(this);

        // Initialize SessionManager singleton
        SessionManager.init(this);
    }

    public static FoodPlannerApplication getInstance() {
        return instance;
    }
}
