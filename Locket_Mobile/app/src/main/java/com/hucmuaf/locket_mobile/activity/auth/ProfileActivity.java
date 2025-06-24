package com.hucmuaf.locket_mobile.activity.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.activity.PageComponentActivity;
import com.hucmuaf.locket_mobile.auth.TokenManager;
import com.hucmuaf.locket_mobile.modedb.User;
import com.hucmuaf.locket_mobile.repo.UploadResponse;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.ImageService;
import com.hucmuaf.locket_mobile.service.UserService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private TextView tvName, tvEditName, editAvatar, tvEditNumber, tvEditEmail;
    private User currUser;
    private String token;
    private String imageUrl;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo View
        ImageView btnBack = findViewById(R.id.back);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        tvEditName = findViewById(R.id.tvEditName);
        editAvatar = findViewById(R.id.EditAvatar);
        tvEditNumber = findViewById(R.id.tvPhoneNumber);
        tvEditEmail = findViewById(R.id.tvEmail);

        // Quay lại
        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PageComponentActivity.class));
            finish();
        });

        // Lấy userId từ intent
        String userId = getIntent().getStringExtra("userId");
        getUser(userId); // load user và set UI bên trong

        // Chọn ảnh mới
        editAvatar.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        // Xử lý ảnh được chọn
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    uploadImage(selectedImageUri);
                }
            }
        });

        // Sửa tên
        tvEditName.setOnClickListener(v -> Toast.makeText(this, "Chức năng sửa tên", Toast.LENGTH_SHORT).show());
    }

    private void getUser(String userId) {
        UserService userService = ApiClient.getUserService();
        Call<User> call = userService.findUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                currUser = response.body();

                if (currUser == null) {
                    Toast.makeText(ProfileActivity.this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Set avatar
                String urlAvatar = currUser.getUrlAvatar();
                if (urlAvatar == null || urlAvatar.isEmpty()) {
                    urlAvatar = "https://yourdomain.com/avatar.jpg";
                }

                Glide.with(ProfileActivity.this)
                        .load(urlAvatar)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(imgAvatar);

                imgAvatar.setVisibility(View.VISIBLE);

                // Set name
                String name = currUser.getFullName() != null ? currUser.getFullName() : "Chưa có tên";
                tvName.setText(name);
                tvEditName.setText(name);

                // Set phone
                String phone = currUser.getPhoneNumber() != null ? currUser.getPhoneNumber() : "Chưa có số điện thoại";
                tvEditNumber.setText(phone);

                // Set email
                String email = currUser.getEmail() != null ? currUser.getEmail() : "Chưa có email";
                tvEditEmail.setText(email);

                Log.e("getUser", "Lấy thông tin thành công: " + currUser.toString());
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi khi lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                Log.e("ProfileActivity", "Lỗi Retrofit: " + t.getMessage());
            }
        });
    }

    private void uploadImage(Uri url) {
        if (url == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = createTempFileFromUri(url);
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);

            ImageService imageService = ApiClient.getImageApiServiceToken(this);
            token = TokenManager.getToken(this); // cập nhật token
            String bearerToken = "Bearer " + token;

            Call<UploadResponse> call = imageService.uploadImage(TokenManager.getUid(this), bearerToken, body);
            call.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        imageUrl = response.body().getUrl();
                        Glide.with(ProfileActivity.this).load(imageUrl).circleCrop().into(imgAvatar);
                        imgAvatar.setVisibility(View.VISIBLE);
                    } else {
                        Log.e("UploadImage", "Lỗi upload ảnh: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UploadError", "Lỗi mạng hoặc server: " + t.getMessage());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri selectedImageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
        int fileSizeInBytes = inputStream.available();
        double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);

        if (fileSizeInMB > 4.0) {
            Toast.makeText(this, "Ảnh vượt quá 4MB. Vui lòng chọn ảnh nhỏ hơn.", Toast.LENGTH_SHORT).show();
        }

        String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
        File tempFile = new File(getCacheDir(), fileName);
        OutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return tempFile;
    }
}
