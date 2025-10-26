package com.example.neurocity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminIssuesAdapter extends RecyclerView.Adapter<AdminIssuesAdapter.ViewHolder> {

    private List<CivicIssue> issueList;
    private Context context;
    private OnStatusClickListener listener;

    public interface OnStatusClickListener {
        void onStatusClick(CivicIssue issue);
    }

    public AdminIssuesAdapter(Context context, List<CivicIssue> issueList, OnStatusClickListener listener) {
        this.context = context;
        this.issueList = issueList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView issueImage;
        TextView issueType, issueDesc, issueStatus;
        Button btnUpdateStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            issueImage = itemView.findViewById(R.id.issueImage);
            issueType = itemView.findViewById(R.id.issueType);
            issueDesc = itemView.findViewById(R.id.issueDesc);
            issueStatus = itemView.findViewById(R.id.issueStatus);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_issue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CivicIssue issue = issueList.get(position);

        holder.issueType.setText(issue.getIssue_type());
        holder.issueDesc.setText(issue.getDescription());

        // Format timestamp nicely
        holder.issueStatus.setText("Status: " + issue.getStatus() + "\n" +
                "Reported: " + formatTimestamp(issue.getTimestamp()));

        // Optional: color-code status
        switch (issue.getStatus()) {
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
                holder.issueStatus.setTextColor(0xFF000000); // Black
        }

        Glide.with(context).load(issue.getImage_url()).into(holder.issueImage);

        holder.btnUpdateStatus.setOnClickListener(v -> listener.onStatusClick(issue));
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
