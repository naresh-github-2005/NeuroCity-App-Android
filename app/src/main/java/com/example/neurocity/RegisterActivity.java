package com.example.neurocity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editEmail, editPassword, editConfirmPassword;
    private Button btnRegister, btnGoToLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLogin = findViewById(R.id.btnGoToLogin);
        progressBar = findViewById(R.id.progressBar);

        // Register button action
        btnRegister.setOnClickListener(v -> registerUser());

        // Navigate to Login page
        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Password is required!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match!");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();

                        // Save user info to Firestore (default role = "user")
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("role", "user");

                        db.collection("users").document(uid)
                                .set(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(RegisterActivity.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        Toast.makeText(RegisterActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void saveFcmToken(String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        db.collection("users").document(uid)
                                .update("fcmToken", token)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "Device registered for notifications", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> {});
                    }
                });
    }
}
