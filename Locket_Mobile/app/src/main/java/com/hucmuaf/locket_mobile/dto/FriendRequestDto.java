package com.hucmuaf.locket_mobile.dto;

import com.google.gson.annotations.SerializedName;

public class FriendRequestDto {
    @SerializedName("senderId")
    private String senderId;
    
    @SerializedName("receiverId")
    private String receiverId;

    public FriendRequestDto() {}

    public FriendRequestDto(String senderId, String receiverId) {
        this.senderId = senderId;
        this.receiverId = receiverId;
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
} 