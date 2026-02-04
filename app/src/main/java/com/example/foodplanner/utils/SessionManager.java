package com.example.foodplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUserSession(String userId, String email, String name) {
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_EMAIL, email);
        editor.putString(Constants.KEY_USER_NAME, name);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.KEY_IS_GUEST, false);
        editor.apply();
    }

    public void saveGuestSession() {
        editor.putBoolean(Constants.KEY_IS_GUEST, true);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public boolean isGuest() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_GUEST, false);
    }

    public String getUserId() {
        return sharedPreferences.getString(Constants.KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.KEY_USER_NAME, null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    // ============ Plan Selection Methods ============

    private static final String KEY_SELECTED_PLAN_DATE = "selected_plan_date";
    private static final String KEY_SELECTED_MEAL_TYPE = "selected_meal_type";

    public void setSelectedPlanDate(String date) {
        editor.putString(KEY_SELECTED_PLAN_DATE, date);
        editor.apply();
    }

    public String getSelectedPlanDate() {
        return sharedPreferences.getString(KEY_SELECTED_PLAN_DATE, null);
    }

    public void setSelectedMealType(String mealType) {
        editor.putString(KEY_SELECTED_MEAL_TYPE, mealType);
        editor.apply();
    }

    public String getSelectedMealType() {
        return sharedPreferences.getString(KEY_SELECTED_MEAL_TYPE, null);
    }

    public void clearPlanSelection() {
        editor.remove(KEY_SELECTED_PLAN_DATE);
        editor.remove(KEY_SELECTED_MEAL_TYPE);
        editor.apply();
    }

    public boolean hasPlanSelection() {
        return getSelectedPlanDate() != null && getSelectedMealType() != null;
    }

    // Additional setter methods for simple login
    public void setGuest(boolean isGuest) {
        editor.putBoolean(Constants.KEY_IS_GUEST, isGuest);
        editor.apply();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
}
