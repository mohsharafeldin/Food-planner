package com.example.foodplanner.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodplanner.MainActivity;
import com.example.foodplanner.R;
import com.example.foodplanner.auth.view.AuthActivity;
import com.example.foodplanner.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000; // 3 seconds
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextScreen, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        Intent intent;

        if (sessionManager.isLoggedIn() || sessionManager.isGuest()) {
            // User is already logged in or is a guest, go to main
            intent = new Intent(this, MainActivity.class);
        } else {
            // User needs to authenticate
            intent = new Intent(this, AuthActivity.class);
        }

        startActivity(intent);
        finish();

        // Add smooth transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
