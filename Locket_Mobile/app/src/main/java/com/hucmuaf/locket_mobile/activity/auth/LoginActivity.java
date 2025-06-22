package com.hucmuaf.locket_mobile.activity.auth;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.auth.AuthManager;
import com.hucmuaf.locket_mobile.auth.TokenManager;
import com.hucmuaf.locket_mobile.activity.PageComponentActivity;
//import com.hucmuaf.locket_mobile.activity.TakeActivity;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.AuthService;
import com.hucmuaf.locket_mobile.service.FirebaseService;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edEmail, edPassword;
    private FirebaseAuth mAuth;
    private String password, email;
    private Intent intent;
    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edEmail = findViewById(R.id.edemailLg);
        edPassword = findViewById(R.id.edpasswordLg);
        TextView txtSignup = findViewById(R.id.txtSignup);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtForgotPass = findViewById(R.id.txtForgotPass);
        ImageView imgBack = findViewById(R.id.arrowBack);

        mAuth = FirebaseService.getInstance().getAuth();

        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.getExtras() != null) {
            Bundle extras = receivedIntent.getExtras();
            edEmail.setText(extras.getString("email", ""));
            edPassword.setText(extras.getString("password", ""));
        }


        // Ban đầu disable nút login và set màu nhạt
        btnLogin.setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                email = edEmail.getText().toString().trim();
                password = edPassword.getText().toString().trim();

                // Kiểm tra nếu cả email và password đều không rỗng
                boolean isValid = !email.isEmpty() && !password.isEmpty();
                btnLogin .setEnabled(isValid);

                int backgroundId = isValid ? R.drawable.radius_border : R.drawable.radius_border_disable;
                btnLogin.setBackground(ContextCompat.getDrawable(LoginActivity.this, backgroundId));

            }
        };
        edEmail.addTextChangedListener(textWatcher);
        edPassword.addTextChangedListener(textWatcher);

        btnLogin.setOnClickListener(view -> attemptLogin());

        imgBack.setOnClickListener(view -> {
            intent = new Intent(LoginActivity.this, ChoiceLoginActivity.class);
            startActivity(intent);
            finish(); // Đóng LoginActivity khi quay lại
        });

        txtSignup.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class))
        );

        txtForgotPass.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, ForgotPassActivity.class))
        );


    }

    private void attemptLogin() {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Không được bỏ trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Địa chỉ email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser != null ? firebaseUser.getUid() : null;
                        if (userId == null) {
                            Toast.makeText(this, "Không thể lấy UID người dùng.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // lấy token của phien đăng nhập hiện tại
                        firebaseUser.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String idToken = tokenTask.getResult().getToken();

                                //Gửi token lên backend nếu muốn
                                AuthManager.verifyToken(this, userId, idToken, new AuthManager.AuthCallback() {;
                                    @Override
                                    public void onSuccess(String userId) {
                                        // Lưu UID vào TokenManager
                                        TokenManager.saveUid(LoginActivity.this, userId);
                                        TokenManager.saveToken(LoginActivity.this ,idToken);
                                        Log.e(TAG, "Xác thực token thành công: " + userId + " - Token: " + idToken);

                                        Intent intent = new Intent(LoginActivity.this, PageComponentActivity.class);
                                        intent.putExtra("userId", userId);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        Log.e(TAG, "Xác thực token thất bại: " + message);
                                        Toast.makeText(LoginActivity.this, "Lỗi xác thực: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
