package com.hucmuaf.locket_mobile.service;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hucmuaf.locket_mobile.activity.PageComponentActivity;
import com.hucmuaf.locket_mobile.inteface.FriendListApiService;
import com.hucmuaf.locket_mobile.modedb.UploadImageResponse;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
//         private static final String BASE_URL = "http://10.51.0.1:8080/"; // For Android emulator

    private static final String BASE_URL = "http://172.16.1.79:8080/"; // For real device, your computer's IP

    // private static final String BASE_URL = "http://localhost:8080/"; // For testing

    private static Retrofit retrofit = null;
    // Retrofit có AuthInterceptor để tự động thêm token vào header
    private static Retrofit authRetrofit = null;

    private static FriendListApiService friendListApiService = null;
    private static MessageListAPIService messageListAPIService;
    private static UploadImageService uploadImageService;
    private static ImageService imageService;
    private static ReactionService reactionService;
    private static UserService userService;
    private static AuthService authService;
    //service lấy ra danh sách id bạn bè, danh sách user là bạn bè của current user
    private static FriendRequestService friendRequestService = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Cấu hình Gson với lenient mode để xử lý JSON malformed
            Gson gson = new GsonBuilder()
                    .setLenient() // Cho phép xử lý JSON không đúng chuẩn
                    .setDateFormat("yyyy-MM-dd HH:mm:ss") // Định dạng ngày tháng
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static ImageService getImageService(){
        if(imageService == null) {
            imageService = getClient().create(ImageService.class);
        }
        return imageService;
    }
    public static UploadImageService getUploadImageService() {
        if (uploadImageService == null) {
            uploadImageService = getClient().create(UploadImageService.class);
        }
        return uploadImageService;
    }
    // retrofit với AuthInterceptor để tự động thêm token vào header
    // Retrofit có token – cho các API quan trọng (profile, cập nhật,...)
    public static Retrofit getAuthClient(Context context) {
        if (authRetrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(context))  // Gán token
                    .build();

            return new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    public static FriendListApiService getFriendListApiService() {
        if (friendListApiService == null) {
            friendListApiService = getClient().create(FriendListApiService.class);
        }
        return friendListApiService;
    }

    public static MessageListAPIService getMessageListApiService() {
        if (messageListAPIService == null) {
            messageListAPIService = getClient().create(MessageListAPIService.class);
        }
        return messageListAPIService;
    }
    public static ReactionService getReactionService() {
        if (reactionService == null) {
            reactionService = getClient().create(ReactionService.class);
        }
        return reactionService;
    }
    public static ImageService getImageApiService() {
        if (imageService == null) {
            imageService = getClient().create(ImageService.class);
        }
        return imageService;
    }

    public static ImageService getImageApiServiceToken(Context context) {
        if (imageService == null) {
            imageService = getAuthClient(context).create(ImageService.class);
        }
        return imageService;
    }

    public static UserService getUserService() {
        if (userService == null) {
            userService = getClient().create(UserService.class);
        }
        return userService;
    }

    //có thên token trên header
    public static UserService getUserServiceToken(Context context) {
        if (userService == null) {
            userService = getAuthClient(context).create(UserService.class);
        }
        return userService;
    }

    //có thên token trên header
    public static AuthService getAuthServiceToken(Context context) {
        if (authService == null) {
            authService = getAuthClient(context).create(AuthService.class);
        }
        return authService;
    }
    public static AuthService getAuthService() {
        if (authService == null) {
            authService = getClient().create(AuthService.class);
        }
        return authService;
    }

    public static FriendRequestService getFriendRequestService(){
        if (friendRequestService == null){
            friendRequestService = getClient().create(FriendRequestService.class);
        }
        return friendRequestService;
    }
}