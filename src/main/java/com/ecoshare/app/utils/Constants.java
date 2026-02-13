package com.ecoshare.app.utils;

public class Constants {

    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_ITEMS = "items";
    public static final String COLLECTION_NOTIFICATIONS = "notifications";
    public static final String COLLECTION_CONVERSATIONS = "conversations";
    public static final String COLLECTION_CHATS = "conversations";
    public static final String COLLECTION_MESSAGES = "messages";

    public static final String RTDB_AUCTIONS = "auctions";
    public static final String RTDB_ACTIVE_BIDS = "active_bids";

    public static final String STORAGE_PROFILE_IMAGES = "profile_images";
    public static final String STORAGE_ITEM_IMAGES = "item_images";

    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_FCM_TOKEN = "fcm_token";

    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String PREF_AUCTION_ALERTS = "auction_alerts";
    public static final String PREF_BADGE_NOTIFICATIONS = "badge_notifications";

    public static final String ITEM_TYPE_DONATE = "donate";
    public static final String ITEM_TYPE_EXCHANGE = "exchange";
    public static final String ITEM_TYPE_AUCTION = "auction";

    public static final String ITEM_STATUS_AVAILABLE = "available";
    public static final String ITEM_STATUS_IN_AUCTION = "in_auction";
    public static final String ITEM_STATUS_AUCTIONED = "auctioned";
    public static final String ITEM_STATUS_COMPLETED = "completed";

    public static final String CATEGORY_ELECTRONICS = "Electronics";
    public static final String CATEGORY_CLOTHING = "Clothing";
    public static final String CATEGORY_FURNITURE = "Furniture";
    public static final String CATEGORY_BOOKS = "Books";
    public static final String CATEGORY_TOYS = "Toys";
    public static final String CATEGORY_HOME_GARDEN = "Home & Garden";
    public static final String CATEGORY_SPORTS = "Sports";
    public static final String CATEGORY_OTHER = "Other";

    public static final String CONDITION_NEW = "New";
    public static final String CONDITION_LIKE_NEW = "Like New";
    public static final String CONDITION_GOOD = "Good";
    public static final String CONDITION_FAIR = "Fair";
    public static final String CONDITION_POOR = "Poor";

    public static final String BADGE_FIRST_DONATION = "first_donation";
    public static final String BADGE_5_DONATIONS = "5_donations";
    public static final String BADGE_10_DONATIONS = "10_donations";
    public static final String BADGE_25_DONATIONS = "25_donations";
    public static final String BADGE_50_DONATIONS = "50_donations";
    public static final String BADGE_ECO_WARRIOR = "eco_warrior";
    public static final String BADGE_AUCTION_MASTER = "auction_master";

    public static final String NOTIFICATION_TYPE_OUTBID = "outbid";
    public static final String NOTIFICATION_TYPE_AUCTION_ENDING = "auction_ending";
    public static final String NOTIFICATION_TYPE_AUCTION_WON = "auction_won";
    public static final String NOTIFICATION_TYPE_AUCTION_LOST = "auction_lost";
    public static final String NOTIFICATION_TYPE_BADGE_EARNED = "badge_earned";
    public static final String NOTIFICATION_TYPE_ITEM_ACTIVITY = "item_activity";
    public static final String NOTIFICATION_TYPE_MESSAGE = "message";

    public static final String NOTIFICATION_CHANNEL_ID = "ecoshare_notifications";
    public static final String NOTIFICATION_CHANNEL_NAME = "EcoShare Notifications";
    public static final String NOTIFICATION_CHANNEL_AUCTION = "auction_notifications";
    public static final String NOTIFICATION_CHANNEL_BADGES = "badge_notifications";

    public static final String INTENT_EXTRA_ITEM_ID = "item_id";
    public static final String INTENT_EXTRA_AUCTION_ID = "auction_id";
    public static final String INTENT_EXTRA_USER_ID = "user_id";
    public static final String INTENT_EXTRA_NOTIFICATION_ID = "notification_id";
    public static final String INTENT_EXTRA_CHAT_ID = "chat_id";

    public static final int MAX_IMAGE_COUNT = 5;
    public static final int IMAGE_QUALITY = 80;
    public static final int MAX_IMAGE_WIDTH = 1080;
    public static final int MAX_IMAGE_HEIGHT = 1080;

    public static final long AUCTION_WARNING_TIME = 3600000;
    
    public static final double CO2_PER_ITEM_KG = 25.0;

    public static final int REQUEST_CODE_PICK_IMAGE = 1001;
    public static final int REQUEST_CODE_CAMERA = 1002;
    public static final int REQUEST_CODE_PERMISSIONS = 1003;

    private Constants() {
    }
}
