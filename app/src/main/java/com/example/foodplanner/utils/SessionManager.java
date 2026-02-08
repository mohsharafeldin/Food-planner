package com.example.foodplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.rxjava3.core.Observable;

public class SessionManager {
    private static final String PREF_NAME = "FoodPlannerPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_GUEST = "isGuest";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_PLAN_DATE = "planDate";
    private static final String KEY_MEAL_TYPE = "mealType";
    private static final String KEY_HAS_PLAN_SELECTION = "hasPlanSelection";
    private static final String KEY_FIREBASE_UID = "firebaseUid";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Login state methods
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

    // User session management
    public void saveUserSession(long userId, String email, String name) {
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putBoolean(KEY_GUEST, false);
        editor.commit();
    }

    public void saveGuestSession() {
        editor.putBoolean(KEY_GUEST, true);
        editor.putBoolean(KEY_LOGGED_IN, false);
        editor.putLong(KEY_USER_ID, -1);
        editor.putString(KEY_USER_EMAIL, "");
        editor.putString(KEY_USER_NAME, "Guest");
        editor.commit();
    }

    public void logout() {
        editor.clear();
        editor.commit();
    }

    public String getUserId() {
        String firebaseUid = pref.getString(KEY_FIREBASE_UID, null);
        if (firebaseUid != null && !firebaseUid.isEmpty()) {
            return firebaseUid;
        }
        long id = pref.getLong(KEY_USER_ID, -1);
        return id == -1 ? "" : String.valueOf(id);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "Guest");
    }

    // Plan selection methods for meal scheduling
    public void savePlanSelection(String date, String mealType) {
        editor.putString(KEY_PLAN_DATE, date);
        editor.putString(KEY_MEAL_TYPE, mealType);
        editor.putBoolean(KEY_HAS_PLAN_SELECTION, true);
        editor.commit();
    }

    // Firebase session methods
    public void saveFirebaseSession(String firebaseUid, String email, String name) {
        editor.putString(KEY_FIREBASE_UID, firebaseUid);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.putBoolean(KEY_GUEST, false);
        editor.commit();
    }

    public String getFirebaseUid() {
        return pref.getString(KEY_FIREBASE_UID, "");
    }

    public boolean hasPlanSelection() {
        return pref.getBoolean(KEY_HAS_PLAN_SELECTION, false);
    }

    public String getSelectedPlanDate() {
        return pref.getString(KEY_PLAN_DATE, "");
    }

    public String getSelectedMealType() {
        return pref.getString(KEY_MEAL_TYPE, "");
    }

    public void clearPlanSelection() {
        editor.remove(KEY_PLAN_DATE);
        editor.remove(KEY_MEAL_TYPE);
        editor.putBoolean(KEY_HAS_PLAN_SELECTION, false);
        editor.commit();
    }

    // RxJava Observables for preferences
    public Observable<Boolean> getLoggedInObservable() {
        return getBooleanObservable(KEY_LOGGED_IN, false);
    }

    public Observable<Boolean> isGuestObservable() {
        return getBooleanObservable(KEY_GUEST, false);
    }

    private Observable<Boolean> getBooleanObservable(String key, boolean defValue) {
        return Observable.create(emitter -> {
            // Emit initial value
            emitter.onNext(pref.getBoolean(key, defValue));

            SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, changedKey) -> {
                if (key.equals(changedKey)) {
                    emitter.onNext(sharedPreferences.getBoolean(key, defValue));
                }
            };

            pref.registerOnSharedPreferenceChangeListener(listener);

            emitter.setCancellable(() -> pref.unregisterOnSharedPreferenceChangeListener(listener));
        });
    }
}
