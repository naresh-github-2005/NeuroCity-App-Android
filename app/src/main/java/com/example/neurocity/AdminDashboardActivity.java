package com.example.neurocity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private static final String TAG = "AdminDashboard";

    private RecyclerView recyclerView;
    private AdminIssuesAdapter adapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<CivicIssue> allIssues = new ArrayList<>();

    private Button btnAll, btnPending, btnInProgress, btnResolved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // --- Firebase Setup ---
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // ✅ DEBUG: Check if user is logged in
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "❌ No user logged in!");
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "✅ User logged in: " + auth.getCurrentUser().getUid());

        // --- Toolbar Setup ---
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle("NeuroCity");
        topAppBar.setSubtitle("Admin Dashboard");
        topAppBar.setNavigationOnClickListener(v -> finish());
        topAppBar.inflateMenu(R.menu.top_app_bar_menu);

        topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_profile) {
                startActivity(new Intent(this, ProfileSettingsActivity.class));
                return true;
            } else if (id == R.id.action_notifications) {
                Toast.makeText(this, "Notifications coming soon!", Toast.LENGTH_SHORT).show();
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

        // --- Initialize Views ---
        recyclerView = findViewById(R.id.recyclerViewIssues);
        btnAll = findViewById(R.id.btnAll);
        btnPending = findViewById(R.id.btnPending);
        btnInProgress = findViewById(R.id.btnInProgress);
        btnResolved = findViewById(R.id.btnResolved);

        // --- RecyclerView Setup ---
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminIssuesAdapter(this, new ArrayList<>(), this::showStatusDialog);
        recyclerView.setAdapter(adapter);

        Log.d(TAG, "RecyclerView and adapter initialized");
        Log.d(TAG, "RecyclerView visibility: " + recyclerView.getVisibility());
        Log.d(TAG, "RecyclerView height: " + recyclerView.getHeight());

        // --- Load Issues ---
        loadIssues();

        // --- Filter Buttons ---
        btnAll.setOnClickListener(v -> {
            List<CivicIssue> displayList = new ArrayList<>(allIssues);
            adapter.updateList(displayList);
            Log.d(TAG, "Filter: ALL - Showing " + allIssues.size() + " issues");
            Toast.makeText(this, "Showing all " + allIssues.size() + " issues", Toast.LENGTH_SHORT).show();
        });
        btnPending.setOnClickListener(v -> filterByStatus("Pending"));
        btnInProgress.setOnClickListener(v -> filterByStatus("In Progress"));
        btnResolved.setOnClickListener(v -> filterByStatus("Resolved"));

        checkAndFixOldIssues();
    }

    // --- Load Issues from Firestore ---
    private void loadIssues() {
        Log.d(TAG, "🔄 Starting to load issues...");

        firestore.collection("civic_issues")
                .addSnapshotListener((value, error) -> {
                    // ✅ Check for errors
                    if (error != null) {
                        Log.e(TAG, "❌ Error loading issues: " + error.getMessage(), error);
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ Check if data is null
                    if (value == null) {
                        Log.w(TAG, "⚠️ QuerySnapshot is null");
                        Toast.makeText(this, "No data received from Firestore", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "📦 Received " + value.size() + " documents from Firestore");

                    allIssues.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        Log.d(TAG, "📄 Processing document: " + doc.getId());
                        Log.d(TAG, "   Data: " + doc.getData());

                        CivicIssue issue = new CivicIssue();

                        issue.setImage_url(doc.getString("image_url"));
                        issue.setIssue_type(doc.getString("issue_type"));
                        issue.setLatitude(doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0.0);
                        issue.setLongitude(doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0.0);
                        issue.setUser_id(doc.getString("user_id"));
                        issue.setDescription(doc.getString("description"));
                        issue.setStatus(doc.getString("status"));

                        // ✅ New Fields
                        issue.setDepartment(doc.getString("department"));
                        issue.setAssigned_worker_id(doc.getString("assigned_worker_id"));
                        issue.setAssigned_worker_name(doc.getString("assigned_worker_name"));

                        // --- Handle Timestamp ---
                        Object tsObj = doc.get("timestamp");
                        if (tsObj instanceof Timestamp) {
                            Timestamp ts = (Timestamp) tsObj;
                            issue.setTimestamp(String.valueOf(ts.toDate().getTime()));
                        } else if (tsObj != null) {
                            issue.setTimestamp(tsObj.toString());
                        } else {
                            issue.setTimestamp("");
                        }

                        issue.setDocId(doc.getId());
                        allIssues.add(issue);

                        Log.d(TAG, "✅ Added issue: " + issue.getIssue_type() + " - Status: " + issue.getStatus());
                    }

                    Log.d(TAG, "🎯 Total issues loaded: " + allIssues.size());

                    // Create a new list to pass to adapter
                    List<CivicIssue> displayList = new ArrayList<>(allIssues);
                    adapter.updateList(displayList);

                    if (allIssues.isEmpty()) {
                        Toast.makeText(this, "No issues found in database", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "✅ Loaded " + allIssues.size() + " issues", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // --- Filter by Status ---
    private void filterByStatus(String status) {
        List<CivicIssue> filtered = new ArrayList<>();
        for (CivicIssue issue : allIssues) {
            if (issue.getStatus() != null && issue.getStatus().equals(status)) {
                filtered.add(issue);
            }
        }
        adapter.updateList(filtered);
        Log.d(TAG, "Filter: " + status + " - Found " + filtered.size() + " issues");
        Toast.makeText(this, filtered.size() + " " + status + " issues", Toast.LENGTH_SHORT).show();
    }

    // --- Update Issue Status ---
    private void showStatusDialog(CivicIssue issue) {
        String[] options = {"Pending", "In Progress", "Resolved"};

        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setItems(options, (dialog, which) -> {
                    String newStatus = options[which];
                    if (issue.getDocId() != null) {
                        firestore.collection("civic_issues")
                                .document(issue.getDocId())
                                .update(
                                        "status", newStatus,
                                        "status_updated_by", auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown",
                                        "status_updated_at", Timestamp.now()
                                )
                                .addOnSuccessListener(aVoid -> {
                                    issue.setStatus(newStatus);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Status updated ✅", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Status updated to: " + newStatus);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update status ❌", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to update status", e);
                                });
                    }
                })
                .show();
    }

    // --- Help Dialog ---
    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help & Support")
                .setMessage("Need help?\n\n📧 Email: support@neurocity.com\n📞 +91 1234567890\n\nFor technical issues, please contact our support team.")
                .setPositiveButton("OK", null)
                .setNegativeButton("Contact Us", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:support@neurocity.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "NeuroCity Support Request");
                    startActivity(Intent.createChooser(intent, "Send Email"));
                })
                .show();
    }

    // --- About Dialog ---
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

    // --- Logout Dialog ---
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

    private void checkAndFixOldIssues() {
        firestore.collection("civic_issues")
                .whereEqualTo("department", null)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Log.d(TAG, "Found old issues without department");
                        fixOldIssuesWithoutDepartment();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking old issues", e);
                });
    }

    private void fixOldIssuesWithoutDepartment() {
        new AlertDialog.Builder(this)
                .setTitle("Update Old Issues")
                .setMessage("Some old issues don't have departments assigned. Update them now?\n\n(Run this only once)")
                .setPositiveButton("Update Now", (dialog, which) -> {
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Updating issues...");
                    progressDialog.show();

                    firestore.collection("civic_issues")
                            .whereEqualTo("department", null)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (querySnapshot.isEmpty()) {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "✅ All issues already have departments!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int totalIssues = querySnapshot.size();
                                final int[] updatedCount = {0};

                                for (QueryDocumentSnapshot doc : querySnapshot) {
                                    String issueType = doc.getString("issue_type");
                                    String department = getDepartmentFromIssueType(issueType);

                                    firestore.collection("civic_issues")
                                            .document(doc.getId())
                                            .update("department", department)
                                            .addOnSuccessListener(aVoid -> {
                                                updatedCount[0]++;
                                                Log.d(TAG, "Updated issue " + updatedCount[0] + "/" + totalIssues);
                                                if (updatedCount[0] == totalIssues) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(this,
                                                            "✅ Updated " + totalIssues + " issues successfully!",
                                                            Toast.LENGTH_LONG).show();
                                                    loadIssues();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e(TAG, "Failed to update issue", e);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "❌ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error updating issues", e);
                            });
                })
                .setNegativeButton("Later", null)
                .show();
    }

    private String getDepartmentFromIssueType(String issueType) {
        if (issueType == null) return "General";

        switch (issueType) {
            case "Pothole":
                return "Roads & Transport";
            case "Water Logging":
                return "Water Supply";
            case "Garbage Disposal":
                return "Sanitation";
            case "Streetlight Failure":
                return "Electricity";
            default:
                return "General";
        }
    }
}