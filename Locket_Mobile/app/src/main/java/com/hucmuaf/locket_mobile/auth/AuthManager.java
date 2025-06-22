package com.hucmuaf.locket_mobile.auth;

import android.content.Context;

import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.AuthService;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthManager {

    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String message);
    }

    public static void verifyToken(Context context, String uId, String idToken, AuthCallback callback) {
        AuthService authService = ApiClient.getAuthService();
        String authHeader = "Bearer " + idToken;

        Call<ResponseBody> call = authService.verifyToken(uId, authHeader);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse JSON từ response body
                        String raw = response.body().string(); // đọc body
                        JSONObject jsonObject = new JSONObject(raw);
                        String userId = jsonObject.getString("userId");

                        TokenManager.saveUid(context, userId);
                        callback.onSuccess(userId);

                    } catch (Exception e) {
                        callback.onFailure("Lỗi xử lý dữ liệu: " + e.getMessage());
                    }
                } else {
                    callback.onFailure("Xác thực thất bại: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}

