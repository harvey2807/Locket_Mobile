package com.hucmuaf.locket_mobile.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.activity.auth.ProfileActivity;
import com.hucmuaf.locket_mobile.adapter.PagerAdapter;

public class PageComponentActivity extends AppCompatActivity {
    private ImageView chat;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.page_component);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView profile = findViewById(R.id.account);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = getIntent().getStringExtra("userId"); // lấy userId hiện tại

                Intent in = new Intent(PageComponentActivity.this, ProfileActivity.class);
                in.putExtra("userId", userId); // truyền sang ProfileActivity
                startActivity(in);
            }
        });


        String userId = getIntent().getStringExtra("userId");

        chat = findViewById(R.id.message);

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(PageComponentActivity.this, MessageActivity.class);
                onPause();
                startActivity(in);
            }
        });

        ViewPager2 pages = findViewById(R.id.main_viewpager);
        PagerAdapter pagerAdapter = new PagerAdapter(this, userId);
        // Nhận từ AllImageActivity
        Intent intent = getIntent();
        String imageId = intent.getStringExtra("imageId");
        String friendId = intent.getStringExtra("friendId");
        String friendName = intent.getStringExtra("friendName");
        int pageIndex = intent.getIntExtra("pageIndex", 0); //Mặc định là trang home

        // Truyền imageId vào adapter
        pagerAdapter.setInitialImageData(imageId, friendId, friendName);
        pages.setAdapter(pagerAdapter);
        pages.setCurrentItem(pageIndex, false);
    }
}
