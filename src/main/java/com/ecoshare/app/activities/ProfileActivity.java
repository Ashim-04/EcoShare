package com.ecoshare.app.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ecoshare.app.R;
import com.ecoshare.app.adapters.BadgeAdapter;
import com.ecoshare.app.models.Badge;
import com.ecoshare.app.models.User;
import com.ecoshare.app.utils.BadgeManager;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.PrefsManager;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    
    private CircleImageView profileImage;
    private ImageView editImageButton;
    private TextView userName;
    private TextView userEmail;
    private TextView itemsListedCount;
    private TextView itemsDonatedCount;
    private TextView joinedDate;
    private TextView noBadgesText;
    private RecyclerView badgesRecyclerView;
    private ProgressBar progressBar;
    
    private BadgeAdapter badgeAdapter;
    private FirebaseHelper firebaseHelper;
    private PrefsManager prefsManager;
    private BadgeManager badgeManager;
    
    private String currentUserId;
    private Uri selectedImageUri;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        initializeComponents();
        setupToolbar();
        setupRecyclerView();
        setupImagePickers();
        setupClickListeners();
        loadUserProfile();
    }

    private void initializeComponents() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsManager = PrefsManager.getInstance(this);
        badgeManager = BadgeManager.getInstance();
        
        currentUserId = prefsManager.getString(Constants.PREF_USER_ID, "");
        
        profileImage = findViewById(R.id.profileImage);
        editImageButton = findViewById(R.id.editImageButton);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        itemsListedCount = findViewById(R.id.itemsListedCount);
        itemsDonatedCount = findViewById(R.id.itemsDonatedCount);
        joinedDate = findViewById(R.id.joinedDate);
        noBadgesText = findViewById(R.id.noBadgesText);
        badgesRecyclerView = findViewById(R.id.badgesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.profile);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        badgeAdapter = new BadgeAdapter(this);
        badgesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        badgesRecyclerView.setAdapter(badgeAdapter);
    }

    private void setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadProfileImage(selectedImageUri);
                    }
                }
            }
        );

        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        uploadProfileImage(selectedImageUri);
                    }
                }
            }
        );
    }

    private void setupClickListeners() {
        editImageButton.setOnClickListener(v -> showImageSourceDialog());
    }

    private void loadUserProfile() {
        if (currentUserId.isEmpty()) {
            Toast.makeText(this, R.string.error_user_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        
        firebaseHelper.getUsersCollection()
            .document(currentUserId)
            .addSnapshotListener((documentSnapshot, error) -> {
                progressBar.setVisibility(View.GONE);
                
                if (error != null) {
                    Log.e(TAG, "Error loading profile: " + error.getMessage());
                    Toast.makeText(this, R.string.error_loading_user_data, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        displayUserData(user);
                    }
                }
            });
    }

    private void displayUserData(User user) {
        userName.setText(user.getName());
        userEmail.setText(user.getEmail());
        itemsListedCount.setText(String.valueOf(user.getItemsListed()));
        itemsDonatedCount.setText(String.valueOf(user.getItemsDonated()));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String joinedDateStr = dateFormat.format(new Date(user.getJoinedTimestamp()));
        joinedDate.setText(joinedDateStr);
        
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.ic_notification)
                .error(R.drawable.ic_notification)
                .into(profileImage);
        }
        
        List<Badge> userBadges = badgeManager.getUserBadges(user);
        if (userBadges.isEmpty()) {
            noBadgesText.setVisibility(View.VISIBLE);
            badgesRecyclerView.setVisibility(View.GONE);
        } else {
            noBadgesText.setVisibility(View.GONE);
            badgesRecyclerView.setVisibility(View.VISIBLE);
            badgeAdapter.setBadges(userBadges);
        }
    }

    private void showImageSourceDialog() {
        String[] options = {getString(R.string.camera), getString(R.string.gallery)};
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.select_image_source)
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    openCamera();
                } else {
                    openGallery();
                }
            })
            .show();
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.CAMERA}, 
                    Constants.REQUEST_CODE_CAMERA);
                return;
            }
        }
        
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                    Constants.REQUEST_CODE_PICK_IMAGE);
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                    Constants.REQUEST_CODE_PICK_IMAGE);
                return;
            }
        }
        
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(galleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == Constants.REQUEST_CODE_CAMERA) {
                openCamera();
            } else if (requestCode == Constants.REQUEST_CODE_PICK_IMAGE) {
                openGallery();
            }
        } else {
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        
        String fileName = currentUserId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = firebaseHelper.getStorage()
            .getReference()
            .child(Constants.STORAGE_PROFILE_IMAGES)
            .child(currentUserId)
            .child(fileName);
        
        UploadTask uploadTask = imageRef.putFile(imageUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                updateProfileImageUrl(uri.toString());
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error getting download URL: " + e.getMessage());
                Toast.makeText(this, R.string.error_uploading_image, Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Log.e(TAG, "Error uploading image: " + e.getMessage());
            Toast.makeText(this, R.string.error_uploading_image, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfileImageUrl(String imageUrl) {
        firebaseHelper.getUsersCollection()
            .document(currentUserId)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, R.string.profile_image_updated, Toast.LENGTH_SHORT).show();
                
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_notification)
                    .error(R.drawable.ic_notification)
                    .into(profileImage);
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error updating profile image URL: " + e.getMessage());
                Toast.makeText(this, R.string.error_uploading_image, Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
