package com.hucmuaf.locket_mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.dto.FriendRequest;
import com.hucmuaf.locket_mobile.modedb.User;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.FriendRequestService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendRequestActivity extends AppCompatActivity {

    private ImageView avatarImageView;
    private TextView nameTextView;
    private TextView statusTextView;
    private Button actionButton;

    private String currentUserId;
    private String senderId;
    private User senderUser;
    private com.hucmuaf.locket_mobile.model.FriendRequest currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_request);

        initViews();
        getIntentData();
        loadSenderInfo();
    }

    private void initViews() {
        avatarImageView = findViewById(R.id.avatarImageView);
        nameTextView = findViewById(R.id.nameTextView);
        statusTextView = findViewById(R.id.statusTextView);
        actionButton = findViewById(R.id.actionButton);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            senderId = intent.getStringExtra("sender_id");
            currentUserId = intent.getStringExtra("current_user_id");
        }
    }

    private void loadSenderInfo() {
        if (senderId == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người gửi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        ApiClient.getClient().create(FriendRequestService.class)
                .getUserById(senderId)
                .enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            senderUser = response.body();
                            displaySenderInfo();
                            checkFriendRequestStatus();
                        } else {
                            Toast.makeText(SendRequestActivity.this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(SendRequestActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void displaySenderInfo() {
        if (senderUser != null) {
            String displayName = senderUser.getFullName() != null ? senderUser.getFullName() : senderUser.getUserName();
            nameTextView.setText(displayName);

            if (senderUser.getUrlAvatar() != null) {
                Glide.with(this)
                        .load(senderUser.getUrlAvatar())
                        .placeholder(R.drawable.avt)
                        .error(R.drawable.avt)
                        .circleCrop()
                        .into(avatarImageView);
            } else {
                avatarImageView.setImageResource(R.drawable.avt);
            }
        }
    }

    private void checkFriendRequestStatus() {
        if (currentUserId == null || senderId == null) {
            return;
        }

        ApiClient.getClient().create(FriendRequestService.class)
                .getFriendRequestStatus(currentUserId, senderId)
                .enqueue(new Callback<com.hucmuaf.locket_mobile.model.FriendRequest>() {
                    @Override
                    public void onResponse(Call<com.hucmuaf.locket_mobile.model.FriendRequest> call, Response<com.hucmuaf.locket_mobile.model.FriendRequest> response) {
                        if (response.isSuccessful()) {
                            currentRequest = response.body();
                            updateUI();
                        } else {
                            currentRequest = null;
                            updateUI();
                        }
                    }

                    @Override
                    public void onFailure(Call<com.hucmuaf.locket_mobile.model.FriendRequest> call, Throwable t) {
                        Toast.makeText(SendRequestActivity.this, "Lỗi kiểm tra trạng thái: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        currentRequest = null;
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        if (currentRequest == null) {
            statusTextView.setText("Chưa có lời mời kết bạn");
            actionButton.setText("Gửi lời mời kết bạn");
            actionButton.setVisibility(View.VISIBLE);
            actionButton.setOnClickListener(v -> sendFriendRequest());
        } else {
            String status = currentRequest.getStatus();
            switch (status) {
                case "ACCEPTED":
                    statusTextView.setText("Bạn bè");
                    actionButton.setVisibility(View.GONE);
                    break;
                case "PENDING":
//                    người gửi
                    if (currentUserId.equals(currentRequest.getSenderId())) {
                        statusTextView.setText("Đã gửi lời mời kết bạn");
                        actionButton.setVisibility(View.GONE);
                    } else {
//                        người nhận
                        statusTextView.setText("Đã nhận lời mời kết bạn");
                        actionButton.setVisibility(View.GONE);
                    }
                    break;
                case "REJECTED":
                case "CANCELLED":
                    statusTextView.setText("Lời mời đã bị từ chối/hủy");
                    actionButton.setText("Gửi lại lời mời kết bạn");
                    actionButton.setVisibility(View.VISIBLE);
                    actionButton.setOnClickListener(v -> sendFriendRequest());
                    break;
                default:
                    statusTextView.setText("Trạng thái không xác định");
                    actionButton.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void sendFriendRequest() {
        if (currentUserId == null || senderId == null) {
            Toast.makeText(this, "Thông tin không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        FriendRequest requestDto = new FriendRequest(currentUserId, senderId);

        ApiClient.getClient().create(FriendRequestService.class)
                .sendFriendRequest(requestDto)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SendRequestActivity.this, "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
                            checkFriendRequestStatus();
                        } else {
                            Toast.makeText(SendRequestActivity.this, "Lỗi gửi lời mời: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(SendRequestActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}