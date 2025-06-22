package com.hucmuaf.locket_mobile.service;

import android.content.Context;

import com.hucmuaf.locket_mobile.auth.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    //gán header token vào request khi gọi class ApiClient hàm getAUthCLient(Context context)
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();// lấy request gốc
        String token = TokenManager.getToken(context);

        if (token != null) {
            // Nếu có token -> tạo request mới với Header Authorization
            Request newRequest = request.newBuilder()
                    .header("Authorization", token)
                    .build();
            return chain.proceed(newRequest);
        }
        // Không có token => trả về lỗi 401 (unauthorized)
        return new Response.Builder()
                .code(401)
                .message("Missing token")
                .protocol(Protocol.HTTP_1_1)
                .request(request)
                .body(ResponseBody.create(
                        MediaType.parse("application/json"),
                        "{\"error\":\"Unauthorized - Token missing\"}"
                ))
                .build();
    }
}
