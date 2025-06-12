package com.hucmuaf.locket_mobile.modeldb;

public class Room {
    private String roomId;
    private String ownerId;
    private String receiverId;
    private long timestamp;

    public Room(){};

    public Room(String roomId, String ownerId, String receiverId, long timestamp) {
        this.roomId = roomId;
        this.ownerId = ownerId;
        this.receiverId = receiverId;
        this.timestamp = timestamp;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

