package com.hucmuaf.locket_mobile.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hucmuaf.locket_mobile.modedb.Image;
import com.hucmuaf.locket_mobile.R;
import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.holder.ImageViewHolder;
import com.hucmuaf.locket_mobile.inteface.OnImageClickListener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private List<Image> imageList;
    private Context context;
    private OnImageClickListener listener;
    
    public ImageAdapter(Context context, List<Image> photoList, OnImageClickListener listener) {
        this.context = context;
        this.imageList = photoList;
        this.listener = listener;
    }

    //Cập nhật danh sách hình ảnh mới
    public void updateList(List<Image> newList) {
        imageList = newList;
        notifyDataSetChanged(); //Cập nhật giao diện
    }

    //
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    //Sử dụng Glide để tải hình ảnh từ URL và hiển thị trong ImageView.
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        //Tính toán kích thước của ô ảnh
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // 3 cột, mỗi cột cách nhau 4dp (spacing)
        int spacing = (int) context.getResources().getDimension(R.dimen.grid_spacing);
        int totalSpacing = spacing * 2; // 3 ảnh = 2 khoảng trống
        int itemSize = (screenWidth - totalSpacing) / 3;

        //set kích thước
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(itemSize, itemSize);
        } else {
            params.width = itemSize;
            params.height = itemSize;
        }
        holder.itemView.setLayoutParams(params);

        Image photo = imageList.get(position);
        String url = photo.getUrlImage();
        if (url != null && !url.isEmpty()) {
            Glide.with(context)
                    .load(url)
                    .override(itemSize, itemSize)
                    .placeholder(R.drawable.default_img) // ảnh tạm khi đang load
                    .error(R.drawable.default_img) // ảnh khi load lỗi
                    .into(holder.getImageView());
        }{
            // Nếu ảnh null thì đặt ảnh mặc định
            holder.getImageView().setImageResource(R.drawable.default_img);
        }

        holder.getImageView().setOnClickListener(v -> {
            if (listener != null) listener.onImageClick(photo);
        });
    }

    //Đếm số lượng phần tử trong danh sách ảnh
    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }


}
