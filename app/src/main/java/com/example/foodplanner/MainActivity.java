package com.example.foodplanner;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.foodplanner.repositry.MealRepository;
import com.example.foodplanner.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    private io.reactivex.rxjava3.disposables.CompositeDisposable disposables = new io.reactivex.rxjava3.disposables.CompositeDisposable();
    private com.example.foodplanner.utils.SessionManager sessionManager;
    private MealRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = SessionManager.getInstance();
        repository = MealRepository.getInstance(this);

        setupNavigation();
        checkAndSyncData();
    }

    private void checkAndSyncData() {
        if (sessionManager.isLoggedIn()) {
            String userId = sessionManager.getUserId();
            if (userId != null && !userId.isEmpty()) {
                disposables.add(
                        repository.restoreAllData(userId)
                                .subscribeOn(io.reactivex.rxjava3.schedulers.Schedulers.io())
                                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                                .subscribe(
                                        () -> {
                                            // Sync success - UI will update automatically via Flowables
                                        },
                                        error -> {
                                            // Sync failed - ignore or log
                                            android.util.Log.e("MainActivity", "Auto-sync failed", error);
                                        }));
            }
        }
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // Hide bottom navigation on detail screens
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int destId = destination.getId();
                if (destId == R.id.mealDetailsFragment ||
                        destId == R.id.categoryMealsFragment ||
                        destId == R.id.countryMealsFragment ||
                        destId == R.id.loginFragment ||
                        destId == R.id.signUpFragment) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
