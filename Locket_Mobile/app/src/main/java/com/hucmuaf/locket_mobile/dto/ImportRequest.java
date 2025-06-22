package com.hucmuaf.locket_mobile.dto;


public class ImportRequest {
    private String userId;

    private String token;

    public ImportRequest() {
    }

    public ImportRequest(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
} 