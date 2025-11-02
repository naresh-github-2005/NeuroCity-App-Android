package com.example.neurocity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // --- Toolbar Setup ---
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle("NeuroCity");
        topAppBar.setNavigationOnClickListener(v -> finish());

        // ✅ Handle Menu Clicks Here
        topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_profile) {
                startActivity(new Intent(this, ProfileSettingsActivity.class));
                return true;

            } else if (id == R.id.action_notifications) {
                Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(this, NotificationsActivity.class));
                return true;

            } else if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            } else if (id == R.id.action_help) {
                showHelpDialog();
                return true;

            } else if (id == R.id.action_about) {
                showAboutDialog();
                return true;

            } else if (id == R.id.action_logout) {
                showLogoutDialog();
                return true;
            }

            return false;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_upload) {
                selectedFragment = new UploadFragment();
            } else if (id == R.id.nav_map) {
                selectedFragment = new MapFragment();
            } else if (id == R.id.nav_my_complaints) {
                selectedFragment = new ComplaintsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new UploadFragment())
                    .commit();
        }
    }


    // --- Helper Dialogs ---
    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help & Support")
                .setMessage("Need help?\n\n📧 Email: support@neurocity.com\n📞 Phone: +91 1234567890\n\nFor technical issues, please contact our support team.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Contact Us", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:support@neurocity.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "NeuroCity Support Request");
                    startActivity(Intent.createChooser(intent, "Send Email"));
                })
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About NeuroCity")
                .setMessage("NeuroCity v1.0\n\nA civic engagement platform for reporting and tracking community issues.\n\n👨‍💻 Developed by:\nArfath, Naresh, and Yusuf\n\n© 2024 NeuroCity. All rights reserved.")
                .setPositiveButton("OK", null)
                .setNeutralButton("Rate App", (dialog, which) -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + getPackageName())));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                })
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes, Logout", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
