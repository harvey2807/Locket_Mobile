package com.hucmuaf.locket_mobile.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.adapter.ItemFriendToSendPhotoAdapter;
import com.hucmuaf.locket_mobile.inteface.OnFriendToSendListenter;
import com.hucmuaf.locket_mobile.modedb.Image;
import com.hucmuaf.locket_mobile.modedb.SaveResponse;
import com.hucmuaf.locket_mobile.modedb.UploadImageResponse;
import com.hucmuaf.locket_mobile.modedb.User;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.FriendRequestService;
import com.hucmuaf.locket_mobile.service.ImageService;
import com.hucmuaf.locket_mobile.service.OnFriendLoadedListener;
import com.hucmuaf.locket_mobile.service.UploadImageService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowImageActivity extends AppCompatActivity {
    private EditText caption;
    private View mask;
    private View decor_caption;
    private View cached;
    private RecyclerView listFriendRV;

    private List<User> listFriend;
    private List<String> listFriendToSend;
    private List<String> listAllFriend;
    private ItemFriendToSendPhotoAdapter itemFriendToSendPhotoAdapter;
    private LinearLayout allFriends;
    private boolean sendAll;
    private String userId;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.take_send_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sendAll = false;
        userId = getIntent().getStringExtra("userId");

        ImageView imageView = findViewById(R.id.camera_preview);
        String imagePath = getIntent().getStringExtra("imagePath");
        Bitmap rotatedBitmap;

        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            rotatedBitmap = rotateImageIfRequired(bitmap, imagePath);
            imageView.setImageBitmap(rotatedBitmap);
        } else {
            rotatedBitmap = null;
        }

        ImageView closeBtn = findViewById(R.id.close);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShowImageActivity.this, PageComponentActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            }
        });

        mask = findViewById(R.id.mask);
        decor_caption = findViewById(R.id.decor_caption);
        cached = findViewById(R.id.cached);

        cached.setOnClickListener(v -> {
            mask.setVisibility(View.VISIBLE);
            decor_caption.setVisibility(View.VISIBLE);
        });

        // CHẶN SỰ KIỆN TỪ decor_caption TRUYỀN XUỐNG mask
        decor_caption.setOnTouchListener((v, event) -> true);

        mask.setOnClickListener(v -> {
            mask.setVisibility(View.GONE);
            decor_caption.setVisibility(View.GONE);
        });

        caption = findViewById(R.id.caption);
        setText();

        ImageView download = findViewById(R.id.download);
        download.setOnClickListener(v -> {
            saveBitmapToGallery(this, rotatedBitmap, "captured_" + System.currentTimeMillis() + ".jpg");
        });

        listFriendToSend = new ArrayList<>();
        listAllFriend = new ArrayList<>();
        listFriendRV = findViewById(R.id.friends_to_send_photo);
        listFriendRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        listFriend = new ArrayList<>();
        allFriends = findViewById(R.id.all_friends);
        LinearLayout borderAllFriend = findViewById(R.id.border_all_friends);

        getFriends(userId, new OnFriendLoadedListener() {
            @Override
            public void onSuccess(List<User> users) {
                listFriend = users;
                Log.e("ShowImageActivity", listFriend.toString());

                itemFriendToSendPhotoAdapter = new ItemFriendToSendPhotoAdapter(ShowImageActivity.this, listFriend, new OnFriendToSendListenter() {
                    @Override
                    public void sendTo(User user) {
                        itemFriendToSendPhotoAdapter.setSelected(false);
                        if (!listFriendToSend.contains(user.getUserId())) {
                            listFriendToSend.add(user.getUserId());
                        } else {
                            listFriendToSend.remove(user.getUserId());
                        }
                        if (sendAll) {
                            sendAll = false;
                            borderAllFriend.setBackground(ContextCompat.getDrawable(ShowImageActivity.this, R.drawable.circle_border_gray));
                        }
                    }
                });
                listFriendRV.setAdapter(itemFriendToSendPhotoAdapter);
                Log.e("Show Image Activity", listFriendToSend.toString());

                allFriends.setOnClickListener(v -> {
                    sendAll = true;
                    listAllFriend.clear();
                    listFriendToSend.clear();
                    for (User u : listFriend) {
                        listAllFriend.add(u.getUserId());
                        borderAllFriend.setBackground(ContextCompat.getDrawable(ShowImageActivity.this, R.drawable.circle_border_blue));
                    }
                    itemFriendToSendPhotoAdapter.setSelected(true);
                    listFriendToSend.addAll(listAllFriend);
                });
            }

            @Override
            public void onFailure(String error) {
            }
        });

        LinearLayout sendLayout = findViewById(R.id.send);
        sendLayout.setOnClickListener(v -> {
            assert imagePath != null;
            File file = new File(imagePath);
            Log.e("Image Path", imagePath);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
            uploadImage(body);
        });

        LinearLayout captionView = findViewById(R.id.caption_view);
        FrameLayout rootView = findViewById(R.id.preview_image_view);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r); // lấy phần visible (không bị bàn phím che)
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                // Bàn phím đã mở
                int[] location = new int[2];
                captionView.getLocationOnScreen(location);
                int editTextY = location[1] + captionView.getHeight();

                // Nếu EditText đang bị bàn phím che thì dịch layout lên
                if (editTextY > r.bottom) {
                    int offset = editTextY - r.bottom + 20; // +20 để có khoảng cách nhỏ
                    captionView.setTranslationY(-offset);
                }
            } else {
                // Bàn phím đã đóng, đưa layout về vị trí ban đầu
                captionView.setTranslationY(0);
            }
        });

        TextView clock = findViewById(R.id.clock);
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY); // 0 - 23
        int minute = calendar.get(Calendar.MINUTE);    // 0 - 59

        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        clock.setText("⏰ " + time);
    }

    public void uploadImage(MultipartBody.Part part) {
        final String[] path = {""};
        UploadImageService uploadImageService = ApiClient.getUploadImageService();
        Call<UploadImageResponse> imagePathCall = uploadImageService.uploadImage(part);
        imagePathCall.enqueue(new Callback<UploadImageResponse>() {
            @Override
            public void onResponse(@NonNull Call<UploadImageResponse> call, @NonNull Response<UploadImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getUrl(); // 🔥 Đây là link ảnh từ Cloudinary
                    Log.d("UPLOAD", "Ảnh đã upload: " + imageUrl);
                    // Bạn có thể hiển thị ảnh:

                    Timestamp timestamp = Timestamp.now();
                    long currentTime = timestamp.toDate().getTime();
                    String timeString = currentTime + "";
                    Random random = new Random(2000);
                    int a = random.nextInt();
                    String imageId = userId + a + timeString.substring(0, 3);
                    @SuppressLint("CutPasteId") EditText captionEt = findViewById(R.id.caption);
                    String caption = captionEt.getText().toString();

                    Image newImage = new Image(imageId, imageUrl, caption, currentTime, userId, listFriendToSend);

                    saveImage(newImage);
                } else {
                    Log.e("UPLOAD", "Lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UploadImageResponse> call, @NonNull Throwable t) {
                Log.e("UPLOAD", "Upload thất bại", t);
            }
        });
    }

    public void saveImage(Image image) {
        ImageService imageService = ApiClient.getImageService();
        Call<SaveResponse> call = imageService.saveImage(image);

        call.enqueue(new Callback<SaveResponse>() {
            @Override
            public void onResponse(@NonNull Call<SaveResponse> call, @NonNull Response<SaveResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().getMessage();
                    Intent intent = new Intent(ShowImageActivity.this, PageComponentActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                    finish();
                    Toast.makeText(ShowImageActivity.this, message, Toast.LENGTH_SHORT).show();
                    Log.d("UPLOAD", "Kết quả: " + message);
                } else {
                    Log.e("UPLOAD", "Lỗi backend: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SaveResponse> call, @NonNull Throwable t) {
                Log.e("UPLOAD", "Lỗi kết nối", t);
            }
        });
    }

    public void getFriends(String userId, OnFriendLoadedListener listener) {
        FriendRequestService friendRequestService = ApiClient.getFriendRequestService();
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

    private Bitmap rotateImageIfRequired(Bitmap bitmap, String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateBitmap(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateBitmap(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateBitmap(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void setText() {
        TextView defaultText = findViewById(R.id.defaultText);
        TextView clock = findViewById(R.id.clock);
        TextView partyTime = findViewById(R.id.partyTime);
        TextView ootd = findViewById(R.id.ootd);
        TextView missYou = findViewById(R.id.missYou);

        defaultText.setOnClickListener(v -> {
            caption.setText("");
            caption.setHint("Thêm một tin nhắn");
            caption.setTextColor(defaultText.getTextColors());
            caption.setBackground(defaultText.getBackground().getConstantState().newDrawable().mutate());
            caption.setCompoundDrawablesRelative(null, null, null, null);
            mask.setVisibility(View.GONE);
            decor_caption.setVisibility(View.GONE);
        });
        clock.setOnClickListener(v -> {
            caption.setText(clock.getText());
            caption.setHint("");
            caption.setTextColor(clock.getTextColors());
            caption.setBackground(clock.getBackground().getConstantState().newDrawable().mutate());
            mask.setVisibility(View.GONE);
            decor_caption.setVisibility(View.GONE);
        });
        partyTime.setOnClickListener(v -> {
            caption.setText(partyTime.getText());
            caption.setHint("");
            caption.setTextColor(partyTime.getTextColors());
            caption.setBackground(partyTime.getBackground().getConstantState().newDrawable().mutate());
            caption.setCompoundDrawablesRelative(null, null, null, null);
            mask.setVisibility(View.GONE);
            decor_caption.setVisibility(View.GONE);
        });
        ootd.setOnClickListener(v -> {
            caption.setText(ootd.getText());
            caption.setHint("");
            caption.setTextColor(ootd.getTextColors());
            caption.setBackground(ootd.getBackground().getConstantState().newDrawable().mutate());
            caption.setCompoundDrawablesRelative(null, null, null, null);
            mask.setVisibility(View.GONE);
            decor_caption.setVisibility(View.GONE);
        });
        missYou.setOnClickListener(v -> {
            caption.setText(missYou.getText());
            caption.setHint("");
            caption.setTextColor(missYou.getTextColors());
            caption.setBackground(missYou.getBackground().getConstantState().newDrawable().mutate());
            caption.setCompoundDrawablesRelative(null, null, null, null);
            mask.setVisibility(View.GONE);
            decor_caption.setVisibility(View.GONE);
        });
    }

    public void saveBitmapToGallery(Context context, Bitmap bitmap, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyApp");

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream out = resolver.openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                Toast.makeText(context, "Đã lưu ảnh vào Thư viện", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Lỗi khi lưu ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
