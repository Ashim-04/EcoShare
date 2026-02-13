package com.ecoshare.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.ecoshare.app.R;
import com.ecoshare.app.models.Item;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.GlideHelper;
import com.ecoshare.app.utils.NetworkUtil;
import com.ecoshare.app.utils.PrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private LinearLayout imagePreviewContainer;
    private MaterialCardView addImageCard;
    private TextInputEditText titleEditText;
    private TextInputEditText descriptionEditText;
    private AutoCompleteTextView categoryAutoComplete;
    private AutoCompleteTextView conditionAutoComplete;
    private RadioGroup typeRadioGroup;
    private RadioButton donateRadioButton;
    private RadioButton exchangeRadioButton;
    private RadioButton auctionRadioButton;
    private TextInputLayout exchangeForLayout;
    private TextInputEditText exchangeForEditText;
    private TextInputLayout startingPriceLayout;
    private TextInputEditText startingPriceEditText;
    private MaterialButton submitButton;
    private ProgressBar progressBar;
    
    private FirebaseHelper firebaseHelper;
    private PrefsManager prefsManager;
    private List<Uri> selectedImageUris;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        
        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupDropdowns();
        setupImagePicker();
        setupListeners();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        imagePreviewContainer = findViewById(R.id.imagePreviewContainer);
        addImageCard = findViewById(R.id.addImageCard);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryAutoComplete = findViewById(R.id.categoryAutoComplete);
        conditionAutoComplete = findViewById(R.id.conditionAutoComplete);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        donateRadioButton = findViewById(R.id.donateRadioButton);
        exchangeRadioButton = findViewById(R.id.exchangeRadioButton);
        auctionRadioButton = findViewById(R.id.auctionRadioButton);
        exchangeForLayout = findViewById(R.id.exchangeForLayout);
        exchangeForEditText = findViewById(R.id.exchangeForEditText);
        startingPriceLayout = findViewById(R.id.startingPriceLayout);
        startingPriceEditText = findViewById(R.id.startingPriceEditText);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsManager = PrefsManager.getInstance();
        selectedImageUris = new ArrayList<>();
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupDropdowns() {
        String[] categories = getResources().getStringArray(R.array.item_categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(categoryAdapter);
        
        String[] conditions = getResources().getStringArray(R.array.item_conditions);
        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, conditions);
        conditionAutoComplete.setAdapter(conditionAdapter);
    }
    
    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUris.add(uri);
                    addImagePreview(uri);
                }
            }
        );
        
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    pickImageLauncher.launch("image/*");
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void setupListeners() {
        addImageCard.setOnClickListener(v -> {
            if (selectedImageUris.size() >= 5) {
                Toast.makeText(this, "Maximum 5 images allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                pickImageLauncher.launch("image/*");
            }
        });
        
        typeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.exchangeRadioButton) {
                exchangeForLayout.setVisibility(View.VISIBLE);
                startingPriceLayout.setVisibility(View.GONE);
            } else if (checkedId == R.id.auctionRadioButton) {
                exchangeForLayout.setVisibility(View.GONE);
                startingPriceLayout.setVisibility(View.VISIBLE);
            } else {
                exchangeForLayout.setVisibility(View.GONE);
                startingPriceLayout.setVisibility(View.GONE);
            }
        });
        
        submitButton.setOnClickListener(v -> validateAndSubmit());
    }
    
    private void addImagePreview(Uri imageUri) {
        ImageView imageView = new ImageView(this);
        int size = (int) (120 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(8, 8, 8, 8);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        GlideHelper.loadImageWithCenterCrop(this, imageUri.toString(), imageView);
        
        imageView.setOnClickListener(v -> {
            selectedImageUris.remove(imageUri);
            imagePreviewContainer.removeView(imageView);
        });
        
        int insertPosition = imagePreviewContainer.getChildCount() - 1;
        imagePreviewContainer.addView(imageView, insertPosition);
    }
    
    private void validateAndSubmit() {
        String title = titleEditText.getText() != null ? titleEditText.getText().toString().trim() : "";
        String description = descriptionEditText.getText() != null ? descriptionEditText.getText().toString().trim() : "";
        String category = categoryAutoComplete.getText().toString().trim();
        String condition = conditionAutoComplete.getText().toString().trim();
        
        if (title.isEmpty()) {
            titleEditText.setError("Title is required");
            titleEditText.requestFocus();
            return;
        }
        
        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            descriptionEditText.requestFocus();
            return;
        }
        
        if (category.isEmpty()) {
            categoryAutoComplete.setError("Category is required");
            categoryAutoComplete.requestFocus();
            return;
        }
        
        if (condition.isEmpty()) {
            conditionAutoComplete.setError("Condition is required");
            conditionAutoComplete.requestFocus();
            return;
        }
        
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "Please add at least one image", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String type;
        int checkedId = typeRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.exchangeRadioButton) {
            type = Constants.ITEM_TYPE_EXCHANGE;
            String exchangeFor = exchangeForEditText.getText() != null ? 
                exchangeForEditText.getText().toString().trim() : "";
            if (exchangeFor.isEmpty()) {
                exchangeForEditText.setError("Please specify what you want in exchange");
                exchangeForEditText.requestFocus();
                return;
            }
        } else if (checkedId == R.id.auctionRadioButton) {
            type = Constants.ITEM_TYPE_AUCTION;
            String priceStr = startingPriceEditText.getText() != null ? 
                startingPriceEditText.getText().toString().trim() : "";
            if (priceStr.isEmpty()) {
                startingPriceEditText.setError("Starting price is required");
                startingPriceEditText.requestFocus();
                return;
            }
        } else {
            type = Constants.ITEM_TYPE_DONATE;
        }
        
        createItem(title, description, category, condition, type);
    }
    
    private void createItem(String title, String description, String category, 
                           String condition, String type) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, R.string.error_network, Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser == null) {
            showLoading(false);
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = currentUser.getUid();
        String userName = prefsManager.getUserName();
        if (userName == null || userName.isEmpty()) {
            userName = currentUser.getEmail();
        }
        
        Item item = new Item(title, description, category, condition, type, userId, userName);
        item.setStatus(Constants.ITEM_STATUS_AVAILABLE);
        
        if (type.equals(Constants.ITEM_TYPE_EXCHANGE)) {
            String exchangeFor = exchangeForEditText.getText() != null ? 
                exchangeForEditText.getText().toString().trim() : "";
            item.setExchangeFor(exchangeFor);
        } else if (type.equals(Constants.ITEM_TYPE_AUCTION)) {
            String priceStr = startingPriceEditText.getText() != null ? 
                startingPriceEditText.getText().toString().trim() : "0";
            try {
                double price = Double.parseDouble(priceStr);
                item.setStartingPrice(price);
            } catch (NumberFormatException e) {
                item.setStartingPrice(0);
            }
        }
        
        DocumentReference itemRef = firebaseHelper.getFirestore().collection("items").document();
        String itemId = itemRef.getId();
        item.setItemId(itemId);
        
        uploadImagesAndSaveItem(item, itemRef);
    }
    
    private void uploadImagesAndSaveItem(Item item, DocumentReference itemRef) {
        List<String> imageUrls = new ArrayList<>();
        int[] uploadedCount = {0};
        int totalImages = selectedImageUris.size();
        
        for (int i = 0; i < selectedImageUris.size(); i++) {
            Uri imageUri = selectedImageUris.get(i);
            String imageName = UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = firebaseHelper.getStorage()
                .getReference()
                .child("item_images")
                .child(item.getItemId())
                .child(imageName);
            
            imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrls.add(uri.toString());
                        uploadedCount[0]++;
                        
                        if (uploadedCount[0] == totalImages) {
                            item.setImageUrls(imageUrls);
                            saveItemToFirestore(item, itemRef);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Failed to upload image: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                });
        }
    }
    
    private void saveItemToFirestore(Item item, DocumentReference itemRef) {
        itemRef.set(item)
            .addOnSuccessListener(aVoid -> {
                updateUserStats();
                showLoading(false);
                Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Failed to add item: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateUserStats() {
        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser != null) {
            firebaseHelper.getFirestore()
                .collection("users")
                .document(currentUser.getUid())
                .update("itemsListed", com.google.firebase.firestore.FieldValue.increment(1));
        }
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!show);
    }
}
