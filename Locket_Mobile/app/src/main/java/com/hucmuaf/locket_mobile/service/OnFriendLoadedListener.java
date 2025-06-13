package com.hucmuaf.locket_mobile.service;

import com.hucmuaf.locket_mobile.ModelDB.User;

import java.util.List;

public interface OnFriendLoadedListener {
    void onSuccess(List<User> users);
    void onFailure(String error);
}
