package com.hucmuaf.locket_mobile.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static final String BASE_URL = "http://192.168.1.140:8080/api/";

    //Kết nối Android với API RESTful
    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()) // chuyển JSON sang Object
                    .build();
        }
        return retrofit;
    }
}
