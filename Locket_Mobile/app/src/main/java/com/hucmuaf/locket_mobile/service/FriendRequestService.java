package com.hucmuaf.locket_mobile.service;

import com.hucmuaf.locket_mobile.ModelDB.User;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface FriendRequestService {
    @GET("friends/listFriendId/{userId}")
    Call<Set<String>> getFriendIdsByUserId(@Path("userId") String userId);

    @GET("friends/list/user/{userId}")
    Call<List<User>> getListFriendByUserId (@Path("userId") String userId);
}
