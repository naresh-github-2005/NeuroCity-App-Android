package com.example.neurocity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "NeuroCity_Prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_THEME_MODE = "theme_mode"; // "default", "light", "dark"

    private SharedPreferences prefs;
    private MaterialSwitch switchNotifications;
    private RadioGroup radioGroupTheme;
    private MaterialCardView cardProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences first
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        applySavedThemeMode();

        setContentView(R.layout.activity_settings);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // Initialize views
        switchNotifications = findViewById(R.id.switch_notifications);
        radioGroupTheme = findViewById(R.id.radio_group_theme);
        cardProfile = findViewById(R.id.card_profile);

        // Load saved settings
        loadSavedSettings();

        // Notification toggle
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) enableNotifications();
            else disableNotifications();
            saveBoolean(KEY_NOTIFICATIONS, isChecked);
        });

        // Theme mode radio selection
        radioGroupTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String mode = "default";

            if (checkedId == R.id.radio_dark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mode = "dark";
                Toast.makeText(this, "Dark Mode Enabled", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radio_light) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                mode = "light";
                Toast.makeText(this, "Light Mode Enabled", Toast.LENGTH_SHORT).show();
            } else if (checkedId == R.id.radio_default) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                mode = "default";
                Toast.makeText(this, "Default Mode (System)", Toast.LENGTH_SHORT).show();
            }

            saveString(KEY_THEME_MODE, mode);
        });

        // Profile settings click
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileSettingsActivity.class);
            startActivity(intent);
        });
    }

    private void loadSavedSettings() {
        boolean notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);
        switchNotifications.setChecked(notificationsEnabled);

        String themeMode = prefs.getString(KEY_THEME_MODE, "default");
        if (themeMode.equals("dark")) {
            radioGroupTheme.check(R.id.radio_dark);
        } else if (themeMode.equals("light")) {
            radioGroupTheme.check(R.id.radio_light);
        } else {
            radioGroupTheme.check(R.id.radio_default);
        }
    }

    private void applySavedThemeMode() {
        String themeMode = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_THEME_MODE, "default");

        switch (themeMode) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    private void enableNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("all_users")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "✅ Notifications Enabled", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "❌ Failed to enable notifications", Toast.LENGTH_SHORT).show());
    }

    private void disableNotifications() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic("all_users")
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "🔕 Notifications Disabled", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "❌ Failed to disable notifications", Toast.LENGTH_SHORT).show());
    }

    private void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    private void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }
}
