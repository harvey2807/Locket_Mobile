package com.hucmuaf.locket_mobile.AllImage;

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
import com.hucmuaf.locket_mobile.Firebase.ImageResponsitory;
import com.hucmuaf.locket_mobile.ModelDB.Image;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.ReactActivity;
import com.hucmuaf.locket_mobile.service.APIClient;
import com.hucmuaf.locket_mobile.service.FriendRequestService;
import com.hucmuaf.locket_mobile.model.ItemFriend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllImageActivity extends AppCompatActivity {
    private RecyclerView photoGrid;

    private RecyclerView listFriendView;
    private ImageAdapter adapter;
    private List<Image> allPhotos = new ArrayList<>();
    private String currentUserId = "u1"; // Hoặc lấy từ session/login

    private Set<String> listFriendIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_images);

        photoGrid = findViewById(R.id.photo_grid);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        photoGrid.setLayoutManager(layoutManager);

        listFriendView = findViewById(R.id.list_friends);
        listFriendView.setLayoutManager(new LinearLayoutManager(this));

        List<ItemFriend> list = Arrays.asList(
                new ItemFriend("account_icon", "Tất cả bạn bè"),
                new ItemFriend("account_icon", "Khanh Duy"),
                new ItemFriend("account_icon", "Cẩm Tú"),
                new ItemFriend("account_icon", "Tấn Lực"),
                new ItemFriend("account_icon", "Thanh Diệu"),
                new ItemFriend("account_icon", "Ngọc Diễm"),
                new ItemFriend("account_icon", "Ngọc Diễm"),
                new ItemFriend("account_icon", "Ngọc Diễm")
        );

//        itemAdapter = new ItemFriendAdapter(this, list);
//
//        recyclerView.setAdapter(itemAdapter);

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

        adapter = new ImageAdapter(this, allPhotos, photo -> {
            Intent intent = new Intent(AllImageActivity.this, ReactActivity.class);
            intent.putExtra("photo", new Gson().toJson(photo)); // Truyền sang chi tiết
            startActivity(intent);
        });

        photoGrid.setAdapter(adapter);

        loadListFriend();
    }

    private void loadListFriend(){
        FriendRequestService frService = APIClient.getClient().create(FriendRequestService.class);
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

    private void loadAllPhotos(Set<String> listFriendIds) {
        ImageResponsitory repo = new ImageResponsitory();
        repo.getAllImagesByUserId(currentUserId, listFriendIds, new ImageResponsitory.onImageLoaded() {
            @Override
            public void onSuccess(List<Image> images) {
                adapter.updateList(images);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("IMAGES", "Lỗi: " + e.getMessage());
            }
        });
    }

//
//    private void setupFriendFilter() {
//        LinearLayout friends = findViewById(R.id.friends);
//        friends.setOnClickListener(v -> {
//            // Gọi dialog chọn bạn -> getFriendId
//            String selectedFriendId = "u2"; // ví dụ
//            filterByFriend(selectedFriendId);
//        });
//    }
//
//    private void filterByFriend(String friendId) {
//        // Gọi /api/photos/user/{currentUserId}/friend/{friendId}
//        ApiClient.getPhotosByFriend(currentUserId, friendId, new Callback<List<Photo>>() {
//            @Override
//            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
//                if (response.isSuccessful()) {
//                    adapter.updateList(response.body());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<Photo>> call, Throwable t) {
//                Toast.makeText(AllImageActivity.this, "Lỗi lọc ảnh", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
}
