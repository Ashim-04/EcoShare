package com.ecoshare.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ecoshare.app.R;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.NetworkUtil;
import com.ecoshare.app.utils.PrefsManager;
import com.ecoshare.app.utils.ValidationUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private TextView registerTextView;
    private TextView forgotPasswordTextView;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        initializeHelpers();
        setupListeners();
    }

    private void initializeViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsManager = PrefsManager.getInstance();
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> validateAndLogin());
        
        registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void validateAndLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.error_email_required));
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError(getString(R.string.error_password_required));
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.error_password_too_short));
            passwordEditText.requestFocus();
            return;
        }

        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoadingState(true);

        FirebaseAuth auth = firebaseHelper.getAuth();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        String userId = authResult.getUser().getUid();
                        loadUserDataAndProceed(userId);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    String errorMessage = getAuthErrorMessage(e.getMessage());
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    private void loadUserDataAndProceed(String userId) {
        firebaseHelper.getUsersCollection()
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        saveUserDataToPrefs(documentSnapshot);
                        navigateToMain();
                    } else {
                        setLoadingState(false);
                        Toast.makeText(this, R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, R.string.error_loading_user_data, Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserDataToPrefs(DocumentSnapshot documentSnapshot) {
        String userId = documentSnapshot.getId();
        String name = documentSnapshot.getString("name");
        String email = documentSnapshot.getString("email");

        prefsManager.saveUserId(userId);
        if (name != null) {
            prefsManager.saveUserName(name);
        }
        if (email != null) {
            prefsManager.saveUserEmail(email);
        }
        prefsManager.setLoggedIn(true);
    }

    private void navigateToMain() {
        fetchAndStoreFcmToken();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void fetchAndStoreFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        firebaseHelper.updateFcmToken(token);
                    }
                });
    }

    private void showForgotPasswordDialog() {
        String email = emailEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, R.string.error_enter_valid_email_reset, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        firebaseHelper.getAuth().sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    setLoadingState(false);
                    Toast.makeText(this, R.string.password_reset_email_sent, Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, R.string.error_sending_reset_email, Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
        registerTextView.setEnabled(!isLoading);
        forgotPasswordTextView.setEnabled(!isLoading);
    }

    private String getAuthErrorMessage(String firebaseError) {
        if (firebaseError == null) {
            return getString(R.string.error_login_failed);
        }

        if (firebaseError.contains("no user record") || firebaseError.contains("user not found")) {
            return getString(R.string.error_user_not_found);
        } else if (firebaseError.contains("password is invalid") || firebaseError.contains("wrong-password")) {
            return getString(R.string.error_wrong_password);
        } else if (firebaseError.contains("network error")) {
            return getString(R.string.error_network);
        } else if (firebaseError.contains("too many requests")) {
            return getString(R.string.error_too_many_requests);
        } else {
            return getString(R.string.error_login_failed);
        }
    }
}
