package com.example.foodplanner;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
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
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
