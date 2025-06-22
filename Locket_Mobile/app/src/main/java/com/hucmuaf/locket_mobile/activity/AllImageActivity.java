package com.hucmuaf.locket_mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.adapter.ImageAdapter;
import com.hucmuaf.locket_mobile.adapter.ItemFriendAdapter;
import com.hucmuaf.locket_mobile.adapter.PhotoAdapter;
import com.hucmuaf.locket_mobile.inteface.OnImageClickListener;
import com.hucmuaf.locket_mobile.modedb.Image;
import com.hucmuaf.locket_mobile.modedb.User;
import com.hucmuaf.locket_mobile.repo.ImageResponsitory;
import com.hucmuaf.locket_mobile.repo.ImageLoadCallback;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.FirebaseService;
import com.hucmuaf.locket_mobile.service.FriendRequestService;
import com.hucmuaf.locket_mobile.service.OnFriendLoadedListener;
import com.hucmuaf.locket_mobile.service.OnImagesLoadedListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllImageActivity extends AppCompatActivity{
   //View
    private RecyclerView photoGrid;
    private RecyclerView listFriendView;

    //Adapter
    private ImageAdapter imageAdapter;
    private ItemFriendAdapter friendAdapter;

    //Firebase
    private FriendRequestService frService;
    private ImageResponsitory imageRepo;
    private FirebaseAuth mAuth;

    private Set<String> listFriendIds;
    private List<User> listFriend;
    private List<Image> allPhotos;
    private String currentUserId = null; //lấy từ session/login

    private String friendId = "ALL";
    private String friendName = "Tất cả bạn bè";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        imageRepo = new ImageResponsitory(FirebaseService.getInstance());
        frService = ApiClient.getFriendRequestService();
        listFriendIds = new HashSet<>();
        listFriend = new ArrayList<>();
        allPhotos = new ArrayList<>();
        //lấy ra user hiện tại
        mAuth = FirebaseService.getInstance().getAuth();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        currentUserId = firebaseUser != null ? firebaseUser.getUid() : null;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_images);

        //Xử lý hiển thị danh sách bạn bè
        View maskView = findViewById(R.id.mask);
        LinearLayout layout = findViewById(R.id.friends_board);
        LinearLayout down_toggle = findViewById(R.id.all_friends);

        down_toggle.setOnClickListener(v -> {
            maskView.setVisibility(View.VISIBLE);
            layout.setVisibility(View.VISIBLE);
        });

        maskView.setOnClickListener(e -> {
            maskView.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        });

        TextView titleFriend = findViewById(R.id.title);

        TextView tvName = findViewById(R.id.tvName);

        listFriendView = findViewById(R.id.list_friends);
        listFriendView.setLayoutManager(new LinearLayoutManager(this));

        listFriend = new ArrayList<>();

        friendAdapter = new ItemFriendAdapter(AllImageActivity.this, listFriend, new ItemFriendAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User user) {
                filterImagesBySenderId(user.getUserId());
                friendId = user.getUserId();
                friendName = user.getFullName();
                titleFriend.setText(user.getFullName());
                maskView.setVisibility(View.GONE);
                layout.setVisibility(View.GONE);
            }
        });
        listFriendView.setAdapter(friendAdapter);

        //Khi bấm tất cả bạn bè thì hiển thị lại toàn bộ ảnh
        LinearLayout layoutAllFriend = findViewById(R.id.image_all_friend);
        layoutAllFriend.setOnClickListener(v -> {
            imageAdapter.updateList(allPhotos);
            titleFriend.setText("Tất cả bạn bè");
            friendId = "ALL";
            friendName = "Tất cả bạn bè";
            maskView.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        });

        //Xử lý hiển thị lưới ảnh
        photoGrid = findViewById(R.id.photo_grid);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        photoGrid.setLayoutManager(layoutManager);

        imageAdapter = new ImageAdapter(this, allPhotos, new OnImageClickListener() {
            @Override
            public void onImageClick(Image image) {
                Intent intent = new Intent(AllImageActivity.this, PageComponentActivity.class);
                intent.putExtra("userId", currentUserId);
                intent.putExtra("imageId", image.getImageId()); // ảnh cần hiển thị
                intent.putExtra("friendId", friendId);
                intent.putExtra("friendName", friendName);
                intent.putExtra("pageIndex", 1); // chuyển sang trang react
                startActivity(intent);
            }
        });

        photoGrid.setAdapter(imageAdapter);

        //Khi nhấn vào nút chụp ở bottomBar thì chuyển sang home page
        View take = findViewById(R.id.take);
        take.setOnClickListener(v -> {
            Intent intent = new Intent(AllImageActivity.this, PageComponentActivity.class);
            startActivity(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        });

        loadListFriendID();
        loadListUser();
    }

    //list user là friends của current user
    private void loadListUser(){
        Call<List<User>> call = frService.getListFriendByUserId(currentUserId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listFriend.clear();
                    listFriend.addAll(response.body());

                    //Thêm mục "Tôi"
                    //Tìm user by id
                    User owner = new User();
                    owner.setUserId(currentUserId);
                    owner.setFullName("Tôi");
                    listFriend.add(owner);
                    friendAdapter.updateList(listFriend);
                    Log.e("AllImage: FRIENDS", listFriend.toString());
                } else {
                    Log.e("AllImage: FRIENDS", "Không lấy được danh sách bạn bè");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e("AllImage API get Friends", "Error: " + t.getMessage());
            }
        });
    }

    //Load danh sách FriendId để dùng cho load tất cả ảnh
    private void loadListFriendID(){
        Call<Set<String>> call = frService.getFriendIdsByUserId(currentUserId);
        call.enqueue(new Callback<Set<String>>() {
            @Override
            public void onResponse(Call<Set<String>> call, Response<Set<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listFriendIds.clear();
                    listFriendIds.addAll(response.body());

                    loadAllPhotos(listFriendIds);
                    Log.e("AllImage: FriendIDs", listFriendIds.toString());
                } else {
                    Log.e("FriendIDs", "Không lấy được danh sách bạn bè");
                }
            }

            @Override
            public void onFailure(Call<Set<String>> call, Throwable t) {
                Log.e("FRIENDIDS", "Lỗi: " + t.getMessage());
            }
        });
    }

    //Load tất cả ảnh
    private void loadAllPhotos(Set<String> listFriendIds) {
        imageRepo.getAllImagesByUserId(currentUserId, listFriendIds, new ImageLoadCallback() {
            @Override
            public void onSuccess(List<Image> images) {
                allPhotos.clear();
                allPhotos.addAll(images);
                imageAdapter.updateList(images);
                Log.e("AllImage: Images", allPhotos.toString());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AllImage: IMAGES", "Lỗi: " + e.getMessage());
            }
        });
    }

    //Lọc ảnh theo người gửi
    public void filterImagesBySenderId(String senderId){
        List<Image> listPhotoFilter = new ArrayList<>();
        for (Image image: allPhotos){
            if (image.getSenderId().equals(senderId))
                listPhotoFilter.add(image);
        }
        imageAdapter.updateList(listPhotoFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageRepo.removeListener(); // khi acitvity bị hủy thì gỡ listener
    }
}
