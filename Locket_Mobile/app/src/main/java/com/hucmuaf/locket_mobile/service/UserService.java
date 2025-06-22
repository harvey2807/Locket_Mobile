package com.hucmuaf.locket_mobile.service;

import com.hucmuaf.locket_mobile.modedb.Image;
import com.hucmuaf.locket_mobile.model.UserProfileRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {
    @POST("images/user/")
    Call<List<Image>> getImageOfUser(@Path("userId") String userId);

    //update avatar va fullname
    @PUT("api/user/{userId}/updateProfile")
    Call<ResponseBody> updateUserProfile(
            @Path("userId") String userId,
            @Header("Authorization") String idToken,
            @Body UserProfileRequest profile
    );

}
