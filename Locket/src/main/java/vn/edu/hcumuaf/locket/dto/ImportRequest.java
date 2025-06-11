package vn.edu.hcumuaf.locket.dto;

import java.util.List;

public class ImportRequest {
    private String userId;
    private List<String> contacts;

    public ImportRequest() {}

    public ImportRequest(String userId, List<String> contacts) {
        this.userId = userId;
        this.contacts = contacts;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }
} 