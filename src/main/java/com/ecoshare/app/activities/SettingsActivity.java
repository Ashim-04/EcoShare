package com.ecoshare.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ecoshare.app.R;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.PrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private MaterialSwitch pushNotificationsSwitch;
    private MaterialSwitch auctionAlertsSwitch;
    private MaterialSwitch badgeNotificationsSwitch;
    private TextView logoutTextView;
    private TextView deleteAccountTextView;

    private PrefsManager prefsManager;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefsManager = PrefsManager.getInstance();
        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupToolbar();
        setupListeners();
        loadSettings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        pushNotificationsSwitch = findViewById(R.id.pushNotificationsSwitch);
        auctionAlertsSwitch = findViewById(R.id.auctionAlertsSwitch);
        badgeNotificationsSwitch = findViewById(R.id.badgeNotificationsSwitch);
        logoutTextView = findViewById(R.id.logoutTextView);
        deleteAccountTextView = findViewById(R.id.deleteAccountTextView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        pushNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.saveBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, isChecked);
            Toast.makeText(this, isChecked ? "Notifications enabled" : "Notifications disabled", Toast.LENGTH_SHORT).show();
        });

        auctionAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.saveBoolean(Constants.PREF_AUCTION_ALERTS, isChecked);
        });

        badgeNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsManager.saveBoolean(Constants.PREF_BADGE_NOTIFICATIONS, isChecked);
        });

        logoutTextView.setOnClickListener(v -> showLogoutDialog());

        deleteAccountTextView.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void loadSettings() {
        pushNotificationsSwitch.setChecked(prefsManager.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true));
        auctionAlertsSwitch.setChecked(prefsManager.getBoolean(Constants.PREF_AUCTION_ALERTS, true));
        badgeNotificationsSwitch.setChecked(prefsManager.getBoolean(Constants.PREF_BADGE_NOTIFICATIONS, true));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void performLogout() {
        firebaseHelper.signOut();
        prefsManager.clearAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_account)
                .setMessage(R.string.confirm_delete_account)
                .setPositiveButton(R.string.delete, (dialog, which) -> performDeleteAccount())
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performDeleteAccount() {
        if (firebaseHelper.getCurrentUser() != null) {
            firebaseHelper.getCurrentUser().delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        performLogout();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
