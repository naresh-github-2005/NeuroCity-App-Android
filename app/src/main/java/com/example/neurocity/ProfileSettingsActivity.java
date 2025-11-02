package com.example.neurocity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileSettingsActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private EditText editName, editAddress, editPhone;
    private Button btnSave, btnChangePhoto;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
        setTitle("Profile Settings");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize views
        imgProfile = findViewById(R.id.img_profile);
        editName = findViewById(R.id.edit_name);
        editAddress = findViewById(R.id.edit_address);
        editPhone = findViewById(R.id.edit_phone);
        btnSave = findViewById(R.id.btn_save);
        btnChangePhoto = findViewById(R.id.btn_change_photo);
        progressBar = findViewById(R.id.progress_bar);

        // Image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imgProfile.setImageURI(selectedImageUri);
                    }
                }
        );

        // Load existing profile data
        loadProfileData();

        // Change photo button
        btnChangePhoto.setOnClickListener(v -> openImagePicker());

        // Save button
        btnSave.setOnClickListener(v -> saveProfileData());
    }

    private void loadProfileData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = currentUser.getUid();

        firestore.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);
                    if (document.exists()) {
                        String name = document.getString("name");
                        String address = document.getString("address");
                        String phone = document.getString("phone");
                        String photoUrl = document.getString("photoUrl");

                        if (name != null) editName.setText(name);
                        if (address != null) editAddress.setText(address);
                        if (phone != null) editPhone.setText(phone);

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .circleCrop()
                                    .into(imgProfile);
                        }
                    } else {
                        // Set email as default name
                        if (currentUser.getEmail() != null) {
                            editName.setText(currentUser.getEmail().split("@")[0]);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveProfileData() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String name = editName.getText().toString().trim();
        String address = editAddress.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editName.setError("Name is required");
            editName.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String userId = currentUser.getUid();

        // If image is selected, upload it first
        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(userId, name, address, phone);
        } else {
            saveProfileToFirestore(userId, name, address, phone, null);
        }
    }

    private void uploadImageAndSaveProfile(String userId, String name, String address, String phone) {
        StorageReference imageRef = storage.getReference()
                .child("profile_images")
                .child(userId + ".jpg");

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        saveProfileToFirestore(userId, name, address, phone, photoUrl);
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfileToFirestore(String userId, String name, String address, String phone, String photoUrl) {
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("name", name);
        profileData.put("address", address);
        profileData.put("phone", phone);

        if (photoUrl != null) {
            profileData.put("photoUrl", photoUrl);
        }

        firestore.collection("users").document(userId)
                .update(profileData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, " Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // If document doesn't exist, create it
                    firestore.collection("users").document(userId)
                            .set(profileData)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                btnSave.setEnabled(true);
                                Toast.makeText(this, " Profile created successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e2 -> {
                                progressBar.setVisibility(View.GONE);
                                btnSave.setEnabled(true);
                                Toast.makeText(this, " Error: " + e2.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
    }
}