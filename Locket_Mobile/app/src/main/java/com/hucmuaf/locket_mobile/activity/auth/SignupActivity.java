
package com.hucmuaf.locket_mobile.activity.auth;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.activity.PageComponentActivity;
import com.hucmuaf.locket_mobile.auth.AuthManager;
import com.hucmuaf.locket_mobile.auth.TokenManager;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.AuthService;
import com.hucmuaf.locket_mobile.service.FirebaseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private EditText edEmail, edPassword, edConfirmPassword, edUserName, edPhoneNumber;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logup);

        edUserName = findViewById(R.id.textUserName);
        edPhoneNumber = findViewById(R.id.textPhone);
        edEmail = findViewById(R.id.textEmail);
        edPassword = findViewById(R.id.textPassword);
        edConfirmPassword = findViewById(R.id.textRepeatPassword);
        Button btSignUp = findViewById(R.id.btSignup);
        ImageView rollback = findViewById(R.id.back);

        FirebaseService firebaseService = FirebaseService.getInstance();
        mAuth = firebaseService.getAuth();
        database = firebaseService.getDatabase();

        btSignUp.setOnClickListener(v -> handleSignUp());
        rollback.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, ChoiceLoginActivity.class));
            finish();
        });

        edUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();

                // Bỏ dấu và khoảng trắng
                String normalized = removeDiacritics(input).replaceAll("\\s+", "");

                if (!input.equals(normalized)) {
                    edUserName.setText(normalized);
                    edUserName.setSelection(normalized.length()); // Đưa con trỏ về cuối
                }
            }
        });


        String input = edUserName.getText().toString();
        if (!input.matches("^[a-zA-Z0-9]+$")) {
            edUserName.setError("Vui lòng nhập liền không dấu, không ký tự đặc biệt");
        }

    }

    private void handleSignUp() {
        String userName = edUserName.getText().toString().trim();
        String phoneNumber = edPhoneNumber.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();

        // Kiểm tra hợp lệ
        if (userName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            toast("Không thể để trống");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Email không hợp lệ!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            toast("Mật khẩu không khớp");
            return;
        }
        if (!Patterns.PHONE.matcher(phoneNumber).matches() || phoneNumber.length() < 9 || phoneNumber.length() > 11) {
            toast("Số điện thoại không hợp lệ (phải từ 9 đến 11 chữ số)");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser != null ? firebaseUser.getUid() : null;
                        Log.e(TAG, "Đăng kí thành công: " + userId);

                        // lấy token của phien đăng nhập hiện tại
                        firebaseUser.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String idToken = tokenTask.getResult().getToken();

                                ///Gửi token lên backend nếu muốn
                                AuthManager.verifyToken(this, userId, idToken, new AuthManager.AuthCallback() {
                                    @Override
                                    public void onSuccess(String userId) {
                                        // Lưu UID vào TokenManager
                                        TokenManager.saveUid(SignupActivity.this, userId);
                                        TokenManager.saveToken(SignupActivity.this, idToken);
                                        Log.e(TAG, "Xác thực token thành công: " + userId + " - Token: " + idToken);
                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        Log.e(TAG, "Xác thực token thất bại: " + message);
                                        Toast.makeText(SignupActivity.this, "Lỗi xác thực: " + message, Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                        });

                        Map<String, String> user = new HashMap<>();
                        user.put("email", email);
                        user.put("userName", userName);
                        user.put("password", hashPwd(password));
                        user.put("phoneNumber", phoneNumber);
                        user.put("avatar", "https://res.cloudinary.com/dwjztnzgv/image/upload/v1750495440/avatar_knqsmw.jpg");
                        user.put("userId", userName);

                        database.getReference("users").child(userId)
                                .setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        toast("Đăng ký thành công");
                                        Intent intent = new Intent(SignupActivity.this, InfoActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Exception e = dbTask.getException();
                                        Log.e("FIREBASE_DB_ERROR", "Lỗi lưu dữ liệu", e);
                                        toast("Lỗi lưu dữ liệu: " + dbTask.getException().getMessage());
                                    }
                                });

                    } else {
                        toast("Đăng kí thất bại" + task.getException().getMessage());
                    }
                });
    }

    // Hàm để loại bỏ dấu và khoảng trắng trong chuỗi
    private String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String hashPwd(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
