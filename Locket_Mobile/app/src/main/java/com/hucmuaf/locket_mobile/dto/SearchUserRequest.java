package com.hucmuaf.locket_mobile.dto;


public class SearchUserRequest {
    private String query;
    
    private String currentUserId;

    public SearchUserRequest() {}

    public SearchUserRequest(String query, String currentUserId) {
        this.query = query;
        this.currentUserId = currentUserId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }
} 