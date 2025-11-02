package com.example.neurocity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminIssuesAdapter extends RecyclerView.Adapter<AdminIssuesAdapter.ViewHolder> {

    private static final String TAG = "AdminIssuesAdapter";

    private final Context context;
    private final List<CivicIssue> issueList;
    private final FirebaseFirestore db;
    private final OnStatusClickListener listener;

    public interface OnStatusClickListener {
        void onStatusClick(CivicIssue issue);
    }

    public AdminIssuesAdapter(Context context, List<CivicIssue> issueList, OnStatusClickListener listener) {
        this.context = context;
        this.issueList = issueList;
        this.db = FirebaseFirestore.getInstance();
        this.listener = listener;
        Log.d(TAG, "Adapter created with " + issueList.size() + " items");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_issue, parent, false);
        Log.d(TAG, "View inflated successfully");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);

        CivicIssue issue = issueList.get(position);
        Log.d(TAG, "Binding issue: " + issue.getIssue_type() + " - Status: " + issue.getStatus());

        // Set issue type with null check
        holder.txtIssueType.setText(issue.getIssue_type() != null ? issue.getIssue_type() : "Unknown Issue");

        // Set description with null check
        holder.txtDesc.setText(issue.getDescription() != null ? issue.getDescription() : "No description available");

        // Set status with null check
        String status = issue.getStatus() != null ? issue.getStatus() : "Pending";
        holder.txtStatus.setText("Status: " + status);

        // Set department with null check
        String dept = issue.getDepartment() != null ? issue.getDepartment() : "Unassigned";
        holder.txtDept.setText("Dept: " + dept);

        // Set reported time
        holder.txtReportedTime.setText("Reported: " + formatTimestamp(issue.getTimestamp()));

        // 👷 Show assigned worker if exists
        if (issue.getAssigned_worker_name() != null && !issue.getAssigned_worker_name().isEmpty()) {
            holder.txtAssigned.setVisibility(View.VISIBLE);
            holder.txtAssigned.setText("Assigned to: " + issue.getAssigned_worker_name());
        } else {
            holder.txtAssigned.setVisibility(View.GONE);
        }

        // 🖼️ Load image
        if (issue.getImage_url() != null && !issue.getImage_url().isEmpty()) {
            Glide.with(context)
                    .load(issue.getImage_url())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(holder.imgIssue);
        } else {
            holder.imgIssue.setImageResource(R.drawable.ic_placeholder);
        }

        // 🎨 Color-code status
        switch (status) {
            case "Pending":
                holder.txtStatus.setTextColor(0xFFFFA500); // Orange
                break;
            case "In Progress":
                holder.txtStatus.setTextColor(0xFF2196F3); // Blue
                break;
            case "Resolved":
                holder.txtStatus.setTextColor(0xFF4CAF50); // Green
                break;
            default:
                holder.txtStatus.setTextColor(0xFF000000);
        }

        // 📍 View Location in Google Maps
        holder.btnViewMap.setOnClickListener(v -> {
            double lat = issue.getLatitude();
            double lng = issue.getLongitude();

            if (lat == 0.0 && lng == 0.0) {
                Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show();
                return;
            }

            String uri = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");

            try {
                context.startActivity(mapIntent);
            } catch (Exception e) {
                // If Google Maps is not installed, open in browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(browserIntent);
            }
        });

        // 🧰 Assign Worker (only same department)
        holder.btnAssignWorker.setOnClickListener(v -> {
            String department = issue.getDepartment();

            // Null check for department
            if (department == null || department.isEmpty() || department.equals("Unassigned")) {
                Toast.makeText(context, "Issue has no department assigned", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("role", "worker")
                    .whereEqualTo("department", department)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            List<String> workerNames = new ArrayList<>();
                            List<String> workerIds = new ArrayList<>();

                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                String name = doc.getString("name");
                                if (name != null && !name.isEmpty()) {
                                    workerNames.add(name);
                                    workerIds.add(doc.getId());
                                }
                            }

                            if (workerNames.isEmpty()) {
                                Toast.makeText(context, "No workers found in " + department + " department", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            new AlertDialog.Builder(context)
                                    .setTitle("Assign Worker (" + department + ")")
                                    .setItems(workerNames.toArray(new String[0]), (dialog, which) -> {
                                        String selectedWorkerId = workerIds.get(which);
                                        String selectedWorkerName = workerNames.get(which);

                                        db.collection("civic_issues").document(issue.getDocId())
                                                .update(
                                                        "assigned_worker_id", selectedWorkerId,
                                                        "assigned_worker_name", selectedWorkerName,
                                                        "status", "In Progress"
                                                )
                                                .addOnSuccessListener(unused -> {
                                                    Toast.makeText(context, "✅ Assigned to " + selectedWorkerName, Toast.LENGTH_SHORT).show();
                                                    issue.setAssigned_worker_id(selectedWorkerId);
                                                    issue.setAssigned_worker_name(selectedWorkerName);
                                                    issue.setStatus("In Progress");
                                                    notifyItemChanged(holder.getAdapterPosition());
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(context, "❌ Failed to assign: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                });
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();

                        } else {
                            Toast.makeText(context, "No workers found in " + department + " department", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error fetching workers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // 🟢 Update Status Button
        holder.btnUpdateStatus.setOnClickListener(v -> listener.onStatusClick(issue));

        Log.d(TAG, "Issue bound successfully at position " + position);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount called, returning: " + issueList.size());
        return issueList.size();
    }

    public void updateList(List<CivicIssue> updatedList) {
        Log.d(TAG, "updateList called with " + (updatedList != null ? updatedList.size() : "null") + " items");

        if (updatedList == null) {
            Log.e(TAG, "❌ updateList received null list!");
            return;
        }

        issueList.clear();
        issueList.addAll(updatedList);
        notifyDataSetChanged();

        Log.d(TAG, "✅ Adapter updated. Current size: " + issueList.size());
    }

    private String formatTimestamp(String timeStr) {
        try {
            if (timeStr == null || timeStr.isEmpty()) {
                return "Unknown";
            }
            long millis = Long.parseLong(timeStr);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
            return sdf.format(new Date(millis));
        } catch (Exception e) {
            return timeStr != null ? timeStr : "Unknown";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgIssue;
        TextView txtIssueType, txtDesc, txtStatus, txtDept, txtAssigned, txtReportedTime;
        MaterialButton btnAssignWorker, btnViewMap, btnUpdateStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            Log.d(TAG, "ViewHolder constructor called");

            imgIssue = itemView.findViewById(R.id.imgIssue);
            txtIssueType = itemView.findViewById(R.id.txtIssueType);
            txtDesc = itemView.findViewById(R.id.txtIssueDesc);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDept = itemView.findViewById(R.id.txtDept);
            txtAssigned = itemView.findViewById(R.id.txtAssigned);
            txtReportedTime = itemView.findViewById(R.id.txtReportedTime);
            btnAssignWorker = itemView.findViewById(R.id.btnAssignWorker);
            btnViewMap = itemView.findViewById(R.id.btnViewMap);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);

            // Check if any views are null
            if (imgIssue == null) Log.e(TAG, "❌ imgIssue is NULL!");
            if (txtIssueType == null) Log.e(TAG, "❌ txtIssueType is NULL!");
            if (txtDesc == null) Log.e(TAG, "❌ txtDesc is NULL!");
            if (txtStatus == null) Log.e(TAG, "❌ txtStatus is NULL!");
            if (txtDept == null) Log.e(TAG, "❌ txtDept is NULL!");
            if (txtAssigned == null) Log.e(TAG, "❌ txtAssigned is NULL!");
            if (txtReportedTime == null) Log.e(TAG, "❌ txtReportedTime is NULL!");
            if (btnAssignWorker == null) Log.e(TAG, "❌ btnAssignWorker is NULL!");
            if (btnViewMap == null) Log.e(TAG, "❌ btnViewMap is NULL!");
            if (btnUpdateStatus == null) Log.e(TAG, "❌ btnUpdateStatus is NULL!");

            Log.d(TAG, "ViewHolder views initialized");
        }
    }
}