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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ComplaintsAdapter adapter;
    private List<CivicIssue> issueList;
    private FirebaseFirestore db;

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
        loadComplaints();

        return view;
    }

    private void loadComplaints() {
        db.collection("civic_issues")
                .orderBy("timestamp")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    issueList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String imageUrl = doc.getString("image_url");
                        String issueType = doc.getString("issue_type");
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String userId = doc.getString("user_id");
                        Timestamp timestamp = doc.getTimestamp("timestamp");

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
                                userId != null ? userId : "Unknown"
                        );

                        issueList.add(issue);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                });
    }
}
