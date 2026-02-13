package com.ecoshare.app.models;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Auction {
    private String auctionId;
    private String itemId;
    private double startPrice;
    private double currentBid;
    private String currentBidderId;
    private String currentBidderName;
    private Map<String, BidInfo> bidders;
    private long startTime;
    private long endTime;
    private boolean isActive;
    private String winnerId;
    private String winnerName;

    public Auction() {
        this.bidders = new HashMap<>();
        this.isActive = true;
    }

    public Auction(String auctionId, String itemId, double startPrice, long startTime, long endTime) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.startPrice = startPrice;
        this.currentBid = startPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = true;
        this.bidders = new HashMap<>();
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public double getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(double currentBid) {
        this.currentBid = currentBid;
    }

    public String getCurrentBidderId() {
        return currentBidderId;
    }

    public void setCurrentBidderId(String currentBidderId) {
        this.currentBidderId = currentBidderId;
    }

    public String getCurrentBidderName() {
        return currentBidderName;
    }

    public void setCurrentBidderName(String currentBidderName) {
        this.currentBidderName = currentBidderName;
    }

    public Map<String, BidInfo> getBidders() {
        return bidders;
    }

    public void setBidders(Map<String, BidInfo> bidders) {
        this.bidders = bidders;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public void addBid(String userId, String userName, double bidAmount) {
        if (bidders == null) {
            bidders = new HashMap<>();
        }
        bidders.put(userId, new BidInfo(userName, bidAmount, System.currentTimeMillis()));
        this.currentBid = bidAmount;
        this.currentBidderId = userId;
        this.currentBidderName = userName;
    }

    @Exclude
    public long getTimeRemaining() {
        long now = System.currentTimeMillis();
        return Math.max(0, endTime - now);
    }

    @Exclude
    public boolean hasEnded() {
        return System.currentTimeMillis() >= endTime;
    }

    @Exclude
    public List<BidderInfo> getBiddersList() {
        List<BidderInfo> list = new ArrayList<>();
        if (bidders != null) {
            for (Map.Entry<String, BidInfo> entry : bidders.entrySet()) {
                list.add(new BidderInfo(entry.getKey(), entry.getValue().getName(), 
                        entry.getValue().getAmount(), entry.getValue().getTimestamp()));
            }
        }
        return list;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("auctionId", auctionId);
        map.put("itemId", itemId);
        map.put("startPrice", startPrice);
        map.put("currentBid", currentBid);
        map.put("currentBidderId", currentBidderId);
        map.put("currentBidderName", currentBidderName);
        map.put("bidders", bidders);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("isActive", isActive);
        map.put("winnerId", winnerId);
        map.put("winnerName", winnerName);
        return map;
    }

    public static class BidInfo {
        private String name;
        private double amount;
        private long timestamp;

        public BidInfo() {
        }

        public BidInfo(String name, double amount, long timestamp) {
            this.name = name;
            this.amount = amount;
            this.timestamp = timestamp;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class BidderInfo {
        private String userId;
        private String name;
        private double bidAmount;
        private long timestamp;

        public BidderInfo(String userId, String name, double bidAmount, long timestamp) {
            this.userId = userId;
            this.name = name;
            this.bidAmount = bidAmount;
            this.timestamp = timestamp;
        }

        public String getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public double getBidAmount() {
            return bidAmount;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
