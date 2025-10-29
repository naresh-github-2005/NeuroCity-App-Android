package com.example.neurocity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText editEmail, editPassword;
    Button btnLogin, btnGoToRegister, btnForgotPassword;
    ProgressBar progressBar;
    SignInButton btnGoogleSignIn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ---------------------------
        // Initialize Views
        // ---------------------------
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        progressBar = findViewById(R.id.progressBar);
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        // ---------------------------
        // Initialize Firebase
        // ---------------------------
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ---------------------------
        // Auto Login if User Already Signed In
        // ---------------------------
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserRole(currentUser.getUid());
        }

        // ---------------------------
        // Configure Google Sign-In
        // ---------------------------
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // ---------------------------
        // Email/Password Login
        // ---------------------------
        btnLogin.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(ProgressBar.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(ProgressBar.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                fetchUserRole(user.getUid());
                                saveFcmToken(user.getUid());
                            }
                        } else {
                            Toast.makeText(this, "Auth failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // ---------------------------
        // Google Sign-In
        // ---------------------------
        btnGoogleSignIn.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // ---------------------------
        // Navigate to RegisterActivity
        // ---------------------------
        btnGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        // ---------------------------
// Forgot Password
// ---------------------------
        btnForgotPassword.setOnClickListener(v -> {
            // Create a proper EditText with padding and styling
            EditText resetMail = new EditText(this);
            resetMail.setHint("Enter your email");
            resetMail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            // Add padding to the EditText
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            resetMail.setPadding(padding, padding, padding, padding);

            new AlertDialog.Builder(this)
                    .setTitle("Reset Password")
                    .setMessage("Enter your email to receive a password reset link.")
                    .setView(resetMail)
                    .setPositiveButton("Send", (dialog, which) -> {
                        String mail = resetMail.getText().toString().trim(); // Add trim()

                        if (TextUtils.isEmpty(mail)) {
                            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Validate email format
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Show progress
                        progressBar.setVisibility(ProgressBar.VISIBLE);

                        mAuth.sendPasswordResetEmail(mail)
                                .addOnSuccessListener(unused -> {
                                    progressBar.setVisibility(ProgressBar.GONE);
                                    Toast.makeText(this, "Reset link sent to " + mail, Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(ProgressBar.GONE);

                                    // Handle specific error cases
                                    String errorMessage = "Error sending reset email";
                                    if (e.getMessage() != null) {
                                        if (e.getMessage().contains("no user record")) {
                                            errorMessage = "No account found with this email";
                                        } else if (e.getMessage().contains("badly formatted")) {
                                            errorMessage = "Invalid email format";
                                        } else {
                                            errorMessage = e.getMessage();
                                        }
                                    }

                                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                                    android.util.Log.e("ForgotPassword", "Error: " + e.getMessage(), e);
                                });
                    })
                    .setNegativeButton("Cancel", null) // Simpler - auto dismisses
                    .show(); // Use show() instead of create().show()
        });
    }

    // ---------------------------
    // Google Sign-In Result
    // ---------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            db.collection("users").document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(document -> {
                                        if (document.exists()) {
                                            fetchUserRole(user.getUid());
                                        } else {
                                            db.collection("users").document(user.getUid())
                                                    .set(new HashMap<String, Object>() {{
                                                        put("email", user.getEmail());
                                                        put("role", "user");
                                                    }})
                                                    .addOnSuccessListener(aVoid -> redirectUser("user"));
                                        }
                                    });
                            saveFcmToken(user.getUid());
                        }
                    } else {
                        Toast.makeText(this, "Google authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 Fetch role from Firestore
    private void fetchUserRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String role = document.getString("role");
                        if (role == null) role = "user";
                        redirectUser(role);
                    } else {
                        Toast.makeText(this, "No user data found. Please register.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching user role: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 🔹 Save FCM Token to Firestore for Notifications
    private void saveFcmToken(String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        db.collection("users").document(uid)
                                .update("fcmToken", token)
                                .addOnFailureListener(e -> {});
                    }
                });
    }

    // 🔹 Redirect based on role
    private void redirectUser(String role) {
        if ("official".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
