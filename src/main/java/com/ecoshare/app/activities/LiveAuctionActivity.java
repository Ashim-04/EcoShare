package com.ecoshare.app.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ecoshare.app.R;
import com.ecoshare.app.adapters.AuctionBiddersAdapter;
import com.ecoshare.app.models.Auction;
import com.ecoshare.app.models.Notification;
import com.ecoshare.app.utils.Constants;
import com.ecoshare.app.utils.FirebaseHelper;
import com.ecoshare.app.utils.PrefsManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LiveAuctionActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView itemTitleTextView;
    private TextView currentBidTextView;
    private TextView bidderNameTextView;
    private TextView timerTextView;
    private RecyclerView biddersRecyclerView;
    private TextInputEditText bidEditText;
    private MaterialButton placeBidButton;
    private ProgressBar progressBar;

    private FirebaseHelper firebaseHelper;
    private PrefsManager prefsManager;
    private String auctionId;
    private String itemId;
    private String itemTitle;
    private String ownerId;
    private DatabaseReference auctionRef;
    private ValueEventListener auctionListener;
    private Auction currentAuction;
    private AuctionBiddersAdapter adapter;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_auction);

        auctionId = getIntent().getStringExtra(Constants.INTENT_EXTRA_AUCTION_ID);
        itemId = getIntent().getStringExtra(Constants.INTENT_EXTRA_ITEM_ID);
        itemTitle = getIntent().getStringExtra("itemTitle");
        ownerId = getIntent().getStringExtra("ownerId");

        if (auctionId == null || itemId == null) {
            Toast.makeText(this, "Invalid auction", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        initializeHelpers();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        observeAuction();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        itemTitleTextView = findViewById(R.id.itemTitleTextView);
        currentBidTextView = findViewById(R.id.currentBidTextView);
        bidderNameTextView = findViewById(R.id.bidderNameTextView);
        timerTextView = findViewById(R.id.timerTextView);
        biddersRecyclerView = findViewById(R.id.biddersRecyclerView);
        bidEditText = findViewById(R.id.bidEditText);
        placeBidButton = findViewById(R.id.placeBidButton);
        progressBar = findViewById(R.id.progressBar);

        if (itemTitle != null) {
            itemTitleTextView.setText(itemTitle);
        }
    }

    private void initializeHelpers() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsManager = PrefsManager.getInstance();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AuctionBiddersAdapter(this, new ArrayList<>());
        biddersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        biddersRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        placeBidButton.setOnClickListener(v -> placeBid());
    }

    /**
     * Set up real-time listener for auction updates
     * Uses Firebase Realtime Database ValueEventListener to get instant updates
     * when any participant places a bid or when auction status changes
     */
    private void observeAuction() {
        showLoading(true);
        auctionRef = firebaseHelper.getAuctionReference(auctionId);
        auctionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showLoading(false);
                if (snapshot.exists()) {
                    currentAuction = snapshot.getValue(Auction.class);
                    if (currentAuction != null) {
                        updateUI();
                    }
                } else {
                    // Auction might not exist yet if just started
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                Toast.makeText(LiveAuctionActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        // Attach listener - will trigger immediately and on every update
        auctionRef.addValueEventListener(auctionListener);
    }

    private void updateUI() {
        currentBidTextView.setText(String.format("$%.2f", currentAuction.getCurrentBid()));
        if (currentAuction.getCurrentBidderName() != null) {
            bidderNameTextView.setText("Highest Bidder: " + currentAuction.getCurrentBidderName());
        } else {
            bidderNameTextView.setText("No bids yet");
        }

        adapter.updateBidders(currentAuction.getBiddersList());

        startTimer(currentAuction.getEndTime());

        if (!currentAuction.isActive() || currentAuction.hasEnded()) {
            placeBidButton.setEnabled(false);
            bidEditText.setEnabled(false);
            timerTextView.setText("Ended");
        }
    }

    /**
     * Start countdown timer for auction
     * Displays time remaining in HH:MM:SS format
     * Automatically handles auction end when timer reaches zero
     * 
     * @param endTime Unix timestamp (milliseconds) when auction ends
     */
    private void startTimer(long endTime) {
        // Cancel any existing timer to avoid memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Calculate time remaining based on server timestamp
        long timeRemaining = endTime - System.currentTimeMillis();
        if (timeRemaining <= 0) {
            timerTextView.setText("Ended");
            return;
        }

        // Create countdown timer that updates every second
        countDownTimer = new CountDownTimer(timeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Convert milliseconds to hours:minutes:seconds
                long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                timerTextView.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                // Auction time expired - disable bidding and process winner
                timerTextView.setText("Ended");
                placeBidButton.setEnabled(false);
                bidEditText.setEnabled(false);
                handleAuctionEnd();
            }
        }.start();
    }

    /**
     * Handle bid placement with validation and notification
     * Validates bid amount, updates Realtime Database, and notifies outbid user
     * Note: Uses last-write-wins strategy - no conflict resolution for concurrent bids
     */
    private void placeBid() {
        String bidStr = bidEditText.getText() != null ? bidEditText.getText().toString().trim() : "";
        if (bidStr.isEmpty()) {
            bidEditText.setError("Enter bid amount");
            return;
        }

        double bidAmount = Double.parseDouble(bidStr);
        if (currentAuction == null) return;

        // Validate: new bid must be higher than current bid
        if (bidAmount <= currentAuction.getCurrentBid()) {
            bidEditText.setError("Bid must be higher than current bid");
            return;
        }

        String userId = firebaseHelper.getCurrentUserId();
        String userName = prefsManager.getUserName();
        if (userName == null) userName = "User";

        // Store previous bidder info to send outbid notification
        String previousBidderId = currentAuction.getCurrentBidderId();
        double previousBidAmount = currentAuction.getCurrentBid();

        // Update auction object with new bid
        currentAuction.addBid(userId, userName, bidAmount);
        
        // Write to Realtime Database - triggers ValueEventListener for all viewers
        showLoading(true);
        auctionRef.setValue(currentAuction).addOnCompleteListener(task -> {
            showLoading(false);
            if (task.isSuccessful()) {
                bidEditText.setText("");
                Toast.makeText(this, "Bid placed successfully!", Toast.LENGTH_SHORT).show();
                
                // Send push notification to user who was just outbid
                if (previousBidderId != null && !previousBidderId.equals(userId)) {
                    sendOutbidNotification(previousBidderId, itemTitle != null ? itemTitle : "Item");
                }
            } else {
                Toast.makeText(this, "Failed to place bid", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendOutbidNotification(String userId, String itemName) {
        Notification notification = new Notification(
                userId,
                Constants.NOTIFICATION_TYPE_OUTBID,
                getString(R.string.notification_outbid_title),
                String.format(getString(R.string.notification_outbid_message), itemName)
        );
        notification.setItemId(itemId);
        notification.setAuctionId(auctionId);
        firebaseHelper.createNotification(notification);
    }

    /**
     * Handle automatic auction end when timer expires
     * Marks auction as inactive, determines winner, updates item status,
     * and sends notifications to all participants
     */
    private void handleAuctionEnd() {
        if (currentAuction != null && currentAuction.isActive()) {
            // Mark auction as ended to prevent further bids
            currentAuction.setActive(false);
            
            // Determine winner (highest bidder)
            String winnerId = null;
            if (currentAuction.getCurrentBidderId() != null) {
                winnerId = currentAuction.getCurrentBidderId();
                currentAuction.setWinnerId(winnerId);
                currentAuction.setWinnerName(currentAuction.getCurrentBidderName());
            }
            
            // Update auction in Realtime Database
            auctionRef.setValue(currentAuction);
            
            // Update item status in Firestore to mark as completed
            firebaseHelper.getItemsCollection().document(itemId)
                .update("status", Constants.ITEM_STATUS_COMPLETED);

            // Send notifications to all participants
            String name = itemTitle != null ? itemTitle : "Item";
            if (winnerId != null) {
                // Notify winner they won the auction
                sendWinnerNotification(winnerId, name);
                
                // Notify owner
                if (ownerId != null) {
                    sendOwnerNotification(ownerId, name, currentAuction.getWinnerName());
                }
            } else if (ownerId != null) {
                // Notify owner that auction ended without bids
                sendAuctionEndedNoBidsNotification(ownerId, name);
            }
        }
    }

    private void sendWinnerNotification(String userId, String itemName) {
        Notification notification = new Notification(
                userId,
                Constants.NOTIFICATION_TYPE_AUCTION_WON,
                getString(R.string.notification_auction_won_title),
                String.format(getString(R.string.notification_auction_won_message), itemName)
        );
        notification.setItemId(itemId);
        notification.setAuctionId(auctionId);
        firebaseHelper.createNotification(notification);
    }

    private void sendOwnerNotification(String userId, String itemName, String winnerName) {
        Notification notification = new Notification(
                userId,
                Constants.NOTIFICATION_TYPE_ITEM_ACTIVITY,
                "Auction Ended",
                String.format("The auction for %s has ended. Winner: %s", itemName, winnerName)
        );
        notification.setItemId(itemId);
        notification.setAuctionId(auctionId);
        firebaseHelper.createNotification(notification);
    }

    private void sendAuctionEndedNoBidsNotification(String userId, String itemName) {
        Notification notification = new Notification(
                userId,
                Constants.NOTIFICATION_TYPE_ITEM_ACTIVITY,
                "Auction Ended",
                String.format("The auction for %s has ended with no bids.", itemName)
        );
        notification.setItemId(itemId);
        notification.setAuctionId(auctionId);
        firebaseHelper.createNotification(notification);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (auctionRef != null && auctionListener != null) {
            auctionRef.removeEventListener(auctionListener);
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
