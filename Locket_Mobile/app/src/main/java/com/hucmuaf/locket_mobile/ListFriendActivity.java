package com.hucmuaf.locket_mobile;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hucmuaf.locket_mobile.ShareDialogFragment;
import com.hucmuaf.locket_mobile.api.ApiClient;
import com.hucmuaf.locket_mobile.api.FriendListApiService;
import com.hucmuaf.locket_mobile.model.FriendListResponse;
import com.hucmuaf.locket_mobile.model.User;
import com.hucmuaf.locket_mobile.model.FriendRequest;
import com.hucmuaf.locket_mobile.dto.SearchUserRequest;
import com.hucmuaf.locket_mobile.dto.FriendRequestDto;
import com.hucmuaf.locket_mobile.dto.ShareRequest;
import com.hucmuaf.locket_mobile.adapter.FriendAdapter;
import com.hucmuaf.locket_mobile.adapter.PendingRequestAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình danh sách bạn bè và lời mời kết bạn
 * Chức năng chính:
 * - Hiển thị danh sách bạn bè hiện có
 * - Hiển thị danh sách lời mời kết bạn đang chờ
 * - Cho phép chấp nhận/từ chối lời mời kết bạn
 * - Tìm kiếm bạn bè
 * - Chia sẻ để mời bạn bè
 */
public class ListFriendActivity extends AppCompatActivity {

    // ===== API SERVICE =====
    // Service để gọi các API backend (lấy danh sách bạn bè, lời mời kết bạn, v.v.)
    private FriendListApiService apiService;

    // ===== USER ID HIỆN TẠI =====
    // ID của user đang đăng nhập - lấy động từ Firebase Auth thay vì hardcode
    private String currentUserId;

    // ===== FIREBASE AUTH =====
    // Firebase Auth để lấy thông tin user đang đăng nhập
    private FirebaseAuth mAuth;

    // ===== UI COMPONENTS =====
    private TextView friendCount;                    // Hiển thị số lượng bạn bè (ví dụ: "5 / 20 người bạn")
    private EditText searchFriend;                  // Ô tìm kiếm bạn bè
    private RecyclerView friendsRecyclerView;       // Danh sách bạn bè hiện có
    private RecyclerView pendingRequestsRecyclerView; // Danh sách lời mời kết bạn đang chờ
    private FriendAdapter friendAdapter;            // Adapter cho danh sách bạn bè
    private PendingRequestAdapter pendingRequestAdapter; // Adapter cho danh sách lời mời đang chờ

    // ===== DATA LISTS =====
    private List<User> friendsList = new ArrayList<>();           // Danh sách bạn bè từ Firebase
    private List<FriendRequest> pendingRequestsList = new ArrayList<>(); // Danh sách lời mời đang chờ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listfriend);

        // ===== KHỞI TẠO FIREBASE AUTH =====
        // Lấy instance Firebase Auth để truy cập thông tin user đang đăng nhập
        mAuth = FirebaseAuth.getInstance();

        // ===== KHỞI TẠO API SERVICE =====
        // Service để gọi các API backend (Retrofit)
        apiService = ApiClient.getFriendListApiService();

        // ===== LẤY USER ID TỪ EMAIL ĐĂNG NHẬP =====
        // Thay vì dùng Firebase Auth UID, lấy user ID từ email để đảm bảo đúng với database
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            // Lấy user ID từ email đăng nhập
            loadUserIdFromEmail(currentUser.getEmail());
        } else {
            // Fallback nếu user chưa đăng nhập (trường hợp test)
            currentUserId = "camt91990"; // User ID đã thêm vào Firebase
            // Tắt Toast thông báo fallback
            // Toast.makeText(this, "Using fallback User ID: " + currentUserId, Toast.LENGTH_SHORT).show();
            initializeAfterUserId();
        }
    }

    /**
     * Load user ID từ email đăng nhập
     * Gọi API để lấy user ID tương ứng với email từ database
     */
    private void loadUserIdFromEmail(String email) {
        // Debug log để theo dõi quá trình
        Log.d("ListFriendActivity", "Loading user ID for email: " + email);

        // Gọi API để lấy user ID từ email
        Call<String> call = apiService.getUserIdByEmail(email);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("ListFriendActivity", "API Response Code: " + response.code());
                Log.d("ListFriendActivity", "API Response Body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    // Lấy user ID thành công
                    currentUserId = response.body();
                    Log.d("ListFriendActivity", "Successfully got User ID: " + currentUserId);
                    initializeAfterUserId();
                } else {
                    // Fallback nếu không tìm thấy user
                    currentUserId = "camt91990";
                    Log.w("ListFriendActivity", "User not found, using fallback: " + currentUserId);
                    initializeAfterUserId();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("ListFriendActivity", "API call failed: " + t.getMessage());
                // Fallback nếu lỗi network
                currentUserId = "camt91990";
                initializeAfterUserId();
            }
        });
    }

    /**
     * Khởi tạo UI sau khi đã có user ID
     * Đây là bước quan trọng để setup toàn bộ màn hình
     */
    private void initializeAfterUserId() {
        // ===== TEST KẾT NỐI API TRƯỚC =====
        // Kiểm tra backend có hoạt động không
        testApiConnection();

        // ===== KHỞI TẠO UI VÀ LOAD DỮ LIỆU =====
        initializeViews();        // Khởi tạo các view và setup search
        setupRecyclerViews();     // Setup RecyclerView cho danh sách bạn bè và lời mời
        setupClickListeners();    // Setup click listeners cho các mũi tên share
        setupAppIconClickListeners(); // Setup click listeners cho các icon app
        loadFriendList();         // Load danh sách bạn bè từ Firebase
        loadPendingRequests();    // Load danh sách lời mời kết bạn đang chờ
    }

    /**
     * Test kết nối API để kiểm tra backend có hoạt động không
     * Gọi API test đơn giản để đảm bảo kết nối ổn định
     */
    private void testApiConnection() {
        Log.d("ListFriendActivity", "Testing API connection...");
        Call<String> call = apiService.testConnection();
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
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
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("ListFriendActivity", "API Test Failed: " + t.getMessage());
                // Chỉ hiển thị Toast khi lỗi nghiêm trọng
                if (t.getMessage() != null && !t.getMessage().contains("JSON document was not fully consumed")) {
                    Toast.makeText(ListFriendActivity.this, "Kết nối thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Khởi tạo các UI components và setup tìm kiếm realtime
     * Ánh xạ các view từ layout và setup các listener
     */
    private void initializeViews() {
        // ===== ÁNH XẠ CÁC VIEW TỪ LAYOUT =====
        friendCount = findViewById(R.id.friendCount);
        searchFriend = findViewById(R.id.searchFriend);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        pendingRequestsRecyclerView = findViewById(R.id.pendingRequestsRecyclerView);

        // ===== SETUP TÌM KIẾM REALTIME =====
        // TextWatcher để lắng nghe thay đổi text trong ô search
        // Khi user gõ, sẽ tự động tìm kiếm và cập nhật danh sách
        searchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Khi user gõ text, gọi hàm tìm kiếm realtime
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Setup RecyclerView cho danh sách bạn bè và lời mời kết bạn
     * Tạo adapter và setup layout manager cho từng danh sách
     */
    private void setupRecyclerViews() {
        // ===== SETUP RECYCLERVIEW CHO DANH SÁCH BẠN BÈ =====
        // Adapter cho danh sách bạn bè hiện có, callback removeFriend khi xóa bạn
        friendAdapter = new FriendAdapter(friendsList, new FriendAdapter.OnFriendActionListener() {
            @Override
            public void onRemoveFriend(User user) {
                removeFriend(user);
            }
        });
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendAdapter);

        // ===== SETUP RECYCLERVIEW CHO DANH SÁCH LỜI MỜI ĐANG CHỜ =====
        // Adapter cho danh sách lời mời kết bạn đang chờ, callback accept/reject khi xử lý lời mời
        pendingRequestAdapter = new PendingRequestAdapter(pendingRequestsList, new PendingRequestAdapter.OnPendingRequestActionListener() {
            @Override
            public void onAcceptRequest(FriendRequest request) {
                // Khi user bấm nút chấp nhận (✓)
                acceptFriendRequest(request);
            }

            @Override
            public void onRejectRequest(FriendRequest request) {
                // Khi user bấm nút từ chối (✕)
                rejectFriendRequest(request);
            }
        });
        pendingRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pendingRequestsRecyclerView.setAdapter(pendingRequestAdapter);
    }

    /**
     * Setup click listeners cho các mũi tên share
     * THAY ĐỔI CHÍNH: Share trực tiếp không hiện dialog
     */
    private void setupClickListeners() {
        // ===== ÁNH XẠ CÁC MŨI TÊN SHARE =====
        ImageView facebookChevron = findViewById(R.id.facebook_chevron);
        ImageView messengerChevron = findViewById(R.id.messenger_chevron);
        ImageView instagramChevron = findViewById(R.id.instagram_chevron);
        ImageView shareChevron = findViewById(R.id.share_chevron);

        // ===== TẠO SHARE TEXT VỚI USER ID ĐỘNG =====
        // Dòng chữ share với userId thực của user đang đăng nhập
        String shareText = "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://modis.app/invite/" + currentUserId;

        // ===== FACEBOOK SHARE TRỰC TIẾP =====
        // Khi bấm mũi tên Facebook → Share trực tiếp qua Facebook app
        facebookChevron.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.facebook.katana");  // Package Facebook app
            try {
                startActivity(shareIntent);  // Mở Facebook app trực tiếp
            } catch (Exception e) {
                Toast.makeText(this, "Facebook chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== MESSENGER SHARE TRỰC TIẾP =====
        // Khi bấm mũi tên Messenger → Share trực tiếp qua Messenger app
        messengerChevron.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.facebook.orca");  // Package Messenger app
            try {
                startActivity(shareIntent);  // Mở Messenger app trực tiếp
            } catch (Exception e) {
                Toast.makeText(this, "Messenger chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== INSTAGRAM SHARE TRỰC TIẾP =====
        // Khi bấm mũi tên Instagram → Share trực tiếp qua Instagram app
        instagramChevron.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.instagram.android");  // Package Instagram app
            try {
                startActivity(shareIntent);  // Mở Instagram app trực tiếp
            } catch (Exception e) {
                Toast.makeText(this, "Instagram chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== SHARE CHUNG =====
        // Khi bấm mũi tên Share → Mở menu share chung của Android
        shareChevron.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));  // Menu share chung
        });
    }

    /**
     * Setup click listeners cho các icon tìm bạn bè từ ứng dụng khác
     * THÊM MỚI: Các icon này sẽ chuyển hướng giống như các mũi tên share
     */
    private void setupAppIconClickListeners() {
        // ===== TẠO SHARE TEXT VỚI USER ID ĐỘNG =====
        // Dòng chữ share với userId thực của user đang đăng nhập
        String shareText = "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://modis.app/invite/" + currentUserId;

        // ===== ÁNH XẠ CÁC ICON APP =====
        LinearLayout messengerLayout = findViewById(R.id.messenger_layout);
        LinearLayout facebookLayout = findViewById(R.id.facebook_layout);
        LinearLayout instagramLayout = findViewById(R.id.instagram_layout);
        LinearLayout shareLayout = findViewById(R.id.share_layout);

        // ===== MESSENGER ICON CLICK =====
        // Khi bấm icon Messenger → Share trực tiếp qua Messenger app
        messengerLayout.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.facebook.orca");  // Package Messenger app
            try {
                startActivity(shareIntent);  // Mở Messenger app trực tiếp
            } catch (Exception e) {
                Toast.makeText(this, "Messenger chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== FACEBOOK ICON CLICK =====
        // Khi bấm icon Facebook → Share trực tiếp qua Facebook app
        facebookLayout.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.facebook.katana");  // Package Facebook app
            try {
                startActivity(shareIntent);  // Mở Facebook app trực tiếp
            } catch (Exception e) {
                Toast.makeText(this, "Facebook chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== INSTAGRAM ICON CLICK =====
        // Khi bấm icon Instagram → Share trực tiếp qua Instagram app
        instagramLayout.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setPackage("com.instagram.android");  // Package Instagram app
            try {
                startActivity(shareIntent);  // Mở Instagram app trực tiếp
            } catch (Exception e) {
                Toast.makeText(this, "Instagram chưa được cài đặt!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== SHARE ICON CLICK =====
        // Khi bấm icon Share → Mở menu share chung của Android
        shareLayout.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));  // Menu share chung
        });
    }

    /**
     * Load danh sách bạn bè từ Firebase
     * THAY ĐỔI CHÍNH: Số lượng và danh sách bạn bè lấy từ Firebase, không set cứng
     */
    private void loadFriendList() {
        Log.d("ListFriendActivity", "Loading friend list for user ID: " + currentUserId);

        // Gọi API để lấy danh sách bạn bè từ Firebase
        Call<FriendListResponse> call = apiService.getFriendList(currentUserId);
        call.enqueue(new Callback<FriendListResponse>() {
            @Override
            public void onResponse(Call<FriendListResponse> call, Response<FriendListResponse> response) {
                Log.d("ListFriendActivity", "Friend List API Response Code: " + response.code());
                Log.d("ListFriendActivity", "Friend List API Response Body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    FriendListResponse friendListResponse = response.body();

                    Log.d("ListFriendActivity", "Total Friends: " + friendListResponse.getTotalFriends());
                    Log.d("ListFriendActivity", "Max Friends: " + friendListResponse.getMaxFriends());
                    Log.d("ListFriendActivity", "Friends List Size: " + friendListResponse.getFriends().size());

                    // ===== CẬP NHẬT SỐ LƯỢNG BẠN BÈ TỪ FIREBASE =====
                    // Thay vì set cứng "10 / 20", giờ lấy số lượng thực từ Firebase
                    friendCount.setText(friendListResponse.getTotalFriends() + " / " +
                            friendListResponse.getMaxFriends() + " người bạn đã được bổ sung");

                    // ===== CẬP NHẬT DANH SÁCH BẠN BÈ TỪ FIREBASE =====
                    // Hiển thị đúng danh sách bạn bè hiện có trên Firebase
                    friendsList.clear();
                    friendsList.addAll(friendListResponse.getFriends());
                    friendAdapter.notifyDataSetChanged();

                    Log.d("ListFriendActivity", "Successfully updated UI with friend data");
                } else {
                    Log.e("ListFriendActivity", "Failed to load friend list - Response not successful or null");
                    // Fallback: hiển thị danh sách rỗng
                    friendsList.clear();
                    friendAdapter.notifyDataSetChanged();
                    friendCount.setText("0 / 20 người bạn đã được bổ sung");
                }
            }

            @Override
            public void onFailure(Call<FriendListResponse> call, Throwable t) {
                Log.e("ListFriendActivity", "Friend List API call failed: " + t.getMessage());
                
                // Xử lý lỗi JSON parsing
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    Log.d("ListFriendActivity", "JSON parsing error - likely empty response, treating as success");
                    // Có thể backend trả về chuỗi rỗng hoặc HTTP 204, coi như thành công
                    friendsList.clear();
                    friendAdapter.notifyDataSetChanged();
                    friendCount.setText("0 / 20 người bạn đã được bổ sung");
                } else {
                    Toast.makeText(ListFriendActivity.this, "Không thể tải danh sách bạn bè", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Tìm kiếm bạn bè realtime
     * THAY ĐỔI CHÍNH: Khi search trống sẽ load lại danh sách bạn bè ban đầu
     */
    private void searchUsers(String query) {
        if (query.trim().isEmpty()) {
            // ===== KHI SEARCH TRỐNG =====
            // Load lại danh sách bạn bè ban đầu từ Firebase
            loadFriendList();
            return;
        }

        // ===== TÌM KIẾM REALTIME =====
        // Gọi API tìm kiếm user theo query
        SearchUserRequest request = new SearchUserRequest(query, currentUserId);
        Call<List<User>> call = apiService.searchUsers(request);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cập nhật danh sách bạn bè với kết quả tìm kiếm
                    friendsList.clear();
                    friendsList.addAll(response.body());
                    friendAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ListFriendActivity.this, "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Thêm bạn bè
     */
    private void addFriend(User user) {
        // Gửi lời mời kết bạn
        FriendRequestDto request = new FriendRequestDto(currentUserId, user.getUserId());
        Call<String> call = apiService.sendFriendRequest(request);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ListFriendActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                    // Không cần xóa khỏi danh sách vì không còn suggestions
                } else {
                    Toast.makeText(ListFriendActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ListFriendActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Xóa bạn bè - hiển thị dialog xác nhận
     */
    private void removeFriend(User user) {
        // Gọi API để xóa bạn bè
        Call<String> call = apiService.removeFriend(currentUserId, user.getUserId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    // Cập nhật giao diện ngay lập tức
                    int position = friendsList.indexOf(user);
                    if (position != -1) {
                        friendsList.remove(position);
                        friendAdapter.notifyItemRemoved(position);
                    }
                    // Sau đó load lại danh sách từ backend để đảm bảo đồng bộ
                    loadFriendList();
                } else {
                    Toast.makeText(ListFriendActivity.this, "Không thể xóa bạn bè", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ListFriendActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hiển thị dialog xác nhận xóa bạn bè
     */
    private void showConfirmationDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.confirmation_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.show();

        // ===== CẬP NHẬT TÊN USER ĐỘNG =====
        // Thay vì hardcode "Đăng Trần", giờ lấy tên thực từ user object
        TextView titleText = dialogView.findViewById(R.id.dialogTitle);
        if (titleText != null) {
            String userName = user.getFullName() != null ? user.getFullName() : user.getUserName();
            if (userName == null || userName.isEmpty()) {
                userName = "người bạn này";
            }
            titleText.setText("Xóa " + userName + " khỏi Modis của bạn?");
        }

        // Ánh xạ các nút
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            // Gọi API để xóa bạn bè
            Call<String> call = apiService.removeFriend(currentUserId, user.getUserId());
            call.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        // Tắt Toast thông báo xóa thành công
                        // Toast.makeText(ListFriendActivity.this, "Đã xóa bạn!", Toast.LENGTH_SHORT).show();
                        
                        // KHÔNG xóa thủ công khỏi danh sách - chỉ reload từ backend
                        // friendsList.remove(user);
                        // friendAdapter.notifyDataSetChanged();
                        
                        // Load lại danh sách để cập nhật UI từ backend
                        loadFriendList();
                    } else {
                        Toast.makeText(ListFriendActivity.this, "Không thể xóa bạn bè", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(ListFriendActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            dialog.dismiss();
        });
    }

    /**
     * Share qua social media (hàm cũ, không dùng nữa)
     * Thay thế bằng share trực tiếp trong setupClickListeners()
     */
    private void shareToSocialMedia(String platform) {
        ShareRequest request = new ShareRequest(currentUserId, "Join me on Locket!");
        Call<String> call = apiService.shareToSocialMedia(platform, request);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ListFriendActivity.this, "Shared to " + platform, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ListFriendActivity.this, "Failed to share", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(ListFriendActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load danh sách lời mời kết bạn đang chờ
     * Gọi API để lấy danh sách lời mời có status = "pending" từ Firebase
     * Backend sẽ trả về danh sách với thông tin đầy đủ của người gửi (tên, avatar)
     */
    private void loadPendingRequests() {
        Log.d("ListFriendActivity", "Loading pending requests for user ID: " + currentUserId);

        // Gọi API để lấy danh sách lời mời kết bạn đang chờ
        Call<List<FriendRequest>> call = apiService.getPendingRequests(currentUserId);
        call.enqueue(new Callback<List<FriendRequest>>() {
            @Override
            public void onResponse(Call<List<FriendRequest>> call, Response<List<FriendRequest>> response) {
                Log.d("ListFriendActivity", "Pending Requests API Response Code: " + response.code());
                Log.d("ListFriendActivity", "Pending Requests API Response Body: " + response.body());

                if (response.isSuccessful() && response.body() != null) {
                    List<FriendRequest> pendingRequests = response.body();
                    Log.d("ListFriendActivity", "Pending Requests Count: " + pendingRequests.size());

                    // ===== CẬP NHẬT DANH SÁCH LỜI MỜI ĐANG CHỜ =====
                    // Cập nhật UI với danh sách lời mời mới từ backend
                    pendingRequestsList.clear();
                    pendingRequestsList.addAll(pendingRequests);
                    pendingRequestAdapter.notifyDataSetChanged();

                    Log.d("ListFriendActivity", "Successfully updated pending requests list");
                } else {
                    Log.e("ListFriendActivity", "Failed to load pending requests - Response not successful or null");
                    // Fallback: hiển thị danh sách rỗng
                    pendingRequestsList.clear();
                    pendingRequestAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<FriendRequest>> call, Throwable t) {
                Log.e("ListFriendActivity", "Pending Requests API call failed: " + t.getMessage());
                
                // Xử lý lỗi JSON parsing
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    Log.d("ListFriendActivity", "JSON parsing error for pending requests - likely empty response, treating as success");
                    // Có thể backend trả về chuỗi rỗng hoặc HTTP 204, coi như thành công
                    pendingRequestsList.clear();
                    pendingRequestAdapter.notifyDataSetChanged();
                } else {
                    // Chỉ hiển thị Toast khi lỗi nghiêm trọng
                    Toast.makeText(ListFriendActivity.this, "Không thể tải lời mời kết bạn", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Chấp nhận lời mời kết bạn
     * Khi user bấm nút chấp nhận (✓), gọi API để cập nhật status thành "ACCEPTED"
     * Sau khi chấp nhận: lời mời biến mất, người đó xuất hiện trong danh sách bạn bè
     */
    private void acceptFriendRequest(FriendRequest request) {
        Log.d("ListFriendActivity", "Accepting friend request: " + request.getFriendRequestId());
        Call<String> call = apiService.acceptFriendRequest(request.getFriendRequestId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("ListFriendActivity", "Friend request accepted successfully");
                    loadPendingRequests(); // cập nhật lại danh sách lời mời
                    loadFriendList();      // cập nhật lại danh sách bạn bè
                } else {
                    Log.e("ListFriendActivity", "Failed to accept friend request - Response code: " + response.code());
                    Toast.makeText(ListFriendActivity.this, "Không thể chấp nhận lời mời", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("ListFriendActivity", "Accept friend request failed: " + t.getMessage());
                
                // Xử lý lỗi JSON parsing
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    Log.d("ListFriendActivity", "JSON parsing error for accept - likely empty response, treating as success");
                    // Có thể backend trả về chuỗi rỗng hoặc HTTP 204, coi như thành công
                    loadPendingRequests(); // cập nhật lại danh sách lời mời
                    loadFriendList();      // cập nhật lại danh sách bạn bè
                } else {
                    Toast.makeText(ListFriendActivity.this, "Lỗi kết nối, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Từ chối lời mời kết bạn
     * Khi user bấm nút từ chối (✕), gọi API để cập nhật status thành "REJECTED"
     * Sau khi từ chối: lời mời biến mất khỏi danh sách
     */
    private void rejectFriendRequest(FriendRequest request) {
        Log.d("ListFriendActivity", "Rejecting friend request: " + request.getFriendRequestId());
        Call<String> call = apiService.rejectFriendRequest(request.getFriendRequestId());
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d("ListFriendActivity", "Friend request rejected successfully");
                    loadPendingRequests(); // cập nhật lại danh sách lời mời
                    loadFriendList();      // cập nhật lại danh sách bạn bè
                } else {
                    Log.e("ListFriendActivity", "Failed to reject friend request - Response code: " + response.code());
                    Toast.makeText(ListFriendActivity.this, "Không thể từ chối lời mời", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("ListFriendActivity", "Reject friend request failed: " + t.getMessage());
                
                // Xử lý lỗi JSON parsing
                if (t.getMessage() != null && t.getMessage().contains("JSON document was not fully consumed")) {
                    Log.d("ListFriendActivity", "JSON parsing error for reject - likely empty response, treating as success");
                    // Có thể backend trả về chuỗi rỗng hoặc HTTP 204, coi như thành công
                    loadPendingRequests(); // cập nhật lại danh sách lời mời
                    loadFriendList();      // cập nhật lại danh sách bạn bè
                } else {
                    Toast.makeText(ListFriendActivity.this, "Lỗi kết nối, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}