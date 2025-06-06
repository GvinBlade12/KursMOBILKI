package com.example.smarthomeapp.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.smarthomeapp.R;
import com.example.smarthomeapp.fragments.DevicesFragment;
import com.example.smarthomeapp.fragments.ProfileFragments;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_DARK_THEME = "dark_theme";

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        applyTheme(); // Применяем тему ДО super.onCreate и setContentView

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализируем Toolbar после setContentView
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // По умолчанию показываем экран устройств
        loadFragment(new DevicesFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.nav_devices) {
                fragment = new DevicesFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                fragment = new ProfileFragments();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    // Меню с кнопкой переключения темы
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.d("MainActivity", "Меню создано, элементов: " + menu.size());
        return true;
    }

    // Обработка нажатия на кнопку переключения темы
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Метод для применения темы (вызываем в onCreate до super.onCreate)
    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkTheme = prefs.getBoolean(KEY_DARK_THEME, false);
        if (darkTheme) {
            setTheme(R.style.AppTheme_Dark2);
        } else {
            setTheme(R.style.AppTheme_Light2);
        }
    }

    // Метод переключения темы и сохранения выбора
    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean darkTheme = prefs.getBoolean(KEY_DARK_THEME, false);
        prefs.edit().putBoolean(KEY_DARK_THEME, !darkTheme).apply();

        recreate(); // Перезапускаем активити для применения темы
    }
}
