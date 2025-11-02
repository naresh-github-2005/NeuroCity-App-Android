package com.example.neurocity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class WorkerDashboardActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private int selectedIssuePosition = -1;

    private MaterialToolbar topAppBar;
    private RecyclerView recyclerView;
    private FloatingActionButton btnNavigate;
    private WorkerIssuesAdapter adapter;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    private final List<CivicIssue> assignedIssues = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setTitle("NeuroCity");
        topAppBar.setSubtitle("Worker Dashboard");
        recyclerView = findViewById(R.id.recyclerAssignedIssues);
        btnNavigate = findViewById(R.id.btnNavigate);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupToolbar();
        setupRecyclerView();
        loadAssignedIssues();

        btnNavigate.setOnClickListener(v -> openGoogleMapsWithRoute());
    }

    private void setupToolbar() {
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

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
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WorkerIssuesAdapter(this, assignedIssues);
        recyclerView.setAdapter(adapter);

        // Set upload button click listener here
        adapter.setOnUploadResolvedImageClickListener(position -> {
            selectedIssuePosition = position;
            openImagePicker();
        });
    }

    private void loadAssignedIssues() {
        String workerId = auth.getCurrentUser().getUid();

        firestore.collection("civic_issues")
                .whereEqualTo("assigned_worker_id", workerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    assignedIssues.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CivicIssue issue = new CivicIssue();

                        issue.setDocId(doc.getId());
                        issue.setImage_url(doc.getString("image_url"));
                        issue.setIssue_type(doc.getString("issue_type"));
                        issue.setLatitude(doc.getDouble("latitude") != null ? doc.getDouble("latitude") : 0.0);
                        issue.setLongitude(doc.getDouble("longitude") != null ? doc.getDouble("longitude") : 0.0);
                        issue.setUser_id(doc.getString("user_id"));
                        issue.setDescription(doc.getString("description"));
                        issue.setStatus(doc.getString("status"));
                        issue.setDepartment(doc.getString("department"));
                        issue.setAssigned_worker_id(doc.getString("assigned_worker_id"));
                        issue.setAssigned_worker_name(doc.getString("assigned_worker_name"));
                        issue.setResolved_image_url(doc.getString("resolved_image_url"));

                        Object tsObj = doc.get("timestamp");
                        if (tsObj instanceof com.google.firebase.Timestamp) {
                            com.google.firebase.Timestamp ts = (com.google.firebase.Timestamp) tsObj;
                            issue.setTimestamp(String.valueOf(ts.toDate().getTime()));
                        } else if (tsObj != null) {
                            issue.setTimestamp(tsObj.toString());
                        } else {
                            issue.setTimestamp("0");
                        }

                        assignedIssues.add(issue);
                    }
                    adapter.notifyDataSetChanged();
                    if (assignedIssues.isEmpty())
                        Toast.makeText(this, "No issues assigned yet", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load issues", Toast.LENGTH_SHORT).show());
    }

    private void openGoogleMapsWithRoute() {
        if (assignedIssues.size() < 2) {
            Toast.makeText(this, "Need at least 2 issues to create route", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder uri = new StringBuilder("https://www.google.com/maps/dir/?api=1");
        CivicIssue origin = assignedIssues.get(0);
        CivicIssue destination = assignedIssues.get(assignedIssues.size() - 1);

        uri.append("&origin=").append(origin.getLatitude()).append(",").append(origin.getLongitude());
        uri.append("&destination=").append(destination.getLatitude()).append(",").append(destination.getLongitude());

        if (assignedIssues.size() > 2) {
            uri.append("&waypoints=");
            for (int i = 1; i < assignedIssues.size() - 1; i++) {
                CivicIssue wp = assignedIssues.get(i);
                uri.append(wp.getLatitude()).append(",").append(wp.getLongitude());
                if (i < assignedIssues.size() - 2) uri.append("|");
            }
        }

        uri.append("&travelmode=driving");

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri.toString()));
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
        else
            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Resolved Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadResolvedImage(imageUri);
        }
    }

    private void uploadResolvedImage(Uri imageUri) {
        if (selectedIssuePosition == -1) return;

        CivicIssue issue = assignedIssues.get(selectedIssuePosition);
        String docId = issue.getDocId();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference resolvedImageRef = storageRef.child("resolved_images/" + docId + ".jpg");

        resolvedImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> resolvedImageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            firestore.collection("civic_issues").document(docId)
                                    .update("resolved_image_url", downloadUri.toString(), "status", "Resolved")
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Issue marked as resolved!", Toast.LENGTH_SHORT).show();

                                        issue.setStatus("Resolved");
                                        issue.setResolved_image_url(downloadUri.toString());

                                        adapter.notifyItemChanged(selectedIssuePosition);
                                        selectedIssuePosition = -1;
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update issue", Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show());
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage("View your assigned issues and navigate through all their locations via Google Maps.")
                .setPositiveButton("OK", null)
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

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage("NeuroCity Worker Dashboard\nSmart civic issue tracking and routing system.")
                .setPositiveButton("Close", null)
                .show();
    }
}
