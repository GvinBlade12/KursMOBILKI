package com.example.smarthomeapp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesHelper {
    private static final String PREF_NAME = "SmartHomePrefs";
    private static final String USERS = "users";
    private static final String PASSWORD_PREFIX = "password_";
    private static final String CURRENT_USER = "current_user";

    public static void saveUser(Context context, String email, String password) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> users = prefs.getStringSet(USERS, new HashSet<>());
        users.add(email);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(USERS, users);
        editor.putString(PASSWORD_PREFIX + email, password);
        editor.apply();
    }

    public static void setCurrentUser(Context context, String email) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(CURRENT_USER, email);
        editor.apply();
    }

    public static String getCurrentUser(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(CURRENT_USER, null);
    }
}