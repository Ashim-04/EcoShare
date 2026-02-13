package com.ecoshare.app.models;

public class Badge {
    private String badgeId;
    private String name;
    private String description;
    private String iconName;
    private int requiredCount;

    public Badge() {
    }

    public Badge(String badgeId, String name, String description, String iconName, int requiredCount) {
        this.badgeId = badgeId;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.requiredCount = requiredCount;
    }

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getRequiredCount() {
        return requiredCount;
    }

    public void setRequiredCount(int requiredCount) {
        this.requiredCount = requiredCount;
    }
}
