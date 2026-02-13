package com.ecoshare.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {
    private static final String PREFS_NAME = "EcoSharePrefs";
    private static PrefsManager instance;
    
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    private PrefsManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new PrefsManager(context);
        }
    }

    public static PrefsManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PrefsManager must be initialized with init(Context) before use");
        }
        return instance;
    }

    public void saveUserId(String userId) {
        editor.putString(Constants.PREF_USER_ID, userId).apply();
    }

    public String getUserId() {
        return preferences.getString(Constants.PREF_USER_ID, null);
    }

    public void saveUserEmail(String email) {
        editor.putString(Constants.PREF_USER_EMAIL, email).apply();
    }

    public String getUserEmail() {
        return preferences.getString(Constants.PREF_USER_EMAIL, null);
    }

    public void saveUserName(String name) {
        editor.putString(Constants.PREF_USER_NAME, name).apply();
    }

    public String getUserName() {
        return preferences.getString(Constants.PREF_USER_NAME, null);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, isLoggedIn).apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public void saveFcmToken(String token) {
        editor.putString(Constants.PREF_FCM_TOKEN, token).apply();
    }

    public String getFcmToken() {
        return preferences.getString(Constants.PREF_FCM_TOKEN, null);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true);
    }

    public void setAuctionAlertsEnabled(boolean enabled) {
        editor.putBoolean(Constants.PREF_AUCTION_ALERTS, enabled).apply();
    }

    public boolean areAuctionAlertsEnabled() {
        return preferences.getBoolean(Constants.PREF_AUCTION_ALERTS, true);
    }

    public void setBadgeNotificationsEnabled(boolean enabled) {
        editor.putBoolean(Constants.PREF_BADGE_NOTIFICATIONS, enabled).apply();
    }

    public boolean areBadgeNotificationsEnabled() {
        return preferences.getBoolean(Constants.PREF_BADGE_NOTIFICATIONS, true);
    }

    public void saveString(String key, String value) {
        editor.putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }

    public void saveInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }

    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        editor.putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return preferences.getLong(key, defaultValue);
    }

    public void clearUserData() {
        editor.remove(Constants.PREF_USER_ID);
        editor.remove(Constants.PREF_USER_EMAIL);
        editor.remove(Constants.PREF_USER_NAME);
        editor.remove(Constants.PREF_IS_LOGGED_IN);
        editor.remove(Constants.PREF_FCM_TOKEN);
        editor.apply();
    }

    public void clearAll() {
        editor.clear().apply();
    }
}
