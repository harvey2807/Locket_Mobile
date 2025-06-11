package com.hucmuaf.locket_mobile.dto;

import com.google.gson.annotations.SerializedName;

public class ShareRequest {
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("message")
    private String message;

    public ShareRequest() {}

    public ShareRequest(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 