package com.hucmuaf.locket_mobile.model;

import com.google.gson.annotations.SerializedName;

public class FriendRequest {
    @SerializedName("friendRequestId")
    private String friendRequestId;
    
    @SerializedName("senderId")
    private String senderId;
    
    @SerializedName("receiverId")
    private String receiverId;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("timestamp")
    private long timestamp;
    
    @SerializedName("senderName")
    private String senderName;
    
    @SerializedName("sender")
    private User sender;

    public FriendRequest() {}

    public FriendRequest(String friendRequestId, String senderId, String receiverId, String status, long timestamp) {
        this.friendRequestId = friendRequestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getFriendRequestId() {
        return friendRequestId;
    }

    public void setFriendRequestId(String friendRequestId) {
        this.friendRequestId = friendRequestId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }
} 