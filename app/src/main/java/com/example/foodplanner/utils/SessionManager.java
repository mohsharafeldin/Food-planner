package com.example.foodplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.rxjava3.core.Observable;

public class SessionManager {
    private static final String PREF_NAME = "FoodPlannerPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_GUEST = "isGuest";
    private static final String KEY_PLAN_DATE = "planDate";
    private static final String KEY_MEAL_TYPE = "mealType";
    private static final String KEY_HAS_PLAN_SELECTION = "hasPlanSelection";

    // Singleton instance
    private static volatile SessionManager instance;

    // Kept for backward compatibility with Observables, though we use Firebase Auth
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private final com.google.firebase.auth.FirebaseAuth auth;

    /**
     * Initialize the SessionManager singleton. Must be called once in
     * Application.onCreate().
     */
    public static void init(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context.getApplicationContext());
                }
            }
        }
    }

    /**
     * Get the SessionManager singleton instance.
     * 
     * @throws IllegalStateException if init() has not been called
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "SessionManager not initialized. Call SessionManager.init(context) in Application.onCreate()");
        }
        return instance;
    }

    private SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        auth = com.google.firebase.auth.FirebaseAuth.getInstance();
    }

    // Login state methods
    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void setGuest(boolean isGuest) {
        editor.putBoolean(KEY_GUEST, isGuest);
        editor.commit();
    }

    public boolean isGuest() {
        return pref.getBoolean(KEY_GUEST, false);
    }

    // User session management
    // Deprecated: No longer needed as Firebase handles session
    public void saveUserSession(long userId, String email, String name) {
        // No-op or migration logic if needed
    }

    public void saveGuestSession() {
        editor.putBoolean(KEY_GUEST, true);
        editor.commit();
    }

    public void logout() {
        auth.signOut();
        editor.clear();
        editor.commit();
    }

    public String getUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        // Return empty string for guest (consistent with recent bug fixes where guest
        // userId = "")
        return "";
    }

    public String getUserEmail() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getEmail();
        }
        return "";
    }

    public String getUserName() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getDisplayName();
        }
        return "Guest";
    }

    // Plan selection methods for meal scheduling
    public void savePlanSelection(String date, String mealType) {
        editor.putString(KEY_PLAN_DATE, date);
        editor.putString(KEY_MEAL_TYPE, mealType);
        editor.putBoolean(KEY_HAS_PLAN_SELECTION, true);
        editor.commit();
    }

    // Firebase session methods - Deprecated
    public void saveFirebaseSession(String firebaseUid, String email, String name) {
        // We still update the pref to trigger the Observable listeners in MainActivity
        editor.putBoolean(KEY_LOGGED_IN, true);
        editor.commit();
    }

    public String getFirebaseUid() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return "";
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
