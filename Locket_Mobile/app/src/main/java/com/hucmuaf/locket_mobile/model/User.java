package com.hucmuaf.locket_mobile.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("userName")
    private String userName;
    
    @SerializedName("fullName")
    private String fullName;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("urlAvatar")
    private String urlAvatar;
    
    @SerializedName("password")
    private String password;

    public User() {}

    public User(String userId, String userName, String fullName, String email, String phoneNumber, String urlAvatar, String password) {
        this.userId = userId;
        this.userName = userName;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.urlAvatar = urlAvatar;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
} 