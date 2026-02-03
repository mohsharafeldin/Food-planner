package com.example.testfoodplanner.utils;

public class Constants {
    // API Base URL
    public static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";

    // Ingredient Image URL
    public static final String INGREDIENT_IMAGE_URL = "https://www.themealdb.com/images/ingredients/";

    // SharedPreferences
    public static final String PREFS_NAME = "FoodPlannerPrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_IS_GUEST = "is_guest";

    // Intent Keys
    public static final String KEY_MEAL_ID = "meal_id";
    public static final String KEY_CATEGORY_NAME = "category_name";
    public static final String KEY_COUNTRY_NAME = "country_name";
    public static final String KEY_INGREDIENT_NAME = "ingredient_name";

    // Firebase Collections
    public static final String COLLECTION_FAVORITES = "favorites";
    public static final String COLLECTION_PLANNED_MEALS = "planned_meals";

    // Meal Types
    public static final String MEAL_TYPE_BREAKFAST = "Breakfast";
    public static final String MEAL_TYPE_LUNCH = "Lunch";
    public static final String MEAL_TYPE_DINNER = "Dinner";

    // Search Types
    public static final String SEARCH_BY_NAME = "name";
    public static final String SEARCH_BY_CATEGORY = "category";
    public static final String SEARCH_BY_COUNTRY = "country";
    public static final String SEARCH_BY_INGREDIENT = "ingredient";
}
