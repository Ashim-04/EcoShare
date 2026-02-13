package com.ecoshare.app.models;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {
    private String userId;
    private String name;
    private String email;
    private String profileImageUrl;
    private int itemsListed;
    private int itemsDonated;
    private List<String> badges;
    private long joinedTimestamp;
    private String fcmToken;

    public User() {
        this.badges = new ArrayList<>();
        this.itemsListed = 0;
        this.itemsDonated = 0;
        this.joinedTimestamp = System.currentTimeMillis();
    }

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.badges = new ArrayList<>();
        this.itemsListed = 0;
        this.itemsDonated = 0;
        this.joinedTimestamp = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public int getItemsListed() {
        return itemsListed;
    }

    public void setItemsListed(int itemsListed) {
        this.itemsListed = itemsListed;
    }

    public int getItemsDonated() {
        return itemsDonated;
    }

    public void setItemsDonated(int itemsDonated) {
        this.itemsDonated = itemsDonated;
    }

    public List<String> getBadges() {
        return badges;
    }

    public void setBadges(List<String> badges) {
        this.badges = badges;
    }

    public long getJoinedTimestamp() {
        return joinedTimestamp;
    }

    public void setJoinedTimestamp(long joinedTimestamp) {
        this.joinedTimestamp = joinedTimestamp;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void incrementItemsListed() {
        this.itemsListed++;
    }

    public void incrementItemsDonated() {
        this.itemsDonated++;
    }

    public void addBadge(String badge) {
        if (!badges.contains(badge)) {
            badges.add(badge);
        }
    }

    @Exclude
    public boolean hasBadge(String badge) {
        return badges.contains(badge);
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("name", name);
        map.put("email", email);
        map.put("profileImageUrl", profileImageUrl);
        map.put("itemsListed", itemsListed);
        map.put("itemsDonated", itemsDonated);
        map.put("badges", badges);
        map.put("joinedTimestamp", joinedTimestamp);
        map.put("fcmToken", fcmToken);
        return map;
    }
}
