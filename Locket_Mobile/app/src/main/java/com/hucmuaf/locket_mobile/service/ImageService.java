package com.hucmuaf.locket_mobile.service;

import com.hucmuaf.locket_mobile.ModelDB.Image;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ImageService {
    @GET("images/{imageId}")
    Call<Image> getImageById(@Path("imageId") String imageId);

    @GET("images/all/{userId}")
    Call<List<Image>> getAllImages(@Path("userId") String userId);

    @GET("images/user/{userId}")
    Call<List<Image>> getImageOfUser(@Path("userId") String userId);

}
