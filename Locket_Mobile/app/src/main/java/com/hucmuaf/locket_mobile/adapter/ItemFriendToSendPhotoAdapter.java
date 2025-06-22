package com.hucmuaf.locket_mobile.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.holder.ItemFriendToSendPhotoHolder;
import com.hucmuaf.locket_mobile.inteface.OnFriendToSendListenter;
import com.hucmuaf.locket_mobile.modedb.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemFriendToSendPhotoAdapter extends RecyclerView.Adapter<ItemFriendToSendPhotoHolder> {
    private Context context;
    private List<User> itemList;
    private OnFriendToSendListenter listenter;
    private boolean allSelected;

    public ItemFriendToSendPhotoAdapter(Context context, List<User> itemList, OnFriendToSendListenter listener) {
        this.context = context;
        this.itemList = itemList;
        this.listenter = listener;
        this.allSelected = false;
    }

    public void setSelected(boolean b) {
        allSelected = b;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemFriendToSendPhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account_send_photo, parent, false);
        return new ItemFriendToSendPhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemFriendToSendPhotoHolder holder, int position) {
        User u = itemList.get(position);
        if (allSelected) {
            String imageName = u.getUrlAvatar();
            holder.bind(u, listenter);
            // Load ảnh từ URL bằng Glide
            Glide.with(context)
                    .load(imageName)
                    .placeholder(R.drawable.avt) // ảnh tạm khi đang load
                    .error(R.drawable.avt) // ảnh khi load lỗi
                    .into(holder.getIvIcon());
            holder.getTvName().setText(u.getFullName());
            Log.e("ImageError", u.getFullName());
            holder.setSelected(false);
            holder.getItemFriend().setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.circle_border_gray));
        } else {
            String imageName = u.getUrlAvatar();
            holder.bind(u, listenter);
            // Load ảnh từ URL bằng Glide
            Glide.with(context)
                    .load(imageName)
                    .placeholder(R.drawable.avt) // ảnh tạm khi đang load
                    .error(R.drawable.avt) // ảnh khi load lỗi
                    .into(holder.getIvIcon());
            holder.getTvName().setText(u.getFullName());
            Log.e("ImageError", u.getFullName());
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
