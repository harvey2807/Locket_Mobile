package vn.edu.hcumuaf.locket.dto;

public class SearchUserRequest {
    private String query;
    private String userId;

    public SearchUserRequest() {}

    public SearchUserRequest(String query, String userId) {
        this.query = query;
        this.userId = userId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 