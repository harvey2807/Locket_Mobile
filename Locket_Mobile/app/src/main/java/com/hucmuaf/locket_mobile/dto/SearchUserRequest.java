package com.hucmuaf.locket_mobile.dto;

import com.google.gson.annotations.SerializedName;

public class SearchUserRequest {
    @SerializedName("query")
    private String query;
    
    @SerializedName("currentUserId")
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