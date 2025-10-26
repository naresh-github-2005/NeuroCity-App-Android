package com.example.neurocity;

import android.app.MediaRouteButton;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;

public class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.ViewHolder> {

    private final Context context;
    private final List<CivicIssue> issueList;

    public ComplaintsAdapter(Context context, List<CivicIssue> issueList) {
        this.context = context;
        this.issueList = issueList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CivicIssue issue = issueList.get(position);

        holder.tvType.setText("Issue: " + issue.getIssue_type());
        holder.tvLocation.setText("Lat: " + issue.getLatitude() + ", Lng: " + issue.getLongitude());
        holder.tvTimestamp.setText("Time: " + issue.getTimestamp());

        // ✅ Show description only if available
        if (issue.getDescription() != null && !issue.getDescription().isEmpty()) {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText("Description: " + issue.getDescription());
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // --- Status display ---
        if (issue.getStatus() != null) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText(issue.getStatus());

            int colorRes;
            switch (issue.getStatus()) {
                case "Pending":
                    colorRes = context.getResources().getColor(R.color.statusPending);
                    break;
                case "In Progress":
                    colorRes = context.getResources().getColor(R.color.statusInProgress);
                    break;
                case "Resolved":
                    colorRes = context.getResources().getColor(R.color.statusResolved);
                    break;
                default:
                    colorRes = context.getResources().getColor(R.color.textColorSecondary);
            }
            holder.tvStatus.setBackgroundColor(colorRes);
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(issue.getImage_url())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.ivIssue);
    }

    @Override
    public int getItemCount() {
        return issueList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvStatus;
        ImageView ivIssue;
        TextView tvType, tvLocation, tvTimestamp, tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIssue = itemView.findViewById(R.id.ivIssue);
            tvType = itemView.findViewById(R.id.tvType);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
