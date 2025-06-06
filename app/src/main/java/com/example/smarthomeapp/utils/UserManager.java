package com.example.smarthomeapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.smarthomeapp.models.User;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static final String PREFS_NAME = "users_pref";
    private static final String USERS_KEY = "users";
    private static final String CURRENT_USER_KEY = "current_user";

    private SharedPreferences prefs;
    private Gson gson;

    public UserManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<User> getUsers() {
        String json = prefs.getString(USERS_KEY, "");
        if (json.isEmpty()) return new ArrayList<>();
        Type type = new TypeToken<List<User>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveUsers(List<User> users) {
        String json = gson.toJson(users);
        prefs.edit().putString(USERS_KEY, json).apply();
    }

    public void setCurrentUser(User user) {
        prefs.edit().putString(CURRENT_USER_KEY, gson.toJson(user)).apply();
    }

    public User getCurrentUser() {
        String json = prefs.getString(CURRENT_USER_KEY, "");
        if (json.isEmpty()) return null;
        return gson.fromJson(json, User.class);
    }

    public String getDeviceKeyForCurrentUser() {
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            return "devices_" + currentUser.getEmail().replaceAll("[^a-zA-Z0-9]", "_");
        }
        return "devices_default";
    }


}
