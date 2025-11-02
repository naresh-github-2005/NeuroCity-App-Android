package com.example.neurocity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.*;

public class UploadFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSION_REQUEST_CODE = 200;

    ImageView imagePreview;
    Button btnCamera, btnGallery, btnSubmitIssue;
    ProgressBar progressBar;
    TextView tvResult, tvLocation;
    Spinner spnIssueType;
    EditText editDescription;

    Uri imageUri = null;
    FusedLocationProviderClient fusedLocationClient;
    FirebaseFirestore db;
    FirebaseStorage storage;
    FirebaseAuth mAuth;

    double latitude = 0.0, longitude = 0.0;
    boolean isGalleryUpload = false;

    GoogleMap mapPicker;
    Marker currentMarker;

    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        imagePreview = view.findViewById(R.id.imagePreview);
        btnCamera = view.findViewById(R.id.btnCamera);
        btnGallery = view.findViewById(R.id.btnGallery);
        btnSubmitIssue = view.findViewById(R.id.btnSubmitIssue);
        progressBar = view.findViewById(R.id.progressBar);
        tvResult = view.findViewById(R.id.tvResult);
        tvLocation = view.findViewById(R.id.tvLocation);
        spnIssueType = view.findViewById(R.id.spinnerIssueType);
        editDescription = view.findViewById(R.id.editDescription);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupSpinner();
        setupActivityResultLaunchers();

        btnCamera.setOnClickListener(v -> openCamera());
        btnGallery.setOnClickListener(v -> openGallery());
        btnSubmitIssue.setOnClickListener(v -> submitIssue());

        return view;
    }

    private void setupSpinner() {
        String[] issues = {
                "Select Issue",
                "Pothole",
                "Water Logging",
                "Streetlight Failure",
                "Garbage Disposal"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, issues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnIssueType.setAdapter(adapter);
    }

    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && imageUri != null) {
                        imagePreview.setImageURI(imageUri);
                        isGalleryUpload = false;
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imagePreview.setImageURI(imageUri);
                        imagePreview.setAlpha(1f);
                        isGalleryUpload = true;
                    }
                });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Issue");
        imageUri = requireActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void submitIssue() {
        if (imageUri == null) {
            Toast.makeText(requireContext(), "Please capture or upload an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedIssue = spnIssueType.getSelectedItem().toString();
        if (selectedIssue.equals("Select Issue")) {
            Toast.makeText(requireContext(), "Please select issue type", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvResult.setText("Uploading issue...");

        if (isGalleryUpload) {
            openMapDialog(selectedIssue);
        } else {
            getCurrentLocationAndUpload(selectedIssue);
        }
    }

    private void getCurrentLocationAndUpload(String issueType) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        tvLocation.setText("Location: " + latitude + ", " + longitude);
                    }
                    uploadImageToFirebase(issueType);
                });
    }

    private void openMapDialog(String issueType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_map_picker, null);
        builder.setView(dialogView);

        MapView mapView = dialogView.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(this);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            tvLocation.setText("Location pinned: " + latitude + ", " + longitude);
            uploadImageToFirebase(issueType);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
            progressBar.setVisibility(View.GONE);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mapPicker = googleMap;
        mapPicker.getUiSettings().setZoomControlsEnabled(true);
        mapPicker.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mapPicker.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                        mapPicker.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 16f));

                        currentMarker = mapPicker.addMarker(new MarkerOptions()
                                .position(current)
                                .title("Current Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                        latitude = current.latitude;
                        longitude = current.longitude;
                    }
                });

        mapPicker.setOnMapClickListener(latLng -> {
            if (currentMarker != null) currentMarker.remove();
            currentMarker = mapPicker.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Selected Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            latitude = latLng.latitude;
            longitude = latLng.longitude;
        });
    }

    private void uploadImageToFirebase(String issueType) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "unknown";
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "issues_images/" + userId + "/" + timeStamp + ".jpg";

        StorageReference storageRef = storage.getReference().child(fileName);
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    saveIssueToFirestore(userId, imageUrl, issueType);
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvResult.setText("Upload failed");
                    Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveIssueToFirestore(String userId, String imageUrl, String issueType) {
        String description = editDescription.getText().toString().trim();
        String department = getDepartmentForIssue(issueType);

        Map<String, Object> issueData = new HashMap<>();
        issueData.put("user_id", userId);
        issueData.put("image_url", imageUrl);
        issueData.put("issue_type", issueType);
        issueData.put("latitude", latitude);
        issueData.put("longitude", longitude);
        issueData.put("timestamp", Timestamp.now());
        issueData.put("status", "Pending");
        issueData.put("department", department);
        if (!description.isEmpty()) issueData.put("description", description);

        db.collection("civic_issues")
                .add(issueData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Issue uploaded successfully ✅", Toast.LENGTH_LONG).show();
                    imagePreview.setImageResource(0);
                    imageUri = null;
                    spnIssueType.setSelection(0);
                    editDescription.setText("");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvResult.setText("Error saving data");
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String getDepartmentForIssue(String issueType) {
        switch (issueType) {
            case "Pothole":
                return "Roads & Transport";
            case "Water Logging":
                return "Water Supply";
            case "Garbage Disposal":
                return "Sanitation";
            case "Streetlight Failure":
                return "Electricity";
            default:
                return "General";
        }
    }
}
