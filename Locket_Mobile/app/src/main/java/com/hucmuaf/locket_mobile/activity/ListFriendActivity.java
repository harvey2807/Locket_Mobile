package com.hucmuaf.locket_mobile.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.inteface.FriendListApiService;
import com.hucmuaf.locket_mobile.inteface.OnFriendActionListener;
import com.hucmuaf.locket_mobile.inteface.OnPendingRequestActionListener;
import com.hucmuaf.locket_mobile.inteface.OnSentRequestActionListener;
import com.hucmuaf.locket_mobile.inteface.OnAddFriendClickListener;
import com.hucmuaf.locket_mobile.model.FriendListResponse;
import com.hucmuaf.locket_mobile.model.User;
import com.hucmuaf.locket_mobile.model.FriendRequest;
import com.hucmuaf.locket_mobile.dto.SearchUserRequest;
import com.hucmuaf.locket_mobile.dto.ShareRequest;
import com.hucmuaf.locket_mobile.adapter.FriendAdapter;
import com.hucmuaf.locket_mobile.adapter.PendingRequestAdapter;
import com.hucmuaf.locket_mobile.adapter.SentRequestAdapter;
import com.hucmuaf.locket_mobile.adapter.SearchUserAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListFriendActivity extends AppCompatActivity implements OnAddFriendClickListener {
    private FriendListApiService apiService;
    private String currentUserId;
    private TextView friendCount;
    private RecyclerView friendsRecyclerView;
    private RecyclerView pendingRequestsRecyclerView;
    private RecyclerView sentRequestsRecyclerView;
    private RecyclerView searchResultsRecyclerView;
    private FriendAdapter friendAdapter;
    private PendingRequestAdapter pendingRequestAdapter;
    private SentRequestAdapter sentRequestAdapter;
    private SearchUserAdapter searchUserAdapter;
    private final List<User> friendsList = new ArrayList<>();
    private final List<FriendRequest> pendingRequestsList = new ArrayList<>();
    private final List<FriendRequest> sentRequestsList = new ArrayList<>();
    private final List<User> searchResultsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listfriend);
        apiService = ApiClient.getFriendListApiService();

        // Lấy userId từ Intent trước, nếu không có thì lấy từ Firebase Auth
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("userId")) {
            currentUserId = intent.getStringExtra("userId");
            Log.d("ListFriendActivity", "Using userId from Intent: " + currentUserId);
        } else {
            //  lấy từ Firebase Auth
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                currentUserId = currentUser.getUid();
                Log.d("ListFriendActivity", "Using Firebase UID as fallback: " + currentUserId);
            } else {
                currentUserId = " ";
                Log.e("ListFriendActivity", "No userId available");
            }
        }
        
        // Log để debug
        Log.d("ListFriendActivity", "Final userId being used: " + currentUserId);
        
        initializeAfterUserId();
    }

    private void initializeAfterUserId() {
        testApiConnection();
        initializeViews();
        setupRecyclerViews();
        setupClickListeners();
        loadFriendList();
        loadPendingRequests();
        loadSentRequests();
        setupAppIconClickListeners();
    }

    // Test kết nối API để kiểm tra backend có hoạt động không
    private void testApiConnection() {
        Call<String> call = apiService.testConnection();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.d("ListFriendActivity", "API Test Response Code: " + response.code());
                Log.d("ListFriendActivity", "API Test Response Body: " + response.body());

                // Xử lý lỗi JSON parsing
                if (response.code() == 204) {
                    Log.d("ListFriendActivity", "API Test successful - No Content");
                } else if (response.isSuccessful()) {
                    Log.d("ListFriendActivity", "API Test successful");
                } else {
                    Log.e("ListFriendActivity", "API Test failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (t.getMessage() != null && !t.getMessage().contains("JSON document was not fully consumed")) {
                    Toast.makeText(ListFriendActivity.this, "Kết nối thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initializeViews() {
        friendCount = findViewById(R.id.friendCount);
        EditText searchFriend = findViewById(R.id.searchFriend);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        pendingRequestsRecyclerView = findViewById(R.id.pendingRequestsRecyclerView);
        sentRequestsRecyclerView = findViewById(R.id.sentRequestsRecyclerView);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        searchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // gọi hàm tìm kiếm realtime
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupRecyclerViews() {
        friendAdapter = new FriendAdapter(friendsList, new OnFriendActionListener() {
            @Override
            public void onRemoveFriend(User user) {
                showConfirmationDialog(user);
            }
        });
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendAdapter);
        pendingRequestAdapter = new PendingRequestAdapter(pendingRequestsList, new OnPendingRequestActionListener() {
            @Override
            public void onAcceptRequest(FriendRequest request) {
                acceptFriendRequest(request);
            }

            @Override
            public void onRejectRequest(FriendRequest request) {
                rejectFriendRequest(request);
            }
        });
        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingRequestsRecyclerView.setAdapter(pendingRequestAdapter);
        sentRequestAdapter = new SentRequestAdapter(this, sentRequestsList, new OnSentRequestActionListener() {
            @Override
            public void onCancelRequest(FriendRequest request) {
                cancelSentRequest(request);
            }
        });
        sentRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        sentRequestsRecyclerView.setAdapter(sentRequestAdapter);
        searchUserAdapter = new SearchUserAdapter(this, searchResultsList, this);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchUserAdapter);
    }

    private void setupClickListeners() {
        ImageView facebookChevron = findViewById(R.id.facebook_chevron);
        ImageView messengerChevron = findViewById(R.id.messenger_chevron);
        ImageView instagramChevron = findViewById(R.id.instagram_chevron);
        ImageView shareChevron = findViewById(R.id.share_chevron);

        facebookChevron.setOnClickListener(v -> shareToApp("com.facebook.katana", "Facebook"));
        messengerChevron.setOnClickListener(v -> shareToApp("com.facebook.orca", "Messenger"));
        instagramChevron.setOnClickListener(v -> shareToApp("com.instagram.android", "Instagram"));
        shareChevron.setOnClickListener(v -> shareToAllApps());
    }

    private void shareToApp(String packageName, String appName) {
        String shareText = "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://modis.app/invite/" + currentUserId;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.setPackage(packageName);
        try {
            startActivity(shareIntent);
        } catch (Exception e) {
            Toast.makeText(this, appName + " chưa được cài đặt!", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToAllApps() {
        String shareText = "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://modis.app/invite/" + currentUserId;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
    }

    private void setupAppIconClickListeners() {
        String shareText = "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://modis.app/invite/" + currentUserId;

        LinearLayout messengerLayout = findViewById(R.id.messenger_layout);
        LinearLayout facebookLayout = findViewById(R.id.facebook_layout);
        LinearLayout instagramLayout = findViewById(R.id.instagram_layout);
        LinearLayout shareLayout = findViewById(R.id.share_layout);

        messengerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                shareIntent.setPackage("com.facebook.orca");
                try {
                    startActivity(shareIntent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Messenger chưa được cài đặt!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        facebookLayout.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.facebook.katana");
            try {
                startActivity(shareIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Facebook chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        instagramLayout.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.instagram.android");
            try {
                startActivity(shareIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Instagram chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        shareLayout.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
        });
    }

    private void loadFriendList() {
        // Gọi API để lấy danh sách bạn bè từ Firebase
        Log.d("ListFriendActivity", "Loading friend list for userId: " + currentUserId);
        Call<FriendListResponse> call = apiService.getFriendList(currentUserId);
        call.enqueue(new Callback<FriendListResponse>() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call<FriendListResponse> call, @NonNull Response<FriendListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FriendListResponse friendListResponse = response.body();
                    Log.d("ListFriendActivity", "Friend count loaded: " + friendListResponse.getTotalFriends() + " for userId: " + currentUserId);
                    friendCount.setText(friendListResponse.getTotalFriends() + " người bạn đã được bổ sung");
// Hiển thị danh sách bạn bè hiện có trên Firebase
                    friendsList.clear();
                    friendsList.addAll(friendListResponse.getFriends());
                    friendAdapter.notifyDataSetChanged();
                } else {
                    Log.e("ListFriendActivity", "Friend list response not successful. Code: " + response.code() + " for userId: " + currentUserId);
                    friendsList.clear();
                    friendAdapter.notifyDataSetChanged();
                    friendCount.setText("0 người bạn đã được bổ sung");
                }
            }

            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onFailure(@NonNull Call<FriendListResponse> call, @NonNull Throwable t) {
                // Xử lý lỗi JSON parsing
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    Log.d("ListFriendActivity", "JSON parsing error - treating as success for userId: " + currentUserId);
                    friendsList.clear();
                    friendAdapter.notifyDataSetChanged();
                    friendCount.setText("0 người bạn đã được bổ sung");
                } else {
                    Log.e("ListFriendActivity", "Friend list load failure: " + t.getMessage() + " for userId: " + currentUserId, t);
                    Toast.makeText(ListFriendActivity.this, "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Tìm kiếm bạn bè realtime
    private void searchUsers(String query) {
        if (query.trim().isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
            loadFriendList();
            return;
        }

        SearchUserRequest request = new SearchUserRequest(query, currentUserId);
        Call<List<User>> call = apiService.searchUsers(request);
        call.enqueue(new Callback<List<User>>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> searchResults = response.body();
                    filterSearchResults(searchResults);
                } else {
                    searchResultsList.clear();
                    searchUserAdapter.notifyDataSetChanged();
                    searchResultsRecyclerView.setVisibility(View.GONE);
                    Toast.makeText(ListFriendActivity.this, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                searchResultsList.clear();
                searchUserAdapter.notifyDataSetChanged();
                searchResultsRecyclerView.setVisibility(View.GONE);
                Toast.makeText(ListFriendActivity.this, "Lỗi tìm kiếm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSearchResults(List<User> searchResults) {
        Call<List<FriendRequest>> call = apiService.getSentRequests(currentUserId);
        call.enqueue(new Callback<List<FriendRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<FriendRequest>> call, @NonNull Response<List<FriendRequest>> response) {
                List<User> filteredResults = new ArrayList<>();

                if (response.isSuccessful() && response.body() != null) {
                    List<FriendRequest> sentRequests = response.body();

                    for (User user : searchResults) {
                        if (user.getUserId().equals(currentUserId)) {
                            continue;
                        }

                        boolean shouldShow = true;
                        for (FriendRequest request : sentRequests) {
                            if ((request.getSenderId().equals(currentUserId) && request.getReceiverId().equals(user.getUserId())) ||
                                    (request.getReceiverId().equals(currentUserId) && request.getSenderId().equals(user.getUserId()))) {
                                if (!"REJECTED".equals(request.getStatus()) && !"CANCELLED".equals(request.getStatus())) {
                                    shouldShow = false;
                                }
                                break;
                            }
                        }

                        if (shouldShow) {
                            filteredResults.add(user);
                        }
                    }
                } else {
                    for (User user : searchResults) {
                        if (!user.getUserId().equals(currentUserId)) {
                            filteredResults.add(user);
                        }
                    }
                }
                updateSearchResults(filteredResults);
            }

            @Override
            public void onFailure(@NonNull Call<List<FriendRequest>> call, @NonNull Throwable t) {
                List<User> filteredResults = new ArrayList<>();
                for (User user : searchResults) {
                    if (!user.getUserId().equals(currentUserId)) {
                        filteredResults.add(user);
                    }
                }
                updateSearchResults(filteredResults);
            }
        });
    }

    // Cập nhật kết quả tìm kiếm
    @SuppressLint("NotifyDataSetChanged")
    private void updateSearchResults(List<User> results) {
        searchResultsList.clear();
        searchResultsList.addAll(results);
        searchUserAdapter.notifyDataSetChanged();

        if (results.isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
        } else {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAddFriendClick(User user) {
        addFriend(user);
    }

    // Thêm bạn bè
    private void addFriend(User user) {
        // Gửi lời mời kết bạn
        FriendRequest request = new FriendRequest();
        request.setSenderId(currentUserId);
        request.setReceiverId(user.getUserId());
        request.setStatus("PENDING");
        request.setTimestamp(System.currentTimeMillis());

        // Thêm thông tin sender để tránh null
        User senderUser = new User();
        senderUser.setUserId(currentUserId);
        senderUser.setUserName("Current User");
        senderUser.setFullName("Current User");
        request.setSender(senderUser);
        request.setSenderName("Current User");

        Log.d("ListFriendActivity", "Sending friend request: " +
                "SenderId=" + request.getSenderId() +
                ", ReceiverId=" + request.getReceiverId() +
                ", Status=" + request.getStatus() +
                ", SenderName=" + request.getSenderName());

        Call<Void> call = apiService.sendFriendRequest(request);
        call.enqueue(new Callback<Void>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ListFriendActivity", "Send friend request successful");
                    Toast.makeText(ListFriendActivity.this, "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show();
                    FriendRequest newRequest = new FriendRequest();
                    newRequest.setSenderId(currentUserId);
                    newRequest.setReceiverId(user.getUserId());
                    newRequest.setStatus("PENDING");
                    newRequest.setTimestamp(System.currentTimeMillis());
                    newRequest.setSenderName("Current User");

                    sentRequestsList.add(0, newRequest);
                    sentRequestAdapter.notifyItemInserted(0);

                    TextView sentRequestsTitle = findViewById(R.id.sent_requests);
                    if (sentRequestsTitle != null && sentRequestsTitle.getVisibility() == View.GONE) {
                        sentRequestsTitle.setVisibility(View.VISIBLE);
                    }
                    if (sentRequestsRecyclerView.getVisibility() == View.GONE) {
                        sentRequestsRecyclerView.setVisibility(View.VISIBLE);
                    }

                    searchResultsList.remove(user);
                    searchUserAdapter.notifyDataSetChanged();

                    if (searchResultsList.isEmpty()) {
                        searchResultsRecyclerView.setVisibility(View.GONE);
                    }

                    new android.os.Handler().postDelayed(() -> {
                        loadSentRequests();
                    }, 200);

                } else {
                    String errorMessage = "Không thể gửi lời mời kết bạn";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                            Log.e("ListFriendActivity", "Send friend request error: " + errorMessage);
                        } catch (IOException e) {
                            Log.e("ListFriendActivity", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(ListFriendActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                    searchUserAdapter.notifyDataSetChanged();
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    Log.d("ListFriendActivity", "JSON parsing error - treating as success");
                    Toast.makeText(ListFriendActivity.this, "Đã gửi lời mời kết bạn!", Toast.LENGTH_SHORT).show();

                    FriendRequest newRequest = new FriendRequest();
                    newRequest.setSenderId(currentUserId);
                    newRequest.setReceiverId(user.getUserId());
                    newRequest.setStatus("PENDING");
                    newRequest.setTimestamp(System.currentTimeMillis());
                    newRequest.setSenderName("Current User");

                    sentRequestsList.add(0, newRequest);
                    sentRequestAdapter.notifyItemInserted(0);
                    TextView sentRequestsTitle = findViewById(R.id.sent_requests);
                    if (sentRequestsTitle != null && sentRequestsTitle.getVisibility() == View.GONE) {
                        sentRequestsTitle.setVisibility(View.VISIBLE);
                    }
                    if (sentRequestsRecyclerView.getVisibility() == View.GONE) {
                        sentRequestsRecyclerView.setVisibility(View.VISIBLE);
                    }

                    searchResultsList.remove(user);
                    searchUserAdapter.notifyDataSetChanged();

                    if (searchResultsList.isEmpty()) {
                        searchResultsRecyclerView.setVisibility(View.GONE);
                    }

                    new android.os.Handler().postDelayed(() -> {
                        loadSentRequests();
                    }, 200);

                } else {
                    Toast.makeText(ListFriendActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    searchUserAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //xóa bạn
    private void removeFriend(User user) {
        Call<String> call = apiService.removeFriend(currentUserId, user.getUserId());
        call.enqueue(new Callback<String>() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ListFriendActivity.this, "Đã xóa bạn thành công", Toast.LENGTH_SHORT).show();
                    friendsList.remove(user);
                    friendAdapter.notifyDataSetChanged();
                    friendCount.setText(friendsList.size() + " người bạn đã được bổ sung");
                    Toast.makeText(ListFriendActivity.this, "Đã xóa bạn thành công", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMessage = "Không thể xóa bạn bè";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("ListFriendActivity", "Error reading error body", e);
                        }
                    }
                    Toast.makeText(ListFriendActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    friendsList.remove(user);
                    friendAdapter.notifyDataSetChanged();
                    friendCount.setText(friendsList.size() + " người bạn đã được bổ sung");
                    Toast.makeText(ListFriendActivity.this, "Đã xóa bạn thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ListFriendActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showConfirmationDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.confirmation_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
        if (titleText != null) {
            String userName = user.getFullName() != null ? user.getFullName() : user.getUserName();
            if (userName == null || userName.isEmpty()) {
                userName = " người bạn này";
            }
            titleText.setText("Xóa " + userName + " khỏi Modis của bạn?");
        }

        // Ánh xạ các nút
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            removeFriend(user);
        });
    }

    // share qua app khác (khi người dùng chưa có tài khoản)
    private void shareToSocialMedia(String platform) {
        ShareRequest request = new ShareRequest(currentUserId, "Join me on Locket!");
        Call<String> call = apiService.shareToSocialMedia(platform, request);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ListFriendActivity.this, "Shared to " + platform, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ListFriendActivity.this, "Failed to share", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Toast.makeText(ListFriendActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load danh sách lời mời kết bạn đang chờ
    private void loadPendingRequests() {
        Call<List<FriendRequest>> call = apiService.getPendingRequests(currentUserId);
        call.enqueue(new Callback<List<FriendRequest>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<FriendRequest>> call, @NonNull Response<List<FriendRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FriendRequest> pendingRequests = response.body();
                    pendingRequestsList.clear();
                    pendingRequestsList.addAll(pendingRequests);
                    pendingRequestAdapter.notifyDataSetChanged();
                } else {
                    pendingRequestsList.clear();
                    pendingRequestAdapter.notifyDataSetChanged();
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFailure(@NonNull Call<List<FriendRequest>> call, @NonNull Throwable t) {
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    pendingRequestsList.clear();
                    pendingRequestAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(ListFriendActivity.this, "Không thể tải lời mời kết bạn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Chấp nhận lời mời kết bạn
    private void acceptFriendRequest(FriendRequest request) {
        Call<Void> call = apiService.acceptFriendRequest(request.getFriendRequestId());
        call.enqueue(new Callback<Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ListFriendActivity.this, "Đã chấp nhận lời mời kết bạn", Toast.LENGTH_SHORT).show();
                    int position = pendingRequestsList.indexOf(request);
                    if (position != -1) {
                        pendingRequestsList.remove(position);
                        pendingRequestAdapter.notifyItemRemoved(position);
                    }
// Cập nhật số lượng bạn bè
                    friendCount.setText((friendsList.size() + 1) + " người bạn đã được bổ sung");
                    loadPendingRequests();
                    loadFriendList();
                } else {
                    Toast.makeText(ListFriendActivity.this, "Không thể chấp nhận lời mời", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ListFriendActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Từ chối lời mời kết bạn
    private void rejectFriendRequest(FriendRequest request) {
        Call<Void> call = apiService.rejectFriendRequest(request.getFriendRequestId());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    int position = pendingRequestsList.indexOf(request);
                    if (position != -1) {
                        pendingRequestsList.remove(position);
                        pendingRequestAdapter.notifyItemRemoved(position);
                    }
                    loadPendingRequests();
                } else {
                    Toast.makeText(ListFriendActivity.this, "Không thể từ chối lời mời", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ListFriendActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelSentRequest(FriendRequest request) {
        try {
            Call<String> call = apiService.cancelFriendRequest(request.getFriendRequestId());
            call.enqueue(new Callback<String>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(ListFriendActivity.this, "Đã hủy lời mời kết bạn", Toast.LENGTH_SHORT).show();
                        int position = sentRequestsList.indexOf(request);
                        if (position != -1) {
                            sentRequestsList.remove(position);
                            sentRequestAdapter.notifyItemRemoved(position);
                        }
                        loadSentRequests();
                    } else {
                        String errorMessage = response.message();
                        if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                            } catch (Exception e) {
                                Log.e("ListFriendActivity", "Error reading error body: " + e.getMessage());
                            }
                        }
                        Toast.makeText(ListFriendActivity.this, "Lỗi hủy lời mời: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    if (t.getCause() != null) {
                        Log.e("ListFriendActivity", "Cancel sent request cause: " + t.getCause().getMessage());
                    }

                    if (t.getMessage() != null && t.getMessage().contains("JSON")) {
                        Toast.makeText(ListFriendActivity.this, "Đã hủy lời mời kết bạn", Toast.LENGTH_SHORT).show();
                        int position = sentRequestsList.indexOf(request);
                        if (position != -1) {
                            sentRequestsList.remove(position);
                            sentRequestAdapter.notifyItemRemoved(position);
                        }
                    } else {
                        Toast.makeText(ListFriendActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ListFriendActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSentRequests() {
        if (apiService == null) {
            Log.e("ListFriendActivity", "API service is null");
            return;
        }

        Call<List<FriendRequest>> call = apiService.getSentRequests(currentUserId);
        call.enqueue(new Callback<List<FriendRequest>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<FriendRequest>> call, @NonNull Response<List<FriendRequest>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FriendRequest> sentRequests = response.body();
                    Log.d("ListFriendActivity", "Sent requests loaded: " + sentRequests.size() + " requests");

                    for (FriendRequest request : sentRequests) {
                        if (request.getSenderName() == null || request.getSenderName().isEmpty()) {
                            request.setSenderName("Current User");
                        }
                        if (request.getSender() == null) {
                            User senderUser = new User();
                            senderUser.setUserId(currentUserId);
                            senderUser.setUserName("Current User");
                            senderUser.setFullName("Current User");
                            request.setSender(senderUser);
                        }
                    }

                    updateSentRequestsList(sentRequests);
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("ListFriendActivity", "Error body: " + errorBody);
                        } catch (IOException e) {
                            Log.e("ListFriendActivity", "Error reading error body", e);
                        }
                    }

                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onFailure(@NonNull Call<List<FriendRequest>> call, @NonNull Throwable t) {
                updateSentRequestsList(new ArrayList<>());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateSentRequestsList(List<FriendRequest> sentRequests) {
        if (sentRequestsList != null && sentRequestAdapter != null) {
            sentRequestsList.clear();
            sentRequestsList.addAll(sentRequests);
            sentRequestAdapter.notifyDataSetChanged();

            TextView sentRequestsTitle = findViewById(R.id.sent_requests);
            if (sentRequestsTitle != null) {
                if (sentRequests.isEmpty()) {
                    sentRequestsTitle.setVisibility(View.GONE);
                    if (sentRequestsRecyclerView != null) {
                        sentRequestsRecyclerView.setVisibility(View.GONE);
                    }
                } else {
                    sentRequestsTitle.setVisibility(View.VISIBLE);
                    if (sentRequestsRecyclerView != null) {
                        sentRequestsRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dữ liệu khi quay lại để đảm bảo đồng bộ
        if (currentUserId != null && !currentUserId.isEmpty() && !currentUserId.equals(" ")) {
            Log.d("ListFriendActivity", "Refreshing data on resume for userId: " + currentUserId);
            loadFriendList();
            loadPendingRequests();
            loadSentRequests();
        }
    }
}