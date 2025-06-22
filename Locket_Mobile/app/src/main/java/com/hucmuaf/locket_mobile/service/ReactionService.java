package com.hucmuaf.locket_mobile.service;


import com.hucmuaf.locket_mobile.modedb.Reaction;
import com.hucmuaf.locket_mobile.modedb.SaveResponse;
import com.hucmuaf.locket_mobile.modedb.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ReactionService {

    @POST("api/reaction/add")
    Call<SaveResponse> addReaction(@Body Reaction reaction);

    @GET("api/reaction/for/{imageId}")
    Call<List<User>> getFriendReactedToImageYou(@Path("imageId") String imageId);
}
