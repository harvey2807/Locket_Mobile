package com.hucmuaf.locket_mobile.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.textfield.TextInputEditText;
import com.hucmuaf.locket_mobile.R;

public class InfoActivity extends AppCompatActivity {
    private  TextInputEditText edName, url;
    private LottieAnimationView avatarLottie;
    private ImageView imgAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        ImageView rollback = findViewById(R.id.back);
        TextInputEditText signUp = findViewById(R.id.teName);

        rollback.setOnClickListener(v -> {
            startActivity(new Intent(InfoActivity.this, ChoiceLoginActivity.class));
            finish();
        });

        avatarLottie = findViewById(R.id.avatarLottie);

        avatarLottie.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1001);
        });

        signUp.setOnClickListener(v -> updateProfile());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            imgAvatar.setImageURI(selectedImageUri);
            imgAvatar.setVisibility(View.VISIBLE);
            avatarLottie.setVisibility(View.GONE);


        }
    }

    private void updateProfile() {

    }
}
