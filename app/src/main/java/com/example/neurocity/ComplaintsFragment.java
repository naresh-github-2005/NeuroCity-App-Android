package com.example.neurocity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.*;

public class ComplaintsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ComplaintsAdapter adapter;
    private List<CivicIssue> issueList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_complaints, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewComplaints);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        issueList = new ArrayList<>();
        adapter = new ComplaintsAdapter(requireContext(), issueList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadUserComplaints();

        return view;
    }

    private void loadUserComplaints() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) return;

        db.collection("civic_issues")
                .whereEqualTo("user_id", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    issueList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String imageUrl = doc.getString("image_url");
                        String issueType = doc.getString("issue_type");
                        String description = doc.getString("description");
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String userId = doc.getString("user_id");
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        String status = doc.getString("status");
                        String department = doc.getString("department");
                        String assigned_worker_id = doc.getString("assigned_worker_id");
                        String assigned_worker_name = doc.getString("assigned_worker_name");
                        String resolved_image_url = doc.getString("resolved_image_url");



                        String formattedDate = "";
                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            formattedDate = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                    .format(date);
                        }

                        CivicIssue issue = new CivicIssue(
                                imageUrl != null ? imageUrl : "",
                                issueType != null ? issueType : "Unknown",
                                lat != null ? lat : 0.0,
                                lng != null ? lng : 0.0,
                                formattedDate,
                                userId != null ? userId : "Unknown",
                                description != null ? description : "",
                                status != null ? status : "Pending",
                                department = "",
                                assigned_worker_id = "",
                                assigned_worker_name = "",
                                resolved_image_url = ""
                        );

                        // Set the document ID
                        issue.setDocId(doc.getId());

                        issueList.add(issue);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
