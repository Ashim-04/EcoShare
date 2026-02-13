package com.ecoshare.app.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ecoshare.app.models.Badge;
import com.ecoshare.app.models.Notification;
import com.ecoshare.app.models.User;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class BadgeManager {
    private static final String TAG = "BadgeManager";
    private static BadgeManager instance;
    
    private final FirebaseHelper firebaseHelper;
    private final List<Badge> allBadges;

    private BadgeManager() {
        this.firebaseHelper = FirebaseHelper.getInstance();
        this.allBadges = initializeBadges();
    }

    public static synchronized BadgeManager getInstance() {
        if (instance == null) {
            instance = new BadgeManager();
        }
        return instance;
    }

    private List<Badge> initializeBadges() {
        List<Badge> badges = new ArrayList<>();
        
        badges.add(new Badge(
            Constants.BADGE_FIRST_DONATION,
            "First Step",
            "Made your first donation",
            "ic_badge_first",
            1
        ));
        
        badges.add(new Badge(
            Constants.BADGE_5_DONATIONS,
            "Generous Soul",
            "Donated 5 items",
            "ic_badge_5",
            5
        ));
        
        badges.add(new Badge(
            Constants.BADGE_10_DONATIONS,
            "Giving Hero",
            "Donated 10 items",
            "ic_badge_10",
            10
        ));
        
        badges.add(new Badge(
            Constants.BADGE_25_DONATIONS,
            "Sustainability Champion",
            "Donated 25 items",
            "ic_badge_25",
            25
        ));
        
        badges.add(new Badge(
            Constants.BADGE_50_DONATIONS,
            "Eco Legend",
            "Donated 50 items",
            "ic_badge_50",
            50
        ));
        
        badges.add(new Badge(
            Constants.BADGE_ECO_WARRIOR,
            "Eco Warrior",
            "Made a significant environmental impact",
            "ic_badge_warrior",
            100
        ));
        
        badges.add(new Badge(
            Constants.BADGE_AUCTION_MASTER,
            "Auction Master",
            "Won 10 auctions",
            "ic_badge_auction",
            10
        ));
        
        return badges;
    }

    public List<Badge> getAllBadges() {
        return new ArrayList<>(allBadges);
    }

    public Badge getBadgeById(String badgeId) {
        for (Badge badge : allBadges) {
            if (badge.getBadgeId().equals(badgeId)) {
                return badge;
            }
        }
        return null;
    }

    /**
     * Check if user has earned any donation milestone badges
     * Called after item is marked as donated
     * 
     * @param userId User to check badges for
     * @param donationCount Current total donation count for user
     */
    public void checkAndAwardDonationBadges(String userId, int donationCount) {
        firebaseHelper.getUsersCollection()
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        // Check all donation milestone badges
                        checkDonationBadge(user, donationCount, Constants.BADGE_FIRST_DONATION, 1);
                        checkDonationBadge(user, donationCount, Constants.BADGE_5_DONATIONS, 5);
                        checkDonationBadge(user, donationCount, Constants.BADGE_10_DONATIONS, 10);
                        checkDonationBadge(user, donationCount, Constants.BADGE_25_DONATIONS, 25);
                        checkDonationBadge(user, donationCount, Constants.BADGE_50_DONATIONS, 50);
                        checkDonationBadge(user, donationCount, Constants.BADGE_ECO_WARRIOR, 100);
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error checking badges: " + e.getMessage()));
    }

    /**
     * Check if user qualifies for a specific donation badge
     * Awards badge if criteria met and not already earned
     */
    private void checkDonationBadge(User user, int donationCount, String badgeId, int requiredCount) {
        // Award badge if user reached threshold and doesn't already have it
        if (donationCount >= requiredCount && !user.hasBadge(badgeId)) {
            awardBadge(user.getUserId(), badgeId);
        }
    }

    public void checkAndAwardAuctionBadges(String userId, int winCount) {
        firebaseHelper.getUsersCollection()
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        if (winCount >= 10 && !user.hasBadge(Constants.BADGE_AUCTION_MASTER)) {
                            awardBadge(userId, Constants.BADGE_AUCTION_MASTER);
                        }
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error checking auction badges: " + e.getMessage()));
    }

    /**
     * Award a badge to a user
     * Updates user document in Firestore and sends notification
     * Prevents duplicate badge awards with hasBadge check
     * 
     * @param userId User to award badge to
     * @param badgeId Badge identifier from Constants
     */
    public void awardBadge(String userId, String badgeId) {
        DocumentReference userRef = firebaseHelper.getUsersCollection().document(userId);
        
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                // Double-check user doesn't already have badge to prevent duplicates
                if (user != null && !user.hasBadge(badgeId)) {
                    user.addBadge(badgeId);
                    
                    // Update user's badge list in Firestore
                    userRef.update("badges", user.getBadges())
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Badge awarded: " + badgeId);
                            // Send in-app and push notification about new badge
                            sendBadgeNotification(userId, badgeId);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Error awarding badge: " + e.getMessage()));
                }
            }
        });
    }

    /**
     * Send notification to user about newly earned badge
     * Creates notification in Firestore which triggers FCM push notification
     */
    private void sendBadgeNotification(String userId, String badgeId) {
        Badge badge = getBadgeById(badgeId);
        if (badge != null) {
            Notification notification = new Notification(
                userId,
                Constants.NOTIFICATION_TYPE_BADGE_EARNED,
                "New Badge Earned!",
                "You've earned the '" + badge.getName() + "' badge!"
            );
            notification.setBadgeId(badgeId);
            firebaseHelper.createNotification(notification);
            Log.d(TAG, "Badge notification created");
        }
    }

    public List<Badge> getUserBadges(@NonNull User user) {
        List<Badge> userBadges = new ArrayList<>();
        List<String> badgeIds = user.getBadges();
        
        if (badgeIds != null) {
            for (String badgeId : badgeIds) {
                Badge badge = getBadgeById(badgeId);
                if (badge != null) {
                    userBadges.add(badge);
                }
            }
        }
        
        return userBadges;
    }

    public int getBadgeProgress(User user, String badgeId) {
        Badge badge = getBadgeById(badgeId);
        if (badge == null) {
            return 0;
        }

        if (badgeId.equals(Constants.BADGE_AUCTION_MASTER)) {
            return 0;
        }

        return Math.min(user.getItemsDonated(), badge.getRequiredCount());
    }

    public boolean isBadgeEarned(User user, String badgeId) {
        return user.hasBadge(badgeId);
    }
}
