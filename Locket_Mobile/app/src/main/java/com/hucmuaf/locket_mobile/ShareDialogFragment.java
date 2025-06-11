package com.hucmuaf.locket_mobile;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ShareDialogFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_share_dialog, container, false);

        // Nút Hủy
        TextView cancelButton = view.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(v -> dismiss());

        // Nút Sao chép
        LinearLayout copyButton = view.findViewById(R.id.copy_button);
        copyButton.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Modis Link", "https://...");
            clipboard.setPrimaryClip(clip);
            dismiss();
        });

        // Xử lý chia sẻ qua các ứng dụng
        ImageView shareIcon = view.findViewById(R.id.share_icon);
        ImageView instagramIcon = view.findViewById(R.id.instagram_icon);
        ImageView facebookIcon = view.findViewById(R.id.facebook_icon);

        shareIcon.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://...");
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ qua"));
            dismiss();
        });

        instagramIcon.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://...");
            shareIntent.setPackage("com.instagram.android");
            try {
                startActivity(shareIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dismiss();
        });

        facebookIcon.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Tôi muốn thêm bạn vào Màn hình chính của tôi qua Modis. Chạm vào liên kết để chấp nhận 💛 https://...");
            shareIntent.setPackage("com.facebook.katana");
            try {
                startActivity(shareIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dismiss();
        });

        return view;
    }
}