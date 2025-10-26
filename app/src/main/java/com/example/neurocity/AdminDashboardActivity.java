package com.example.neurocity;

import android.content.Intent;
import android.os.Bundle;
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

        // --- Firebase Auth & Firestore ---
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // --- Toolbar Setup ---
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle("NeuroCity Admin");
        topAppBar.setTitleTextColor(getResources().getColor(android.R.color.white, null));

        // Back arrow (optional)
        topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        topAppBar.setNavigationOnClickListener(v -> finish());

        // Handle menu clicks (Settings / Logout)
        topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_settings) {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            } else if (id == R.id.action_logout) {
                auth.signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
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
        adapter = new AdminIssuesAdapter(this, allIssues, this::showStatusDialog);
        recyclerView.setAdapter(adapter);

        // --- Load Issues ---
        loadIssues();

        // --- Filter Buttons ---
        btnAll.setOnClickListener(v -> adapter.updateList(allIssues));
        btnPending.setOnClickListener(v -> filterByStatus("Pending"));
        btnInProgress.setOnClickListener(v -> filterByStatus("In Progress"));
        btnResolved.setOnClickListener(v -> filterByStatus("Resolved"));
    }

    // --- Firestore Data Loading ---
    private void loadIssues() {
        firestore.collection("civic_issues")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    allIssues.clear();

                    for (QueryDocumentSnapshot doc : value) {
                        CivicIssue issue = new CivicIssue();

                        issue.setImage_url(doc.getString("image_url"));
                        issue.setIssue_type(doc.getString("issue_type"));
                        issue.setLatitude(doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0.0);
                        issue.setLongitude(doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0.0);
                        issue.setUser_id(doc.getString("user_id"));
                        issue.setDescription(doc.getString("description"));
                        issue.setStatus(doc.getString("status"));

                        // Timestamp handling
                        Object tsObj = doc.get("timestamp");
                        if (tsObj instanceof Timestamp) {
                            Timestamp ts = (Timestamp) tsObj;
                            issue.setTimestamp(String.valueOf(ts.toDate().getTime()));
                        } else if (tsObj != null) {
                            issue.setTimestamp(tsObj.toString());
                        } else {
                            issue.setTimestamp("");
                        }

                        // Save document ID
                        issue.setDocId(doc.getId());

                        allIssues.add(issue);
                    }

                    adapter.updateList(allIssues);
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
    }

    // --- Status Update Dialog ---
    private void showStatusDialog(CivicIssue issue) {
        String[] options = {"Pending", "In Progress", "Resolved"};

        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setItems(options, (dialog, which) -> {
                    String newStatus = options[which];

                    if (issue.getDocId() != null) {
                        firestore.collection("civic_issues")
                                .document(issue.getDocId())
                                .update("status", newStatus)
                                .addOnSuccessListener(aVoid -> {
                                    issue.setStatus(newStatus);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
                    }
                })
                .show();
    }
}