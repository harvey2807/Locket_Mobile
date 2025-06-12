package com.hucmuaf.locket_mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.hucmuaf.locket_mobile.model.Image;
import com.hucmuaf.locket_mobile.model.ItemFriendAdapter;
import com.hucmuaf.locket_mobile.modeldb.User;
import com.hucmuaf.locket_mobile.service.APIClient;
import com.hucmuaf.locket_mobile.service.FriendRequestService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReactActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViewPager2 imageView;
    private ItemFriendAdapter itemAdapter;
    GestureDetector gestureDetector;
    private List<User> listUser = new ArrayList<>();
    private String currentUserId = "camt91990"; // Hoặc lấy từ session/login
    private FriendRequestService frService = APIClient.getClient().create(FriendRequestService.class);

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.react_emoji_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View main = findViewById(R.id.main);
        gestureDetector  = new GestureDetector(this, new SwipeGestureListenerDown(this));
        main.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        recyclerView = findViewById(R.id.list_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

//        List<ItemFriend> list = Arrays.asList(
//          new ItemFriend("account_icon", "Tất cả bạn bè"),
//          new ItemFriend("account_icon", "Khanh Duy"),
//          new ItemFriend("account_icon", "Cẩm Tú"),
//          new ItemFriend("account_icon", "Tấn Lực"),
//          new ItemFriend("account_icon", "Thanh Diệu"),
//          new ItemFriend("account_icon", "Ngọc Diễm"),
//          new ItemFriend("account_icon", "Ngọc Diễm"),
//          new ItemFriend("account_icon", "Ngọc Diễm")
//        );

//        itemAdapter = new ItemFriendAdapter(this, list);
        itemAdapter = new ItemFriendAdapter(this, listUser, new ItemFriendAdapter.OnFriendClickListener() {
            @Override
            public void onFriendClick(User user) {
//                if (user.getUserId().equals("ALL")) {
//                    // Hiện tất cả ảnh
//                    imageAdapter.updateList(allPhotos);
//                } else {
//                    // Lọc theo senderId
//                    filterImagesBySenderId(user.getUserId());
//                }

                // Ẩn danh sách bạn bè sau khi chọn
                findViewById(R.id.friends_board).setVisibility(View.GONE);
                findViewById(R.id.mask).setVisibility(View.GONE);
            }
        });

        recyclerView.setAdapter(itemAdapter);

        View maskView = findViewById(R.id.mask);
        LinearLayout layout = findViewById(R.id.friends_board);
        LinearLayout down_toggle = findViewById(R.id.all_friends);

        down_toggle.setOnClickListener(v ->{
            maskView.setVisibility(View.VISIBLE);
            layout.setVisibility(View.VISIBLE);
        });

        maskView.setOnClickListener(e ->{
            maskView.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        });

        imageView = findViewById(R.id.list_image_react);
        List<Image> pages = Arrays.asList(
//                new Image("1", "", "Check-in metro", 1, "5", Arrays.asList("1","3")),
//                new Image("2", "", "Dạo một vòng quanh metro", 1, "5", Arrays.asList("1","3")),
//                new Image("3", "", "Metro buổi tối", 1, "5", Arrays.asList("1","3"))
        );
        PhotoAdapter adapter = new PhotoAdapter(this, pages);

        imageView.setAdapter(adapter);

        View take = findViewById(R.id.take);
        take.setOnClickListener(v ->{
            startActivityWithAnimation(this, TakeActivity.class, R.anim.slide_down);
        });

        //Hiển thị trang lưới ảnh khi bấm vào nút flash
        ImageView flash = findViewById(R.id.flash);
        flash.setOnClickListener(v -> {
            Intent intent = new Intent(ReactActivity.this, AllImageActivity.class);
            startActivity(intent);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        });
    }

    private void startActivityWithAnimation(Context context, Class<?> cls, int animEnter) {
        if (context == null) return;

        Intent intent = new Intent(context, cls);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(animEnter, R.anim.no_animation);
        }
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
                    itemAdapter.updateList(listUser);
                } else {
                    Log.e("FRIENDS", "Không lấy được danh sách bạn bè");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {

            }
        });
    }
}
