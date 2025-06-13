package com.hucmuaf.locket_mobile;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStream;

public class ShowImageActivity extends AppCompatActivity {
    private EditText caption;
    private View mask;
    private View decor_caption;
    private View cached;


    @SuppressLint("ClickableViewAccessibility")
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
                Intent intent = new Intent(ShowImageActivity.this, TakeActivity.class);
                startActivity(intent);
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

        // Lưu thời gian hiện tại
//        Map<String, Object> data = new HashMap<>();
//        data.put("createdAt", FieldValue.serverTimestamp());


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
            Drawable drawableStart = clock.getCompoundDrawablesRelative()[0];
            // Chú ý: cần gọi mutate() + setBounds nếu muốn hiển thị đúng
            if (drawableStart != null) {
                drawableStart = drawableStart.mutate(); // Tránh ảnh hưởng đến drawable gốc
                drawableStart.setBounds(0, 0, drawableStart.getIntrinsicWidth(), drawableStart.getIntrinsicHeight());
                caption.setCompoundDrawablesRelative(drawableStart, null, null, null);
            }
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
