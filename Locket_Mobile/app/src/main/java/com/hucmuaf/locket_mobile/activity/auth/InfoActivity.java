package com.hucmuaf.locket_mobile.activity.auth;

import static android.util.Log.DEBUG;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.auth.AuthManager;
import com.hucmuaf.locket_mobile.auth.TokenManager;
import com.hucmuaf.locket_mobile.activity.PageComponentActivity;
import com.hucmuaf.locket_mobile.model.UserProfileRequest;
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
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoActivity extends AppCompatActivity {
    private TextInputEditText edName;
    private LottieAnimationView avatarLottie;
    private ImageView imgAvatar;
    private String imageUrl;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String token;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ImageView rollback = findViewById(R.id.back);
        imgAvatar = findViewById(R.id.imgAvatar);
        TextInputEditText fullName = findViewById(R.id.teName);
        Button btnLogin = findViewById(R.id.btnLogin);
        token = TokenManager.getToken(this);

        btnLogin.setOnClickListener(v -> updateProfile(fullName.getText().toString()));

        rollback.setOnClickListener(v -> {
            startActivity(new Intent(InfoActivity.this, ChoiceLoginActivity.class));
            finish();
        });

        // Mở trình chọn ảnh
        avatarLottie = findViewById(R.id.avatarLottie);
        avatarLottie.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            //return reponse to onActivityResult
            imagePickerLauncher.launch(intent);

        });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                assert selectedImageUri != null;
                Log.d("DEBUG_URI", selectedImageUri.toString());
                // xử lý ảnh tại đây
                uploadImage(selectedImageUri);
            }
        });
    }

    private void uploadImage(Uri url) {
        if (url == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh đại diện", Toast.LENGTH_SHORT).show();
        }
        try {
            File imageFile = createTempFileFromUri(url);  // Chuyển Uri thành File thật
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            // Tạo MultipartBody.Part để gửi ảnh , dữ liệu nhận vào của api là part
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);

            // Gọi API upload ảnh
            ImageService imageService = ApiClient.getImageApiServiceToken(this);
            Call<UploadResponse> call = imageService.uploadImage(TokenManager.getUid(InfoActivity.this), token, body);

            call.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        imageUrl = response.body().getUrl();
                        Log.d("Upload", "URL ảnh: " + imageUrl + " - Token: " + token);

                        Glide.with(InfoActivity.this).load(imageUrl).circleCrop().into(imgAvatar);

                        imgAvatar.setVisibility(View.VISIBLE);
                        avatarLottie.setVisibility(View.GONE);
                    } else {
                        // kiểm tra lại token nếu hết hạn thì tạo lại cái mới
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.getIdToken(true).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // lấy token mới
                                    String newToken = task.getResult().getToken();

                                    ///Gửi token lên backend nếu muốn
                                    AuthManager.verifyToken(InfoActivity.this, TokenManager.getUid(InfoActivity.this), newToken, new AuthManager.AuthCallback() {
                                        ;

                                        @Override
                                        public void onSuccess(String userId) {
                                            // Lưu UID vào TokenManager
                                            TokenManager.saveUid(InfoActivity.this, userId);
                                            Log.e(TAG, "Xác thực token thành công: " + userId);

                                            Intent intent = new Intent(InfoActivity.this, PageComponentActivity.class);
                                            intent.putExtra("userId", userId);
                                            startActivity(intent);
                                            finish();
                                        }

                                        @Override
                                        public void onFailure(String message) {
                                            Log.e(TAG, "Xác thực token thất bại: " + message);
                                            Toast.makeText(InfoActivity.this, "Lỗi xác thực: " + message, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                } else {
                                    // Lỗi khi refresh token → buộc người dùng đăng nhập lại
                                    Toast.makeText(InfoActivity.this, "Token hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(InfoActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
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
        Log.d("UploadDebug", "Bắt đầu tạo file tạm từ Uri: " + selectedImageUri);

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(selectedImageUri);
            int fileSizeInBytes = inputStream.available();
            double fileSizeInMB = fileSizeInBytes / (1024.0 * 1024.0);

            if (fileSizeInMB > 4.0) {
                Toast.makeText(this, "Ảnh vượt quá 4MB. Vui lòng chọn ảnh nhỏ hơn.", Toast.LENGTH_SHORT).show();
            }

            // Tạo tên file tạm thời
            String fileName = "temp_image_" + System.currentTimeMillis() + ".jpg";
            Log.d("UploadDebug", "Tên file tạm: " + fileName);

            // Tạo file trong thư mục cache của app
            File tempFile = new File(getCacheDir(), fileName);
            Log.d("UploadDebug", "Đường dẫn file tạm: " + tempFile.getAbsolutePath());

            outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalBytes = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            Log.d("UploadDebug", "Tổng số byte đã ghi: " + totalBytes);
            return tempFile;

        } catch (IOException e) {
            Log.e("UploadDebug", "Lỗi khi tạo file tạm: " + e.getMessage(), e);
            throw e;
        }
    }


    private void updateProfile(String fullname) {
        String userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy userId", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra nếu fullname rỗng
        if (fullname == null || fullname.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo dữ liệu để gửi lên server
        String name = fullname.trim();
        String avatar = imageUrl != null ? imageUrl : "https://res.cloudinary.com/dwjztnzgv/image/upload/v1750495440/avatar_knqsmw.jpg";

        UserProfileRequest dataRequest = new UserProfileRequest(name, avatar);
        // Gọi API cập nhật thông tin người dùng
        UserService userService = ApiClient.getUserServiceToken(this);
        Call<ResponseBody> call = userService.updateUserProfile(userId, token, dataRequest);

        // Gửi request
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(InfoActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình tiếp theo
                    Intent intent = new Intent(InfoActivity.this, PageComponentActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    if (response.code() == 401) {
                        String errorBody = null; // Lấy nội dung lỗi
                        try {
                            errorBody = response.errorBody().string();
                            Log.e("UpdateProfile", "Lỗi xác thực: " + errorBody);
                            Toast.makeText(InfoActivity.this, "Bạn cần đăng nhập lại!", Toast.LENGTH_SHORT).show();

                            //chuyen ve trang login
                            Intent intent = new Intent(InfoActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        Log.e("UpdateProfile", "Lỗi: " + response.code());
                    }
                    Toast.makeText(InfoActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(InfoActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
}
