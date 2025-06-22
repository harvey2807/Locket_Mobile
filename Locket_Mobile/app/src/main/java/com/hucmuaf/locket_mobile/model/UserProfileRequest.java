package com.hucmuaf.locket_mobile.model;

public class UserProfileRequest { // dung upload image và fullname khi dang ki lan dau
    private String fullName;
    private String urlAvatar;

    // Constructors
    public UserProfileRequest(String fullName, String urlAvatar) {
        this.fullName = fullName;
        this.urlAvatar = urlAvatar;
    }

    // Getters và setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }
}
