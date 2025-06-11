package com.hucmuaf.locket_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home); // Sử dụng layout home.xml

        // Tìm LinearLayout với id="friends" và thêm sự kiện onClick
        LinearLayout friendsLayout = findViewById(R.id.friends);
        if (friendsLayout != null) {
            Log.d(TAG, "Found friends layout, setting click listener");
            
            // Đảm bảo layout có thể click được
            friendsLayout.setClickable(true);
            friendsLayout.setFocusable(true);
            
            friendsLayout.setOnClickListener(v -> {
                Log.d(TAG, "Friends layout clicked, starting ListFriendActivity");
                Toast.makeText(this, "Đang chuyển đến danh sách bạn bè...", Toast.LENGTH_SHORT).show();
                
                try {
                    Intent intent = new Intent(HomeActivity.this, ListFriendActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ListFriendActivity", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e(TAG, "Could not find LinearLayout with id 'friends'");
            Toast.makeText(this, "Không tìm thấy LinearLayout friends", Toast.LENGTH_SHORT).show();
        }
    }
}