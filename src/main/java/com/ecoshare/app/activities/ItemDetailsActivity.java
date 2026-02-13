package com.ecoshare.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.ecoshare.app.R;
import com.ecoshare.app.adapters.ImagePagerAdapter;
import com.ecoshare.app.models.Auction;
import com.ecoshare.app.models.Item;
import com.ecoshare.app.utils.BadgeManager;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemDetailsActivity extends AppCompatActivity {
    
    private MaterialToolbar toolbar;
    private ViewPager2 imageViewPager;
    private TabLayout imageIndicator;
    private TextView itemTitleTextView;
    private Chip statusChip;
    private Chip categoryChip;
    private Chip typeChip;
    private Chip conditionChip;
    private TextView itemDescriptionTextView;
    private CircleImageView ownerProfileImageView;
    private TextView ownerNameTextView;
    private TextView timeTextView;
    private LinearLayout exchangeInfoLayout;
    private TextView exchangeForTextView;
    private LinearLayout auctionInfoLayout;
    private TextView startingPriceTextView;
    private LinearLayout actionButtonsLayout;
    private MaterialButton primaryActionButton;
    private MaterialButton secondaryActionButton;
    private ProgressBar progressBar;
    
    private FirebaseHelper firebaseHelper;
    private String itemId;
    private Item currentItem;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);
        
        itemId = getIntent().getStringExtra("itemId");
        if (itemId == null) {
            Toast.makeText(this, "Invalid item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupListeners();
        loadItemDetails();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        imageViewPager = findViewById(R.id.imageViewPager);
        imageIndicator = findViewById(R.id.imageIndicator);
        itemTitleTextView = findViewById(R.id.itemTitleTextView);
        statusChip = findViewById(R.id.statusChip);
        categoryChip = findViewById(R.id.categoryChip);
        typeChip = findViewById(R.id.typeChip);
        conditionChip = findViewById(R.id.conditionChip);
        itemDescriptionTextView = findViewById(R.id.itemDescriptionTextView);
        ownerProfileImageView = findViewById(R.id.ownerProfileImageView);
        ownerNameTextView = findViewById(R.id.ownerNameTextView);
        timeTextView = findViewById(R.id.timeTextView);
        exchangeInfoLayout = findViewById(R.id.exchangeInfoLayout);
        exchangeForTextView = findViewById(R.id.exchangeForTextView);
        auctionInfoLayout = findViewById(R.id.auctionInfoLayout);
        startingPriceTextView = findViewById(R.id.startingPriceTextView);
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout);
        primaryActionButton = findViewById(R.id.primaryActionButton);
        secondaryActionButton = findViewById(R.id.secondaryActionButton);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupListeners() {
        primaryActionButton.setOnClickListener(v -> {
            if (currentItem != null) {
                if (Constants.ITEM_TYPE_AUCTION.equals(currentItem.getType())) {
                    handleAuctionAction();
                } else {
                    Intent intent = new Intent(this, ChatRoomActivity.class);
                    intent.putExtra("otherParticipantId", currentItem.getOwnerId());
                    intent.putExtra("otherParticipantName", currentItem.getOwnerName());
                    startActivity(intent);
                }
            }
        });
        
        secondaryActionButton.setOnClickListener(v -> {
            if (currentItem != null) {
                String currentUserId = firebaseHelper.getCurrentUserId();
                if (currentUserId != null && currentUserId.equals(currentItem.getOwnerId()) 
                    && !Constants.ITEM_TYPE_AUCTION.equals(currentItem.getType())) {
                    markAsCompleted();
                } else {
                    shareItem();
                }
            }
        });
    }

    private void markAsCompleted() {
        showLoading(true);
        firebaseHelper.getItemsCollection().document(itemId)
            .update("status", Constants.ITEM_STATUS_COMPLETED)
            .addOnSuccessListener(aVoid -> {
                if (Constants.ITEM_TYPE_DONATE.equals(currentItem.getType())) {
                    incrementDonationCount();
                } else {
                    showLoading(false);
                    currentItem.setStatus(Constants.ITEM_STATUS_COMPLETED);
                    displayItemDetails();
                    Toast.makeText(this, "Item marked as completed!", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void incrementDonationCount() {
        String userId = firebaseHelper.getCurrentUserId();
        firebaseHelper.getUsersCollection().document(userId)
            .update("itemsDonated", com.google.firebase.firestore.FieldValue.increment(1))
            .addOnSuccessListener(aVoid -> {
                // Check for badges
                firebaseHelper.getUsersCollection().document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        showLoading(false);
                        int count = documentSnapshot.getLong("itemsDonated").intValue();
                        BadgeManager.getInstance().checkAndAwardDonationBadges(userId, count);
                        
                        currentItem.setStatus(Constants.ITEM_STATUS_COMPLETED);
                        displayItemDetails();
                        Toast.makeText(this, "Item marked as donated! Great job!", Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error updating stats", Toast.LENGTH_SHORT).show();
            });
    }

    private void handleAuctionAction() {
        String currentUserId = firebaseHelper.getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(currentItem.getOwnerId());

        if (currentItem.getAuctionId() == null) {
            if (isOwner) {
                startNewAuction();
            } else {
                Toast.makeText(this, "Auction hasn't started yet", Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent intent = new Intent(this, LiveAuctionActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_AUCTION_ID, currentItem.getAuctionId());
            intent.putExtra(Constants.INTENT_EXTRA_ITEM_ID, currentItem.getItemId());
            intent.putExtra("itemTitle", currentItem.getTitle());
            intent.putExtra("ownerId", currentItem.getOwnerId());
            startActivity(intent);
        }
    }

    private void startNewAuction() {
        showLoading(true);
        String auctionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (24 * 60 * 60 * 1000); // 24 hours from now

        Auction auction = new Auction(auctionId, currentItem.getItemId(), 
                currentItem.getStartingPrice(), startTime, endTime);

        firebaseHelper.getAuctionReference(auctionId).setValue(auction)
            .addOnSuccessListener(aVoid -> {
                // Update item with auctionId
                firebaseHelper.getItemsCollection().document(itemId)
                    .update("auctionId", auctionId, "status", Constants.ITEM_STATUS_IN_AUCTION)
                    .addOnSuccessListener(aVoid2 -> {
                        showLoading(false);
                        currentItem.setAuctionId(auctionId);
                        currentItem.setStatus(Constants.ITEM_STATUS_IN_AUCTION);
                        displayItemDetails();
                        Toast.makeText(this, "Auction started!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        Toast.makeText(this, "Failed to update item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Failed to create auction: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void loadItemDetails() {
        showLoading(true);
        
        firebaseHelper.getFirestore()
            .collection("items")
            .document(itemId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                showLoading(false);
                
                if (documentSnapshot.exists()) {
                    currentItem = documentSnapshot.toObject(Item.class);
                    if (currentItem != null) {
                        currentItem.setItemId(documentSnapshot.getId());
                        displayItemDetails();
                    }
                } else {
                    Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Error loading item: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                finish();
            });
    }
    
    private void displayItemDetails() {
        itemTitleTextView.setText(currentItem.getTitle());
        itemDescriptionTextView.setText(currentItem.getDescription());
        categoryChip.setText(currentItem.getCategory());
        conditionChip.setText(currentItem.getCondition());
        ownerNameTextView.setText(currentItem.getOwnerName());
        
        if (currentItem.getType() != null) {
            typeChip.setText(capitalizeFirst(currentItem.getType()));
            setTypeChipColor(typeChip, currentItem.getType());
        }
        
        if (currentItem.getStatus() != null) {
            statusChip.setText(capitalizeFirst(currentItem.getStatus()));
            setStatusChipColor(statusChip, currentItem.getStatus());
        }
        
        if (currentItem.getImageUrls() != null && !currentItem.getImageUrls().isEmpty()) {
            ImagePagerAdapter adapter = new ImagePagerAdapter(this, currentItem.getImageUrls());
            imageViewPager.setAdapter(adapter);
            
            new TabLayoutMediator(imageIndicator, imageViewPager, (tab, position) -> {
            }).attach();
        }
        
        CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
            currentItem.getCreatedTimestamp(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        );
        timeTextView.setText("Posted " + timeAgo);
        
        loadOwnerProfile();
        
        if (Constants.ITEM_TYPE_EXCHANGE.equals(currentItem.getType())) {
            exchangeInfoLayout.setVisibility(View.VISIBLE);
            exchangeForTextView.setText(currentItem.getExchangeFor());
            primaryActionButton.setText(R.string.propose_exchange);
        } else if (Constants.ITEM_TYPE_AUCTION.equals(currentItem.getType())) {
            auctionInfoLayout.setVisibility(View.VISIBLE);
            startingPriceTextView.setText("Starting Price: $" + 
                String.format("%.2f", currentItem.getStartingPrice()));
            
            if (currentItem.getAuctionId() == null) {
                primaryActionButton.setText(R.string.start_auction);
            } else {
                primaryActionButton.setText(R.string.view_auction);
            }
        } else {
            primaryActionButton.setText(R.string.contact_owner);
        }
        
        String currentUserId = firebaseHelper.getCurrentUserId();
        boolean isOwner = currentUserId != null && currentUserId.equals(currentItem.getOwnerId());
        
        if (isOwner) {
            if (Constants.ITEM_TYPE_AUCTION.equals(currentItem.getType())) {
                actionButtonsLayout.setVisibility(View.VISIBLE);
                if (currentItem.getAuctionId() != null) {
                    primaryActionButton.setText(R.string.view_auction);
                } else {
                    primaryActionButton.setText(R.string.start_auction);
                }
                secondaryActionButton.setText(R.string.share);
            } else {
                actionButtonsLayout.setVisibility(View.VISIBLE);
                primaryActionButton.setVisibility(View.GONE);
                secondaryActionButton.setText(Constants.ITEM_TYPE_DONATE.equals(currentItem.getType()) ? 
                        R.string.mark_as_donated : R.string.mark_as_completed);
                
                if (Constants.ITEM_STATUS_COMPLETED.equals(currentItem.getStatus())) {
                    actionButtonsLayout.setVisibility(View.GONE);
                }
            }
        } else {
            actionButtonsLayout.setVisibility(View.VISIBLE);
            primaryActionButton.setVisibility(View.VISIBLE);
            secondaryActionButton.setText(R.string.share);
        }
    }
    
    private void loadOwnerProfile() {
        if (currentItem.getOwnerId() != null) {
            firebaseHelper.getFirestore()
                .collection("users")
                .document(currentItem.getOwnerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_notification)
                                .into(ownerProfileImageView);
                        }
                    }
                });
        }
    }
    
    private void shareItem() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentItem.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            "Check out this item on EcoShare: " + currentItem.getTitle() + 
            "\n\n" + currentItem.getDescription());
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
    
    private void setTypeChipColor(Chip chip, String type) {
        int color;
        if (Constants.ITEM_TYPE_DONATE.equalsIgnoreCase(type)) {
            color = Color.parseColor("#4CAF50");
        } else if (Constants.ITEM_TYPE_EXCHANGE.equalsIgnoreCase(type)) {
            color = Color.parseColor("#2196F3");
        } else if (Constants.ITEM_TYPE_AUCTION.equalsIgnoreCase(type)) {
            color = Color.parseColor("#FF9800");
        } else {
            color = Color.parseColor("#9E9E9E");
        }
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
        chip.setTextColor(Color.WHITE);
    }
    
    private void setStatusChipColor(Chip chip, String status) {
        int color;
        if (Constants.ITEM_STATUS_AVAILABLE.equalsIgnoreCase(status)) {
            color = Color.parseColor("#4CAF50");
        } else if (Constants.ITEM_STATUS_IN_AUCTION.equalsIgnoreCase(status) || 
                   Constants.ITEM_STATUS_AUCTIONED.equalsIgnoreCase(status)) {
            color = Color.parseColor("#FF9800");
        } else if (Constants.ITEM_STATUS_COMPLETED.equalsIgnoreCase(status)) {
            color = Color.parseColor("#2196F3");
        } else {
            color = Color.parseColor("#9E9E9E");
        }
        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
        chip.setTextColor(Color.WHITE);
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
