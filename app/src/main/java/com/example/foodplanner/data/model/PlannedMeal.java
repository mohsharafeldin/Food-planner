package com.example.foodplanner.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "planned_meals")
public class PlannedMeal {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String mealId;
    private String mealName;
    private String mealThumb;
    private String mealCategory;
    private String mealArea;

    @NonNull
    private String date; // Format: yyyy-MM-dd

    private String mealType; // Breakfast, Lunch, Dinner
    private String userId;

    public PlannedMeal() {
    }

    public static PlannedMeal fromMeal(Meal meal, String date, String mealType, String userId) {
        PlannedMeal planned = new PlannedMeal();
        planned.setMealId(meal.getIdMeal());
        planned.setMealName(meal.getStrMeal());
        planned.setMealThumb(meal.getStrMealThumb());
        planned.setMealCategory(meal.getStrCategory());
        planned.setMealArea(meal.getStrArea());
        planned.setDate(date);
        planned.setMealType(mealType);
        planned.setUserId(userId);
        return planned;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getMealId() {
        return mealId;
    }

    public void setMealId(@NonNull String mealId) {
        this.mealId = mealId;
    }

    public String getMealName() {
        return mealName;
    }

    public void setMealName(String mealName) {
        this.mealName = mealName;
    }

    public String getMealThumb() {
        return mealThumb;
    }

    public void setMealThumb(String mealThumb) {
        this.mealThumb = mealThumb;
    }

    public String getMealCategory() {
        return mealCategory;
    }

    public void setMealCategory(String mealCategory) {
        this.mealCategory = mealCategory;
    }

    public String getMealArea() {
        return mealArea;
    }

    public void setMealArea(String mealArea) {
        this.mealArea = mealArea;
    }

    @NonNull
    public String getDate() {
        return date;
    }

    public void setDate(@NonNull String date) {
        this.date = date;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
