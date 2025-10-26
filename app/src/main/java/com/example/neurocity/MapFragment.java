package com.example.neurocity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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

        // Realtime updates
        db.collection("civic_issues")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) return;

                    mMap.clear(); // Clear old markers to avoid duplicates

                    // Add markers for each document
                    for (int i = 0; i < querySnapshot.getDocuments().size(); i++) {
                        addMarkerFromDoc(querySnapshot.getDocuments().get(i));
                    }
                });
    }

    private void addMarkerFromDoc(@NonNull com.google.firebase.firestore.DocumentSnapshot doc) {
        Double lat = doc.getDouble("latitude");
        Double lng = doc.getDouble("longitude");
        String issueType = doc.getString("issue_type");
        String status = doc.getString("status");
        Date timestamp = doc.getDate("timestamp");

        if (lat != null && lng != null) {
            LatLng location = new LatLng(lat, lng);

            String formattedDate = timestamp != null ?
                    new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(timestamp)
                    : "Unknown time";

            // Determine marker color based on status
            float hue = BitmapDescriptorFactory.HUE_RED; // default Pending
            if ("Pending".equalsIgnoreCase(status)) hue = BitmapDescriptorFactory.HUE_RED;
            else if ("In Progress".equalsIgnoreCase(status)) hue = BitmapDescriptorFactory.HUE_ORANGE;
            else if ("Resolved".equalsIgnoreCase(status)) hue = BitmapDescriptorFactory.HUE_GREEN;

            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(issueType)
                    .snippet("Status: " + status + "\nReported: " + formattedDate)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
            );
        }
    }
}
