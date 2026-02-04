package com.example.foodplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "FoodPlannerPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_GUEST = "isGuest";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_LOGGED_IN, isLoggedIn);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setGuest(boolean isGuest) {
        editor.putBoolean(KEY_GUEST, isGuest);
        editor.commit();
    }

    public boolean isGuest() {
        return pref.getBoolean(KEY_GUEST, false);
    }
}
