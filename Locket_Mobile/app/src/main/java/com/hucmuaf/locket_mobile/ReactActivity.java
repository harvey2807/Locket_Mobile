package com.hucmuaf.locket_mobile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.hucmuaf.locket_mobile.ModelDB.Image;
import com.hucmuaf.locket_mobile.model.ItemFriendAdapter;
import com.hucmuaf.locket_mobile.model.PhotoAdapter;
import com.hucmuaf.locket_mobile.ModelDB.User;
import com.hucmuaf.locket_mobile.service.APIClient;
import com.hucmuaf.locket_mobile.service.FriendRequestService;
import com.hucmuaf.locket_mobile.service.ImageService;
import com.hucmuaf.locket_mobile.service.OnFriendLoadedListener;
import com.hucmuaf.locket_mobile.service.OnImagesLoadedListener;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReactActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViewPager2 imageView;
    private ItemFriendAdapter itemAdapter;
    GestureDetector gestureDetector;
    private List<Image> pages;
    private List<User> listFriend;
    private final String userID = "camt91990";
    private APIClient apiClient;

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
        gestureDetector = new GestureDetector(this, new SwipeGestureListenerDown(this));
        main.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

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

        LinearLayout imageAllFriend = findViewById(R.id.image_all_friend);
        TextView tvName = findViewById(R.id.tvName);
        // CHẶN SỰ KIỆN TỪ decor_caption TRUYỀN XUỐNG mask
        imageAllFriend.setOnClickListener(v -> {
            Log.e("Click", "Click rồi này");
            getAllImagePages(userID, new OnImagesLoadedListener() {
                @Override
                public void onSuccess(List<Image> images) {
                    pages.clear();
                    // Xử lý danh sách ảnh ở đây
                    pages = images;
                    titleFriend.setText(tvName.getText());
                    Log.e("React Activity", pages.toString());
                    PhotoAdapter adapter = new PhotoAdapter(ReactActivity.this, pages);

                    imageView.setAdapter(adapter);
                    Log.e("API", images.toString());
                    maskView.setVisibility(View.GONE);
                    layout.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(String error) {
                    Log.e("API", "Error: " + error);
                }
            });
        });

        recyclerView = findViewById(R.id.list_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listFriend = new ArrayList<>();
        getFriends(userID, new OnFriendLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                listFriend = users;
                Log.e("React Activity", listFriend.toString());

                itemAdapter = new ItemFriendAdapter(ReactActivity.this, listFriend, new ItemFriendAdapter.OnFriendClickListener() {
                    @Override
                    public void onFriendClick(User user) {
                        getImageOfUser(user.getUserId(), new OnImagesLoadedListener() {
                            @Override
                            public void onSuccess(List<Image> images) {
                                // Xử lý danh sách ảnh ở đây
                                pages = images;
                                titleFriend.setText(user.getUserName());
                                Log.e("React Activity", pages.toString());
                                PhotoAdapter adapter = new PhotoAdapter(ReactActivity.this, pages);
                                imageView.setAdapter(adapter);
                                Log.e("API", images.toString());
                                maskView.setVisibility(View.GONE);
                                layout.setVisibility(View.GONE);
                            }

                            @Override
                            public void onFailure(String error) {
                                Log.e("API", "Error: " + error);
                            }
                        });
                    }
                });
                recyclerView.setAdapter(itemAdapter);

            }

            @Override
            public void onFailure(String error) {
            }
        });

        imageView = findViewById(R.id.list_image_react);
        pages = new ArrayList<>();
        getAllImagePages(userID, new OnImagesLoadedListener() {
            @Override
            public void onSuccess(List<Image> images) {
                pages.clear();
                // Xử lý danh sách ảnh ở đây
                pages = images;

                Log.e("React Activity", pages.toString());
                PhotoAdapter adapter = new PhotoAdapter(ReactActivity.this, pages);

                imageView.setAdapter(adapter);
                Log.e("API", images.toString());
            }

            @Override
            public void onFailure(String error) {
                Log.e("API", "Error: " + error);
            }
        });

        View take = findViewById(R.id.take);
        take.setOnClickListener(v -> {
            startActivityWithAnimation(this, TakeActivity.class, R.anim.slide_down);
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

    public void getAllImagePages(String userId, OnImagesLoadedListener listener) {
        ImageService imageService = APIClient.getClient().create(ImageService.class);
        Call<List<Image>> call = imageService.getAllImages(userId);
        Log.e("PAGES", call.toString());

        call.enqueue(new Callback<List<Image>>() {
            @Override
            public void onResponse(@NonNull Call<List<Image>> call, @NonNull Response<List<Image>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    listener.onFailure("Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Image>> call, @NonNull Throwable t) {
                listener.onFailure(t.getMessage());
            }
        });
    }

    public void getFriends(String userId, OnFriendLoadedListener listener) {
        FriendRequestService friendRequestService = APIClient.getClient().create(FriendRequestService.class);
        Call<List<User>> call = friendRequestService.getListFriendByUserId(userId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    listener.onFailure("Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
                listener.onFailure(t.getMessage());
            }
        });
    }

    public void getImageOfUser(String userId, OnImagesLoadedListener listener) {
        ImageService imageService = APIClient.getClient().create(ImageService.class);
        Call<List<Image>> call = imageService.getImageOfUser(userId);

        call.enqueue(new Callback<List<Image>>() {
            @Override
            public void onResponse(@NonNull Call<List<Image>> call, @NonNull Response<List<Image>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    listener.onFailure("Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Image>> call, @NonNull Throwable t) {
                listener.onFailure(t.getMessage());
            }
        });
    }
}
