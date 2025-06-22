package vn.edu.hcumuaf.locket.model.entity;

import vn.edu.hcumuaf.locket.model.FriendRequestStatus;

public class FriendRequest {
    private String friendRequestId;
    private String senderId;
    private String receiverId;
    private FriendRequestStatus status;
    private long timestamp;
    private String senderName;
    private User sender;

    public FriendRequest() {
    }

    public FriendRequest(String friendRequestId, String senderId, String receiverId, FriendRequestStatus status, long timestamp) {
        this.friendRequestId = friendRequestId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status; //pending, accepted, rejected, cancelled
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

    public FriendRequestStatus getStatus() {
        return status;
    }

    public void setStatus(FriendRequestStatus status) {
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
