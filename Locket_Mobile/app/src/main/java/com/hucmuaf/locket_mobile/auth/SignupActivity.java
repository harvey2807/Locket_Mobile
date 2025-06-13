package com.hucmuaf.locket_mobile.auth;

import android.content.Intent;
import android.os.Bundle;
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
import com.hucmuaf.locket_mobile.service.FirebaseService;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private EditText edEmail, edPassword, edConfirmPassword, edFullName, edPhoneNumber;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logup);

        edFullName = findViewById(R.id.textName);
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
    }

    private void handleSignUp() {
        String fullName = edFullName.getText().toString().trim();
        String phoneNumber = edPhoneNumber.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();

        // Kiểm tra hợp lệ
        if (fullName.isEmpty() || phoneNumber.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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
                        if (userId == null) {
                            toast("Không thể lấy UID người dùng.");
                            return;
                        }

                        Map<String, String> user = new HashMap<>();
                        user.put("email", email);
                        user.put("fullName", fullName);
                        user.put("password", hashPwd(password));
                        user.put("phoneNumber", phoneNumber);
                        user.put("userId", userId);

                        database.getReference("users").child(userId)
                                .setValue(user)
                                .addOnCompleteListener(dbTask -> {
                                    if (dbTask.isSuccessful()) {
                                        toast("Đăng ký thành công");
                                        Intent intent = new Intent(SignupActivity.this, InfoActivity.class);
                                        intent.putExtra("userId", userId);
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
