package com.example.neurocity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.*;

public class UploadFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 200;

    ImageView imagePreview;
    Button btnCamera, btnGallery, btnSubmitIssue;
    ProgressBar progressBar;
    TextView tvResult, tvLocation;
    Spinner spnIssueType;
    EditText editDescription; // ✅ New field for optional description

    Uri imageUri = null;
    FusedLocationProviderClient fusedLocationClient;
    FirebaseFirestore db;
    FirebaseStorage storage;
    FirebaseAuth mAuth;

    double latitude = 0.0, longitude = 0.0;

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
        editDescription = view.findViewById(R.id.editDescription); // ✅ Link to layout

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
        String[] issues = {"Select Issue", "Pothole", "Water Logging", "Streetlight Failure", "Garbage Disposal"};
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
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imagePreview.setImageURI(imageUri);
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

        progressBar.setVisibility(ProgressBar.VISIBLE);
        tvResult.setText("Uploading issue...");

        getCurrentLocationAndUpload(selectedIssue);
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
                    }
                    tvLocation.setText("Location: " + latitude + ", " + longitude);
                    uploadImageToFirebase(issueType);
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
                    progressBar.setVisibility(ProgressBar.GONE);
                    tvResult.setText("Upload failed");
                    Toast.makeText(requireContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveIssueToFirestore(String userId, String imageUrl, String issueType) {
        String description = editDescription.getText().toString().trim(); // ✅ Optional field

        Map<String, Object> issueData = new HashMap<>();
        issueData.put("user_id", userId);
        issueData.put("image_url", imageUrl);
        issueData.put("issue_type", issueType);
        issueData.put("latitude", latitude);
        issueData.put("longitude", longitude);
        issueData.put("timestamp", new Date());
        issueData.put("status", "Pending");
        if (!description.isEmpty()) issueData.put("description", description); // ✅ Add only if provided

        db.collection("civic_issues")
                .add(issueData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(requireContext(), "Issue uploaded: " + issueType, Toast.LENGTH_LONG).show();
                    imagePreview.setImageResource(0);
                    imageUri = null;
                    spnIssueType.setSelection(0);
                    editDescription.setText(""); // ✅ Clear field
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    tvResult.setText("Error saving data");
                    Toast.makeText(requireContext(), "Error saving data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
