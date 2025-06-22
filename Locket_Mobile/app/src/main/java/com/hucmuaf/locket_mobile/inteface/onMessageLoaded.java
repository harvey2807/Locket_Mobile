package com.hucmuaf.locket_mobile.inteface;

import com.hucmuaf.locket_mobile.model.Message;

import java.util.List;

public interface onMessageLoaded {
    void onSuccess(List<Message> mess);


    void onFailure(Exception e);

}
