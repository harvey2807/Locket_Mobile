package vn.edu.hcumuaf.locket.model;

import vn.edu.hcumuaf.locket.model.entity.User;

public class FriendRequest {
    private String friendRequestId;
    private String senderId;
    private String receiverId;
    private String status; // "pending", "ACCEPTED", "REJECTED"
    private long timestamp;
    private String senderName;
    private User sender;

    public FriendRequest() {}

    public FriendRequest(String friendRequestId, String senderId, String receiverId, String status, long timestamp) {
        this.friendRequestId = friendRequestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
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