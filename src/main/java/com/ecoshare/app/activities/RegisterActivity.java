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
import com.ecoshare.app.models.User;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.NetworkUtil;
import com.ecoshare.app.utils.PrefsManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private MaterialButton registerButton;
    private TextView loginTextView;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        initializeHelpers();
        setupListeners();
    }

    private void initializeViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsManager = PrefsManager.getInstance();
    }

    private void setupListeners() {
        registerButton.setOnClickListener(v -> validateAndRegister());
        
        loginTextView.setOnClickListener(v -> {
            finish();
        });
    }

    private void validateAndRegister() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        nameInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.error_name_required));
            nameEditText.requestFocus();
            return;
        }

        if (name.length() < 2) {
            nameInputLayout.setError(getString(R.string.error_name_too_short));
            nameEditText.requestFocus();
            return;
        }

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

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.setError(getString(R.string.error_confirm_password_required));
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError(getString(R.string.error_passwords_dont_match));
            confirmPasswordEditText.requestFocus();
            return;
        }

        performRegistration(name, email, password);
    }

    private void performRegistration(String name, String email, String password) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }
        
        setLoadingState(true);

        FirebaseAuth auth = firebaseHelper.getAuth();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        createUserDocument(firebaseUser.getUid(), name, email);
                    }
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    String errorMessage = getAuthErrorMessage(e.getMessage());
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    private void createUserDocument(String userId, String name, String email) {
        User user = new User(userId, name, email);

        firebaseHelper.getUsersCollection()
                .document(userId)
                .set(user)
                .addOnSuccessListener(unused -> {
                    saveUserDataToPrefs(userId, name, email);
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    firebaseHelper.getAuth().getCurrentUser().delete();
                    Toast.makeText(this, R.string.error_creating_account, Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserDataToPrefs(String userId, String name, String email) {
        prefsManager.saveUserId(userId);
        prefsManager.saveUserName(name);
        prefsManager.saveUserEmail(email);
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

    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!isLoading);
        nameEditText.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
        confirmPasswordEditText.setEnabled(!isLoading);
        loginTextView.setEnabled(!isLoading);
    }

    private String getAuthErrorMessage(String firebaseError) {
        if (firebaseError == null) {
            return getString(R.string.error_registration_failed);
        }

        if (firebaseError.contains("email address is already in use") || firebaseError.contains("email-already-in-use")) {
            return getString(R.string.error_email_already_exists);
        } else if (firebaseError.contains("network error")) {
            return getString(R.string.error_network);
        } else if (firebaseError.contains("weak password")) {
            return getString(R.string.error_weak_password);
        } else {
            return getString(R.string.error_registration_failed);
        }
    }
}
