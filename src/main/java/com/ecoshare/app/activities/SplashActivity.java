package com.ecoshare.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.ecoshare.app.R;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.PrefsManager;

public class SplashActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PrefsManager.init(this);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthState, SPLASH_DELAY);
    }

    private void checkAuthState() {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        
        if (firebaseHelper.isUserLoggedIn()) {
            navigateToMain();
        } else {
            navigateToLogin();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
