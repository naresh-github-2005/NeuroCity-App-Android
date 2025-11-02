package com.example.neurocity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class WorkerIssuesAdapter extends RecyclerView.Adapter<WorkerIssuesAdapter.ViewHolder> {

    public interface OnUploadResolvedImageClickListener {
        void onUploadResolvedImageClick(int position);
    }

    private List<CivicIssue> issueList;
    private Context context;
    private OnUploadResolvedImageClickListener uploadListener;

    public WorkerIssuesAdapter(Context context, List<CivicIssue> issueList) {
        this.context = context;
        this.issueList = issueList;
    }

    public void setOnUploadResolvedImageClickListener(OnUploadResolvedImageClickListener listener) {
        this.uploadListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView issueImage;
        TextView issueType, issueDesc, issueStatus ,imageLabel;;
        MaterialButton btnUploadResolvedImage;

        public ViewHolder(View itemView, OnUploadResolvedImageClickListener listener) {
            super(itemView);
            issueImage = itemView.findViewById(R.id.issueImage);
            issueType = itemView.findViewById(R.id.issueType);
            issueDesc = itemView.findViewById(R.id.issueDesc);
            issueStatus = itemView.findViewById(R.id.issueStatus);
            btnUploadResolvedImage = itemView.findViewById(R.id.btnUploadResolvedImage);
            imageLabel = itemView.findViewById(R.id.imageLabel);

            btnUploadResolvedImage.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onUploadResolvedImageClick(position);
                    }
                }
            });

        }
    }

    @NonNull
    @Override
    public WorkerIssuesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_worker_issue, parent, false);
        return new ViewHolder(view, uploadListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkerIssuesAdapter.ViewHolder holder, int position) {
        CivicIssue issue = issueList.get(position);

        if (issue.getResolved_image_url() != null && !issue.getResolved_image_url().isEmpty()) {
            Glide.with(context).load(issue.getResolved_image_url()).into(holder.issueImage);
            holder.imageLabel.setText("Resolved Image");
        } else {
            Glide.with(context).load(issue.getImage_url()).into(holder.issueImage);
            holder.imageLabel.setText("Original Image");
        }

        holder.issueType.setText(issue.getIssue_type());
        holder.issueDesc.setText(issue.getDescription());

        holder.issueStatus.setText("Status: " + issue.getStatus() + "\n" +
                "Reported: " + formatTimestamp(issue.getTimestamp()));

        // Show/hide upload button only if not resolved
        boolean isResolved = "Resolved".equalsIgnoreCase(issue.getStatus());
        holder.btnUploadResolvedImage.setVisibility(isResolved ? View.GONE : View.VISIBLE);

        String status = issue.getStatus() != null ? issue.getStatus() : "Pending";
        // 🎨 Color-code status
        switch (status) {
            case "Pending":
                holder.issueStatus.setTextColor(0xFFFFA500); // Orange
                break;
            case "In Progress":
                holder.issueStatus.setTextColor(0xFF2196F3); // Blue
                break;
            case "Resolved":
                holder.issueStatus.setTextColor(0xFF4CAF50); // Green
                break;
            default:
                holder.issueStatus.setTextColor(0xFF000000);
        }
    }


    @Override
    public int getItemCount() {
        return issueList.size();
    }

    public void updateList(List<CivicIssue> newList) {
        issueList = newList;
        notifyDataSetChanged();
    }

    private String formatTimestamp(String timeStr) {
        try {
            long millis = Long.parseLong(timeStr);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            return sdf.format(new Date(millis));
        } catch (Exception e) {
            return timeStr;
        }
    }
}
