package com.ecoshare.app.models;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item {
    private String itemId;
    private String title;
    private String description;
    private String category;
    private String condition;
    private String type;
    private String status;
    private String ownerId;
    private String ownerName;
    private List<String> imageUrls;
    private long createdTimestamp;
    private long updatedTimestamp;
    private String auctionId;
    private double startingPrice;
    private String exchangeFor;

    public Item() {
        this.imageUrls = new ArrayList<>();
        this.createdTimestamp = System.currentTimeMillis();
        this.updatedTimestamp = System.currentTimeMillis();
    }

    public Item(String title, String description, String category, String condition, String type, String ownerId, String ownerName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.condition = condition;
        this.type = type;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.imageUrls = new ArrayList<>();
        this.createdTimestamp = System.currentTimeMillis();
        this.updatedTimestamp = System.currentTimeMillis();
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(long updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public String getExchangeFor() {
        return exchangeFor;
    }

    public void setExchangeFor(String exchangeFor) {
        this.exchangeFor = exchangeFor;
    }

    public void addImageUrl(String url) {
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        imageUrls.add(url);
    }

    @Exclude
    public String getFirstImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("title", title);
        map.put("description", description);
        map.put("category", category);
        map.put("condition", condition);
        map.put("type", type);
        map.put("status", status);
        map.put("ownerId", ownerId);
        map.put("ownerName", ownerName);
        map.put("imageUrls", imageUrls);
        map.put("createdTimestamp", createdTimestamp);
        map.put("updatedTimestamp", updatedTimestamp);
        map.put("auctionId", auctionId);
        map.put("startingPrice", startingPrice);
        map.put("exchangeFor", exchangeFor);
        return map;
    }
}
