package com.hucmuaf.locket_mobile.service;

import com.hucmuaf.locket_mobile.dto.FriendRequest;
import com.hucmuaf.locket_mobile.modedb.User;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FriendRequestService {
    @GET("api/friends/listFriendId/{userId}")
    Call<Set<String>> getFriendIdsByUserId(@Path("userId") String userId);

    @GET("api/friends/list/user/{userId}")
    Call<List<User>> getListFriendByUserId(@Path("userId") String userId);

    @GET("api/friend-list/user/{userId}")
    Call<User> getUserById(@Path("userId") String userId);

    @GET("api/friend-list/request-status/{userId1}/{userId2}")
    Call<com.hucmuaf.locket_mobile.model.FriendRequest> getFriendRequestStatus(@Path("userId1") String userId1, @Path("userId2") String userId2);

    @POST("api/friend-list/send-request")
    Call<String> sendFriendRequest(@Body FriendRequest request);
}
