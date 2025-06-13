package com.hucmuaf.locket_mobile.ModelDB;

public class Reaction {
    private String userId;
    private String imageId;
    private String icon;
    private long timestamp;

    public Reaction() {
    }

    public Reaction(String userId, String imageId, String icon, long timestamp) {
        this.userId = userId;
        this.imageId = imageId;
        this.icon = icon;
        this.timestamp = timestamp;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
