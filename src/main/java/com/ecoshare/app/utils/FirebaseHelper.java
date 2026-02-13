package com.ecoshare.app.utils;

import com.ecoshare.app.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FirebaseDatabase realtimeDatabase;
    private final FirebaseStorage storage;

    private FirebaseHelper() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.realtimeDatabase = FirebaseDatabase.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseDatabase getRealtimeDatabase() {
        return realtimeDatabase;
    }

    public FirebaseStorage getStorage() {
        return storage;
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public CollectionReference getUsersCollection() {
        return firestore.collection(Constants.COLLECTION_USERS);
    }

    public CollectionReference getItemsCollection() {
        return firestore.collection(Constants.COLLECTION_ITEMS);
    }

    public CollectionReference getNotificationsCollection() {
        return firestore.collection(Constants.COLLECTION_NOTIFICATIONS);
    }

    public CollectionReference getChatsCollection() {
        return firestore.collection(Constants.COLLECTION_CHATS);
    }

    public DatabaseReference getAuctionsReference() {
        return realtimeDatabase.getReference(Constants.RTDB_AUCTIONS);
    }

    public DatabaseReference getAuctionReference(String auctionId) {
        return getAuctionsReference().child(auctionId);
    }

    public StorageReference getProfileImagesReference() {
        return storage.getReference().child(Constants.STORAGE_PROFILE_IMAGES);
    }

    public StorageReference getItemImagesReference() {
        return storage.getReference().child(Constants.STORAGE_ITEM_IMAGES);
    }

    public StorageReference getProfileImageReference(String userId) {
        return getProfileImagesReference().child(userId + ".jpg");
    }

    public StorageReference getItemImageReference(String itemId, int imageIndex) {
        return getItemImagesReference().child(itemId).child("image_" + imageIndex + ".jpg");
    }

    public void signOut() {
        auth.signOut();
    }

    public void updateFcmToken(String token) {
        if (isUserLoggedIn()) {
            getUsersCollection()
                    .document(getCurrentUserId())
                    .update("fcmToken", token);
        }
    }

    public void createNotification(Notification notification) {
        getNotificationsCollection()
                .add(notification.toMap())
                .addOnSuccessListener(documentReference -> {
                    notification.setNotificationId(documentReference.getId());
                    documentReference.update("notificationId", documentReference.getId());
                });
    }
}
