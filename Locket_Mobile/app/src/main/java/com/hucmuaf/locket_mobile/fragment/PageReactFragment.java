package com.hucmuaf.locket_mobile.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.emoji2.widget.EmojiButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.activity.ChatActivity;
import com.hucmuaf.locket_mobile.adapter.FriendReactAdapter;
import com.hucmuaf.locket_mobile.activity.AllImageActivity;
import com.hucmuaf.locket_mobile.adapter.ItemFriendAdapter;
import com.hucmuaf.locket_mobile.adapter.PhotoAdapter;
import com.hucmuaf.locket_mobile.inteface.FriendListApiService;
import com.hucmuaf.locket_mobile.modedb.Image;
import com.hucmuaf.locket_mobile.modedb.Reaction;
import com.hucmuaf.locket_mobile.modedb.SaveResponse;
import com.hucmuaf.locket_mobile.modedb.User;

import com.hucmuaf.locket_mobile.model.Message;
import com.hucmuaf.locket_mobile.model.MessageType;

import com.hucmuaf.locket_mobile.model.FriendListResponse;
import com.hucmuaf.locket_mobile.repo.ImageLoadCallback;
import com.hucmuaf.locket_mobile.repo.ImageResponsitory;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.FirebaseService;
import com.hucmuaf.locket_mobile.service.FriendRequestService;
import com.hucmuaf.locket_mobile.service.ImageService;
import com.hucmuaf.locket_mobile.service.MessageService;
import com.hucmuaf.locket_mobile.service.OnFriendLoadedListener;
import com.hucmuaf.locket_mobile.service.OnImagesLoadedListener;
import com.hucmuaf.locket_mobile.service.ReactionService;
import com.hucmuaf.locket_mobile.service.UserService;
import com.vanniktech.emoji.EmojiPopup;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PageReactFragment extends Fragment {
    private RecyclerView recyclerView;
    private ViewPager2 imageView;
    private ItemFriendAdapter itemAdapter;
    GestureDetector gestureDetector;
    private List<Image> pages;
    private List<User> usersOfPages;
    private WebSocket webSocket;
    private OkHttpClient client;
    private List<User> listFriend;
    private List<User> listFriendReactToYou;
    private String userId = null;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private View temp_comment_view;
    private EditText commentEdit;
    private MaterialButton sendButton;
    private String initialImageId = null;
    private String initialFriendId = null;
    private String initialFriendName = null;
    private Context context;
    private Activity activity;
    private boolean ignoreNextTextChange = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    int currentPage = 0;

    LinearLayout reactLayout;
    LinearLayout reactToYouLayout;
    LinearLayout tempComment;
    private ImageResponsitory imageResponsitory;
    private User currUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.react_emoji_comment, container, false);
    }

    @SuppressLint({"ClickableViewAccessibility", "WrongViewCast"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = requireContext(); // hoặc getContext()
        activity = requireActivity(); // cần thiết nếu gọi hàm yêu cầu Activity
        imageResponsitory = new ImageResponsitory(FirebaseService.getInstance());

        assert getArguments() != null;
        userId = getArguments().getString("userId");
        getUser(userId);
        initialImageId = getArguments().getString("imageId"); //TRuyền thêm imageId để cuộn đến đúng ảnh
        initialFriendId = getArguments().getString("friendId");
        initialFriendName = getArguments().getString("friendName");
        Log.e("React Activity", String.valueOf(userId) + "+ ImageId: " + String.valueOf(initialImageId));

        View maskView = view.findViewById(R.id.mask);
        LinearLayout layout = view.findViewById(R.id.friends_board);
        LinearLayout down_toggle = view.findViewById(R.id.all_friends);
        reactLayout = view.findViewById(R.id.react_emoji_comment);
        reactToYouLayout = view.findViewById(R.id.friend_react_to_you);
        tempComment = view.findViewById(R.id.temp_comment_view);

        down_toggle.setOnClickListener(v -> {
            maskView.setVisibility(View.VISIBLE);
            layout.setVisibility(View.VISIBLE);
        });

        maskView.setOnClickListener(e -> {
            maskView.setVisibility(View.GONE);
            layout.setVisibility(View.GONE);
        });

        TextView titleFriend = view.findViewById(R.id.title);
        if (initialFriendId != null) {
            titleFriend.setText(initialFriendName);
        }


        LinearLayout imageAllFriend = view.findViewById(R.id.image_all_friend);
        TextView tvName = view.findViewById(R.id.tvName);
        // CHẶN SỰ KIỆN TỪ decor_caption TRUYỀN XUỐNG mask
        imageAllFriend.setOnClickListener(v -> {
            Log.e("Click", "Click rồi này");
            getAllImagePagesRealTime(userId, convertListToSet(listFriend), new ImageLoadCallback() {
                @Override
                public void onSuccess(List<Image> images) {
                    pages.clear();
                    // Xử lý danh sách ảnh ở đây
                    pages = images;
                    if (!usersOfPages.contains(currUser)) usersOfPages.add(currUser);
                    titleFriend.setText(tvName.getText());
                    Log.e("React Activity", pages.toString());
                    PhotoAdapter adapter = new PhotoAdapter(context, pages, usersOfPages);

                    imageView.setAdapter(adapter);
                    Log.e("API", images.toString());
                    maskView.setVisibility(View.GONE);
                    layout.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("API", "Error: " + e.getMessage());
                }
            });
        });

        recyclerView = view.findViewById(R.id.list_friends);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        pages = new ArrayList<>();
        usersOfPages = new ArrayList<>();
//            getAllImagePages(userId, new OnImagesLoadedListener() {
//                @Override
//                public void onSuccess(List<Image> images) {
//                    pages.clear();
//                    // Xử lý danh sách ảnh ở đây
//                    pages = images;
//                    if (!usersOfPages.contains(currUser)) usersOfPages.add(currUser);
//                    Log.d("React Activity empty ","Gửi ảnh qua nè"+ pages.toString());
//                    Log.d("React Activity empty ","Gửi user qua nè"+ usersOfPages.toString());
//
//                    PhotoAdapter adapter = new PhotoAdapter(context, pages, usersOfPages);
//
//                    imageView.setAdapter(adapter);
//
//                    // Scroll đến đúng ảnh
//                    if (initialImageId != null) {
//                        for (int i = 0; i < pages.size(); i++) {
//                            if (initialImageId.equals(pages.get(i).getImageId())) {
//                                imageView.setCurrentItem(i, false);
//                                break;
//                            }
//                        }
//                    }
//                    Log.e("API", images.toString());
//                }
//
//                @Override
//                public void onFailure(String error) {
//                    Log.e("API", "Error: " + error);
//
//                }
//            });
        listFriend = new ArrayList<>();
        getFriends(userId, new OnFriendLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                listFriend = users;
                Log.e("React Activity", listFriend.toString());

                itemAdapter = new ItemFriendAdapter(context, listFriend, new ItemFriendAdapter.OnFriendClickListener() {
                    @Override
                    public void onFriendClick(User user) {
                        getImageBySenderIdAndReceiverId(user.getUserId(), userId, new OnImagesLoadedListener() {
                            @Override
                            public void onSuccess(List<Image> images) {
                                // Xử lý danh sách ảnh ở đây
                                pages = images;
                                if (!usersOfPages.contains(currUser)) usersOfPages.add(currUser);
                                titleFriend.setText(user.getFullName());
                                Log.e("React Activity", pages.toString());
                                PhotoAdapter adapter = new PhotoAdapter(context, pages, usersOfPages);
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

                        if (user.getUserId().equals(userId)) titleFriend.setText("Tôi");
                    }
                });
                recyclerView.setAdapter(itemAdapter);

                getAllImagePagesRealTime(userId, convertListToSet(listFriend), new ImageLoadCallback() {
                    @Override
                    public void onSuccess(List<Image> images) {
                        pages.clear();
                        // Xử lý danh sách ảnh ở đây
                        pages = images;
                        if (!usersOfPages.contains(currUser)) usersOfPages.add(currUser);
                        Log.d("React Activity ", "Gửi ảnh qua nè" + pages.toString());
                        Log.d("React Activity ", "Gửi user qua nè" + usersOfPages.toString());
                        PhotoAdapter adapter = new PhotoAdapter(context, pages, usersOfPages);

                        imageView.setAdapter(adapter);

                        // Scroll đến đúng ảnh
                        if (initialImageId != null) {
                            for (int i = 0; i < pages.size(); i++) {
                                if (initialImageId.equals(pages.get(i).getImageId())) {
                                    imageView.setCurrentItem(i, false);
                                    break;
                                }
                            }
                        }
                        Log.e("API", images.toString());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("API", "Error: " + e.getMessage());

                    }
                });
            }

            @Override
            public void onFailure(String error) {
            }
        });


        imageView = view.findViewById(R.id.list_image_react);
        RecyclerView recyclerView1 = (RecyclerView) imageView.getChildAt(0);
        recyclerView1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float startX = 0;
                float startY = 0;
                final int threshold = 30; // Độ nhạy nhận biết vuốt
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        // Không quyết định gì tại đây cả
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getX() - startX;
                        float deltaY = event.getY() - startY;
                        if (Math.abs(deltaY) > Math.abs(deltaX)) {
                            if (currentPage == 0) {
                                Log.d("Touch", deltaY + "+" + deltaX);
                                if (deltaY > threshold) {
                                    // Vuốt xuống
                                    Log.d("Touch", "Vuốt lên");
                                    v.getParent().requestDisallowInterceptTouchEvent(true);
                                } else if (deltaY < -threshold) {
                                    // Vuốt lên
                                    Log.d("Touch", "Vuốt xuống");
                                    v.getParent().requestDisallowInterceptTouchEvent(false);
                                }
                            } else if (currentPage > 0) {
                                Log.e("Page React Fragment", "Vuốt được con rồi này");
                                v.getParent().requestDisallowInterceptTouchEvent(true);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                return false;
            }
        });
        imageView.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                if (!pages.isEmpty()) {
                    String id = pages.get(currentPage).getSenderId();
                    String imageId = pages.get(currentPage).getImageId();

                    Log.e("Page React Fragment", "Id người gửi: " + id);
                    Log.e("Page React Fragment", "Id của ảnh ở trang hiện tại: " + imageId);

                    if (id.equals(userId)) {
                        reactLayout.setVisibility(View.GONE);
                        tempComment.setVisibility(View.GONE);
                        reactToYouLayout.setVisibility(View.VISIBLE);
                        checkYourselfImage(view, imageId);
                    } else {
                        reactLayout.setVisibility(View.VISIBLE);
                        tempComment.setVisibility(View.VISIBLE);
                        reactToYouLayout.setVisibility(View.GONE);
                    }
                }
            }
        });

        View take = view.findViewById(R.id.take);
        take.setOnClickListener(v -> {
            ViewPager2 viewPager = requireActivity().findViewById(R.id.main_viewpager);
            viewPager.setCurrentItem(0, true); // trở lại trang đầu tiên.
        });

        TextView comment = view.findViewById(R.id.comment);
        reactLayout.setOnTouchListener((v, event) -> true);
        EditText commentEditText = view.findViewById(R.id.comment_edit_text);

        FrameLayout rootView = view.findViewById(R.id.main_screen);
        comment.setOnClickListener(v -> {
//            tempComment.setVisibility(View.VISIBLE);
            // Focus vào EditText
            commentEditText.requestFocus();

            // Hiện bàn phím
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
            }
            Log.d("Nhan vao text View", "comment da duoc nhap");

            rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r); // lấy phần visible (không bị bàn phím che)
                int screenHeight = rootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {
                    // Bàn phím đã mở
                    int[] location = new int[2];
                    tempComment.getLocationOnScreen(location);
                    int editTextY = location[1] + tempComment.getHeight();

                    // Nếu EditText đang bị bàn phím che thì dịch layout lên
                    if (editTextY > r.bottom) {
                        int offset = editTextY - r.bottom + 20; // +20 để có khoảng cách nhỏ
                        tempComment.setTranslationY(-offset);
                    }
                } else {
                    // Bàn phím đã đóng, đưa layout về vị trí ban đầu
                    tempComment.setTranslationY(0);
//                    tempComment.setVisibility(View.GONE);
                }
            });
        });

        View root = requireActivity().findViewById(android.R.id.content);
        EditText tempText = view.findViewById(R.id.temp_et);
        ImageView add_emoji = view.findViewById(R.id.add_emoji);

        EmojiPopup emojiPopup = new EmojiPopup(root, tempText);

        add_emoji.setOnClickListener(v -> {
            tempText.requestFocus();
            Log.e("Page React Fragment", "Đã nhận sự kiện click");
            Log.d("EmojiPopup", "EditText focused? " + tempText.isFocused());
            emojiPopup.show();
        });
        tempText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (ignoreNextTextChange) {
                    ignoreNextTextChange = false;
                    return;
                }

                if (count > 0) {
                    CharSequence newText = s.subSequence(start, start + count);
                    String icon = newText.toString();
                    ignoreNextTextChange = true; // để không xử lý setText bên dưới nữa
                    tempText.setText("");

                    Timestamp timestamp = Timestamp.now();
                    long time = timestamp.toDate().getTime();
                    int index = imageView.getCurrentItem();
                    String imageId = pages.get(index).getImageId();

                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(tempText.getWindowToken(), 0);
                    tempText.postDelayed(emojiPopup::dismiss, 1000);

                    reactEmoji(rootView, userId, imageId, icon, time);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        EmojiButton heart = view.findViewById(R.id.heart_emoji);
        EmojiButton happy = view.findViewById(R.id.happy_emoji);
        EmojiButton star = view.findViewById(R.id.star_eye_emoji);
        long time = Timestamp.now().toDate().getTime();
        heart.setOnClickListener(v -> {
            int index = imageView.getCurrentItem();
            String imageId = pages.get(index).getImageId();
            String icon = heart.getText().toString();
            Reaction reaction = new Reaction(userId, imageId, icon, time);
            addReaction(rootView, reaction);
        });
        happy.setOnClickListener(v -> {
            int index = imageView.getCurrentItem();
            String imageId = pages.get(index).getImageId();
            String icon = happy.getText().toString();
            Reaction reaction = new Reaction(userId, imageId, icon, time);
            addReaction(rootView, reaction);
        });
        star.setOnClickListener(v -> {
            int index = imageView.getCurrentItem();
            String imageId = pages.get(index).getImageId();
            String icon = star.getText().toString();
            Reaction reaction = new Reaction(userId, imageId, icon, time);
            addReaction(rootView, reaction);
        });

        //Hiển thị trang lưới ảnh khi bấm vào nút flash
        ImageView flash = view.findViewById(R.id.flash);
        flash.setOnClickListener(v -> {
            Log.e("CHUYển SANG", "All Image");
            Intent intent = new Intent(requireContext(), AllImageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
        commentEdit = view.findViewById(R.id.comment_edit_text);
        sendButton = view.findViewById(R.id.send_comment);
        setupClickListeners();
    }

    @SuppressLint("WrongViewCast")
    private void setupClickListeners() {

        sendButton.setOnClickListener(v -> {
            String messageContent = commentEdit.getText().toString();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
                commentEdit.setText("");
            }
        });
        commentEdit.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (pickImageLauncher != null) {
                pickImageLauncher.launch(intent);
            }
        });

    }


    private void sendMessage(String content) {
        String id = pages.get(currentPage).getSenderId();
        Log.w("PageReactFragment", "initialFriendId: " + id);
        MessageService.getInstance().sendMessage(userId, id, content);
        Log.w("PageReactFragment", "Sent message: " + content + "from" + userId + " to " + id);
    }

    public void reactEmoji(FrameLayout rootView, String userId, String imageId, String icon, long time) {
        Reaction reaction = new Reaction(userId, imageId, icon, time);
        addReaction(rootView, reaction);
    }

    public void getUser(String userId) {
        UserService userService = ApiClient.getUserService();
        Call<User> call = userService.findUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                currUser = response.body();
                assert currUser != null;
                Log.e("Page React Fragment UserCurr", currUser.toString());
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("Page React Fragment", "Không lấy được USER");

            }
        });
    }

    public void addReaction(FrameLayout rootView, Reaction reaction) {
        ReactionService reactionService = ApiClient.getReactionService();
        Call<SaveResponse> call = reactionService.addReaction(reaction);
        call.enqueue(new Callback<SaveResponse>() {
            @Override
            public void onResponse(@NonNull Call<SaveResponse> call, @NonNull Response<SaveResponse> response) {
                addFlyingIconsRandomly(rootView, reaction.getIcon(), 10);
            }

            @Override
            public void onFailure(@NonNull Call<SaveResponse> call, @NonNull Throwable t) {
                Toast.makeText(activity, "Vui Lòng thử lại sao!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkYourselfImage(View view, String imageId) {
        RecyclerView friendReactRecycleView = view.findViewById(R.id.friend_react_recycle);
        friendReactRecycleView.setLayoutManager(new LinearLayoutManager(context));
        listFriendReactToYou = new ArrayList<>();
        TextView activityReation = view.findViewById(R.id.activity_reaction);

        getFriendReactToYou(imageId, new OnFriendLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                listFriendReactToYou = users;
                if (listFriendReactToYou.isEmpty())
                    activityReation.setText("Chưa có hoạt động nào");
                else activityReation.setText("Hoạt động");
                Log.e("Danh sach ban be da tha emoji", listFriendReactToYou.toString());
                FriendReactAdapter adapter = new FriendReactAdapter(context, listFriendReactToYou);
                friendReactRecycleView.setAdapter(adapter);
            }

            @Override
            public void onFailure(String error) {

            }
        });

    }

    public Set<String> convertListToSet(List<User> users) {
        Set<String> set = new HashSet<>();
        for (User u : users) {
            set.add(u.getUserId());
        }
        return set;
    }

    public void getAllImagePages(String userId, OnImagesLoadedListener listener) {
        ImageService imageService = ApiClient.getImageService();
        Call<List<Image>> call = imageService.getAllImages(userId);
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

    public void getAllImagePagesRealTime(String userId, Set<String> listFriendIds, ImageLoadCallback listener) {
        imageResponsitory.getAllImagesByUserId(userId, listFriendIds, listener);
    }

    public void getFriends(String userId, OnFriendLoadedListener listener) {
        FriendListApiService friendRequestService = ApiClient.getFriendListApiService();
        Call<FriendListResponse> call = friendRequestService.getFriendList(userId);
        call.enqueue(new Callback<FriendListResponse>() {
            @Override
            public void onResponse(@NonNull Call<FriendListResponse> call, @NonNull Response<FriendListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
//                    usersOfPages.clear();
                    usersOfPages.addAll(convert(response.body().getFriends()));
                    Log.e("PageReactFragment", usersOfPages.toString());
                    Log.e("PageReactFragment", response.body().toString());
                    listener.onSuccess(usersOfPages);
                } else {
                    listener.onFailure("Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<FriendListResponse> call, @NonNull Throwable t) {
                listener.onFailure(t.getMessage());
            }
        });
    }

    public List<User> convert(List<com.hucmuaf.locket_mobile.model.User> users) {
        List<User> newList = new ArrayList<>();
        for (com.hucmuaf.locket_mobile.model.User u : users) {
            User newUser = new User(u.getUserId(), u.getUserName(), u.getFullName(), u.getEmail(), u.getPhoneNumber(), u.getUrlAvatar(), u.getPassword());
            newList.add(newUser);
        }
        return newList;
    }

    public void getFriendReactToYou(String imageId, OnFriendLoadedListener listener) {
        ReactionService reactionService = ApiClient.getReactionService();
        Call<List<User>> call = reactionService.getFriendReactedToImageYou(imageId);
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                    Log.e("Ban be da tha emoji", response.body().toString());
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

    public void getImageBySenderIdAndReceiverId(String senderId, String
            receiverId, OnImagesLoadedListener listener) {
        ImageService imageService = ApiClient.getImageService();
        Call<List<Image>> call;
        if (senderId.equals(receiverId)) {
            call = imageService.getImageOfUser(senderId);
        } else {
            call = imageService.getImageBySenderIdAndReceiverId(senderId, receiverId);
        }

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

    public void addFlyingIconsRandomly(FrameLayout animationContainer, String emoji, int count) {
        for (int i = 0; i < count; i++) {
            int delay = random.nextInt(800); // từ 0-800ms

            handler.postDelayed(() -> {
                addFlyingIcon(animationContainer, emoji);
            }, delay);
        }
    }

    private void addFlyingIcon(FrameLayout animationContainer, String emoji) {
        final TextView icon = new TextView(context);
        icon.setText(emoji);
        icon.setTextSize(30 + random.nextInt(20)); // random size 30-50

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set vị trí ban đầu ngẫu nhiên trong khoảng 20% đến 80% chiều rộng màn hình
        int screenWidth = animationContainer.getWidth();
        int startX = random.nextInt(screenWidth);
        params.leftMargin = startX;
        params.gravity = Gravity.BOTTOM;

        icon.setLayoutParams(params);

        animationContainer.addView(icon);

        float startY = 0f;
        float endY = -random.nextInt(1800) - 2300f; // cao hơn nếu muốn

        ObjectAnimator moveY = ObjectAnimator.ofFloat(icon, "translationY", startY, endY);
        moveY.setDuration(1500 + random.nextInt(600)); // 1500-2100ms
        moveY.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(icon, "alpha", 1f, 1f);
        fadeOut.setDuration(moveY.getDuration());

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveY, fadeOut);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationContainer.removeView(icon);
            }
        });

        animatorSet.start();
    }
}
