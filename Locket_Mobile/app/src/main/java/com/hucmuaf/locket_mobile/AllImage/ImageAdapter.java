package com.hucmuaf.locket_mobile.AllImage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hucmuaf.locket_mobile.ModelDB.Image;
import com.hucmuaf.locket_mobile.R;
import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<PhotoViewHolder> {

    private List<Image> imageList;
    private Context context;
    private OnImageClickListener listener;

    //Xử lý sự kiện khu người dùng nhấn vào một ảnh
    public interface OnImageClickListener {
        void onImageClick(Image image);
    }

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
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new PhotoViewHolder(view);
    }

    //Sử dụng Glide để tải hình ảnh từ URL và hiển thị trong ImageView.
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Image photo = imageList.get(position);
        Glide.with(context)
                .load(photo.getUrlImage())
                .placeholder(R.drawable.image1) //ảnh mặc định hiển thị trước khi ảnh chính được tải xong
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
        if (listener != null) listener.onImageClick(photo);
    });
    }

    //Đếm số lượng phần tử trong danh sách ảnh
    @Override
    public int getItemCount() {
        return imageList != null ? imageList.size() : 0;
    }


}
