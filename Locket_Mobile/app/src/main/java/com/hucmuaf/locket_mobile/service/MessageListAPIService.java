package com.hucmuaf.locket_mobile.service;

import com.hucmuaf.locket_mobile.model.Message;

import java.util.*;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MessageListAPIService {
    @GET("/app/api/messages")
    Call<List<Message>> getAllMessages();

    @GET("app/api/messages/{userId}")
    Call<List<Message>> getMessageWithUserId(@Path("userId") String userId);

    //    @GET("/api/chat.send")
    @POST("/api/messages/send")
    Call<Void> sendMessage(@Body Message message);

    @GET("app/api/messages/{senderId}/{receiverId}")
    Call<List<Message>> getMessagesBetweenUsers(@Path("senderId") String senderId, @Path("receiverId") String receiverId);
}
