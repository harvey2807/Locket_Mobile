package com.hucmuaf.locket_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hucmuaf.locket_mobile.allImage.ImageAdapter;
import com.hucmuaf.locket_mobile.firebase.ImageResponsitory;
import com.hucmuaf.locket_mobile.modeldb.Image;
import com.hucmuaf.locket_mobile.modeldb.User;
import com.hucmuaf.locket_mobile.service.APIClient;
import com.hucmuaf.locket_mobile.service.FriendRequestService;
import com.hucmuaf.locket_mobile.model.ItemFriendAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllImageActivity extends AppCompatActivity {
    private ImageResponsitory imageRepo = new ImageResponsitory();
    private RecyclerView photoGrid;
    private RecyclerView listFriendView;
    private ImageAdapter imageAdapter;
    private ItemFriendAdapter friendAdapter;
    private List<Image> allPhotos = new ArrayList<>();
    private String currentUserId = "camt91990"; // Hoặc lấy từ session/login
    private FriendRequestService frService = APIClient.getClient().create(FriendRequestService.class);
    private Set<String> listFriendIds = new HashSet<>();
    private List<User> listUser = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_images);

        photoGrid = findViewById(R.id.photo_grid);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        photoGrid.setLayoutManager(layoutManager);

        listFriendView = findViewById(R.id.list_friends);
        listFriendView.setLayoutManager(new LinearLayoutManager(this));

        friendAdapter = new ItemFriendAdapter(this, listUser, new ItemFriendAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User user) {
                if (user.getUserId().equals("ALL")) {
                    // Hiện tất cả ảnh
                    imageAdapter.updateList(allPhotos);
                } else {
                    // Lọc theo senderId
                    filterImagesBySenderId(user.getUserId());
                }

                // Ẩn danh sách bạn bè sau khi chọn
                findViewById(R.id.friends_board).setVisibility(View.GONE);
                findViewById(R.id.mask).setVisibility(View.GONE);
            }
        });

        listFriendView.setAdapter(friendAdapter);

        View maskView = findViewById(R.id.mask);
        LinearLayout layout = findViewById(R.id.friends_board);

        ImageView down_toggle = findViewById(R.id.down_toggle);

        down_toggle.setOnClickListener(v ->{
            maskView.setVisibility(View.VISIBLE);
            layout.setVisibility(View.VISIBLE);
        });

        maskView.setOnClickListener(e ->{
            maskView.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        });

        imageAdapter = new ImageAdapter(this, allPhotos, photo -> {
            Intent intent = new Intent(AllImageActivity.this, ReactActivity.class);
            intent.putExtra("photo", new Gson().toJson(photo)); // Truyền sang chi tiết
            startActivity(intent);
        });

        photoGrid.setAdapter(imageAdapter);

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
                    listUser.clear();
                    // Thêm mục "Tất cả bạn bè"
                    User allUser = new User();
                    allUser.setUserId("ALL");
                    allUser.setFullName("Tất cả bạn bè");
                    allUser.setUrlAvatar("@mipmap/groups");
                    listUser.add(allUser);

                    listUser.addAll(response.body());
                    friendAdapter.updateList(listUser);
                } else {
                    Log.e("FRIENDS", "Không lấy được danh sách bạn bè");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

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
                } else {
                    Log.e("FRIENDS", "Không lấy được danh sách bạn bè");
                }
            }

            @Override
            public void onFailure(Call<Set<String>> call, Throwable t) {
                Log.e("FRIENDS", "Lỗi: " + t.getMessage());
            }
        });
    }

    //Load tất cả ảnh
    private void loadAllPhotos(Set<String> listFriendIds) {
        imageRepo.getAllImagesByUserId(currentUserId, listFriendIds, new ImageResponsitory.onImageLoaded() {
            @Override
            public void onSuccess(List<Image> images) {
                allPhotos.clear();
                allPhotos.addAll(images);
                imageAdapter.updateList(images);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("IMAGES", "Lỗi: " + e.getMessage());
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
