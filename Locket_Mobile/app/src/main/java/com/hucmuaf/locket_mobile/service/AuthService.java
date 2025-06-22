package com.hucmuaf.locket_mobile.service;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthService {
    @POST("api/auth/{userId}/verifyToken")
    Call<ResponseBody> verifyToken(
            @Path("userId") String userId,
            @Header("Authorization") String idToken
    );


}
