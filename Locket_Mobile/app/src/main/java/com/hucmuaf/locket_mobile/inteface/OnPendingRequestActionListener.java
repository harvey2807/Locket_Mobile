package com.hucmuaf.locket_mobile.inteface;

import com.hucmuaf.locket_mobile.model.FriendRequest;

public interface OnPendingRequestActionListener {
    void onAcceptRequest(FriendRequest request);
    void onRejectRequest(FriendRequest request);
}