package com.example.neurocity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IssueTrackingActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String issueId;

    // Timeline Views
    private View step1Line, step2Line, step3Line;
    private View step1Circle, step2Circle, step3Circle, step4Circle;
    private TextView step1Text, step2Text, step3Text, step4Text;
    private TextView step1Date, step2Date, step3Date, step4Date;

    // Issue Details Views
    private TextView tvIssueType, tvDescription, tvDepartment;
    private ImageView ivReportedImage, ivResolvedImage;
    private View resolvedImageSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_tracking);

        issueId = getIntent().getStringExtra("issue_id");

        initViews();
        db = FirebaseFirestore.getInstance();
        loadIssueTracking();
    }

    private void initViews() {
        // Timeline elements
        step1Circle = findViewById(R.id.step1Circle);
        step2Circle = findViewById(R.id.step2Circle);
        step3Circle = findViewById(R.id.step3Circle);
        step4Circle = findViewById(R.id.step4Circle);

        step1Line = findViewById(R.id.step1Line);
        step2Line = findViewById(R.id.step2Line);
        step3Line = findViewById(R.id.step3Line);

        step1Text = findViewById(R.id.step1Text);
        step2Text = findViewById(R.id.step2Text);
        step3Text = findViewById(R.id.step3Text);
        step4Text = findViewById(R.id.step4Text);

        step1Date = findViewById(R.id.step1Date);
        step2Date = findViewById(R.id.step2Date);
        step3Date = findViewById(R.id.step3Date);
        step4Date = findViewById(R.id.step4Date);

        // Issue details
        tvIssueType = findViewById(R.id.tvIssueType);
        tvDescription = findViewById(R.id.tvDescription);
        tvDepartment = findViewById(R.id.tvDepartment);
        ivReportedImage = findViewById(R.id.ivReportedImage);
        ivResolvedImage = findViewById(R.id.ivResolvedImage);
        resolvedImageSection = findViewById(R.id.resolvedImageSection);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadIssueTracking() {
        db.collection("civic_issues").document(issueId)
                .get()
                .addOnSuccessListener(this::updateTrackingUI)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load tracking info", Toast.LENGTH_SHORT).show()
                );
    }

    private void updateTrackingUI(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        String status = doc.getString("status");
        String issueType = doc.getString("issue_type");
        String description = doc.getString("description");
        String department = doc.getString("department");
        String assignedWorkerName = doc.getString("assigned_worker_name");
        String imageUrl = doc.getString("image_url");
        String resolvedImageUrl = doc.getString("resolved_image_url");

        // Update issue details
        tvIssueType.setText(issueType != null ? issueType : "Unknown Issue");
        tvDescription.setText(description != null ? description : "No description available");
        tvDepartment.setText("Department: " + (department != null ? department : "Not assigned"));

        // Load reported image
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivReportedImage);
        }

        // Show resolved image if available
        if (resolvedImageUrl != null && !resolvedImageUrl.isEmpty()) {
            resolvedImageSection.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(resolvedImageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(ivResolvedImage);
        } else {
            resolvedImageSection.setVisibility(View.GONE);
        }

        // Update tracking timeline based on status
        updateTimeline(status, assignedWorkerName);
    }

    private void updateTimeline(String status, String workerName) {
        // Reset all to inactive
        resetTimeline();

        // Step 1: Issue Reported - Always active
        activateStep(step1Circle, step1Text);
        step1Date.setText("Completed");

        if (status == null) status = "Pending";

        switch (status) {
            case "Pending":
                // Only step 1 active
                step2Text.setText("Awaiting Assignment");
                step3Text.setText("Work in Progress");
                step4Text.setText("Issue Resolved");
                break;

            case "Assigned":
            case "In Progress":
                // Steps 1 & 2 active
                activateStep(step2Circle, step2Text);
                activateLine(step1Line);
                step2Date.setText("Completed");

                if (workerName != null) {
                    step2Text.setText("Assigned to " + workerName);
                } else {
                    step2Text.setText("Worker Assigned");
                }

                if (status.equals("In Progress")) {
                    activateStep(step3Circle, step3Text);
                    activateLine(step2Line);
                    step3Date.setText("In Progress");
                    step3Text.setText("Work Started");
                }

                step4Text.setText("Issue Resolved");
                break;

            case "Resolved":
                // All steps active
                activateStep(step2Circle, step2Text);
                activateStep(step3Circle, step3Text);
                activateStep(step4Circle, step4Text);
                activateLine(step1Line);
                activateLine(step2Line);
                activateLine(step3Line);

                step2Date.setText("Completed");
                step3Date.setText("Completed");
                step4Date.setText("Completed");

                if (workerName != null) {
                    step2Text.setText("Assigned to " + workerName);
                } else {
                    step2Text.setText("Worker Assigned");
                }
                step3Text.setText("Work Completed");
                step4Text.setText("Issue Resolved ✓");
                break;
        }
    }

    private void resetTimeline() {
        int inactiveColor = ContextCompat.getColor(this, R.color.textColorSecondary);

        step1Circle.setBackgroundResource(R.drawable.timeline_circle_inactive);
        step2Circle.setBackgroundResource(R.drawable.timeline_circle_inactive);
        step3Circle.setBackgroundResource(R.drawable.timeline_circle_inactive);
        step4Circle.setBackgroundResource(R.drawable.timeline_circle_inactive);

        step1Line.setBackgroundColor(inactiveColor);
        step2Line.setBackgroundColor(inactiveColor);
        step3Line.setBackgroundColor(inactiveColor);

        step1Text.setTextColor(inactiveColor);
        step2Text.setTextColor(inactiveColor);
        step3Text.setTextColor(inactiveColor);
        step4Text.setTextColor(inactiveColor);

        step1Date.setText("");
        step2Date.setText("");
        step3Date.setText("");
        step4Date.setText("");
    }

    private void activateStep(View circle, TextView text) {
        circle.setBackgroundResource(R.drawable.timeline_circle_active);
        text.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
    }

    private void activateLine(View line) {
        line.setBackgroundColor(ContextCompat.getColor(this, R.color.statusResolved));
    }
}