package com.example.neurocity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        db = FirebaseFirestore.getInstance();

        // Setup the Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        loadIssuesFromFirestore();
    }

    private void loadIssuesFromFirestore() {
        db.collection("civic_issues")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        String issueType = doc.getString("issue_type");
                        Date timestamp = doc.getDate("timestamp");

                        if (lat != null && lng != null) {
                            LatLng location = new LatLng(lat, lng);

                            String formattedDate = timestamp != null ?
                                    new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(timestamp)
                                    : "Unknown time";

                            mMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .title(issueType)
                                    .snippet("Reported: " + formattedDate));

                            // Optional: Move camera to first marker
                            if (queryDocumentSnapshots.getDocuments().indexOf(doc) == 0) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f));
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
}
