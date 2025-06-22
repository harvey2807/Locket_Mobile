package com.hucmuaf.locket_mobile.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.modedb.User;

import com.hucmuaf.locket_mobile.holder.ItemFriendViewHolder;
import com.hucmuaf.locket_mobile.model.ItemFriend;

import java.util.List;

public class ItemFriendAdapter extends RecyclerView.Adapter<ItemFriendViewHolder> {
    private Context context;
    private List<User> itemList;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener {
        void onFriendClick(User user);
    }

    public ItemFriendAdapter(Context context, List<User> itemList
            , OnFriendClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    public void updateList(List<User> newList) {
        itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_dropdown, parent, false);
        return new ItemFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemFriendViewHolder holder, int position) {
        User u = itemList.get(position);
        holder.bind(u, listener);
        String imageName = u.getUrlAvatar();

        /**try catch lại khi URL bị null*/
        // Load ảnh từ URL bằng Glide
        Glide.with(context)
                .load(imageName)
                .placeholder(R.drawable.default_img) // ảnh tạm khi đang load
                .error(R.drawable.default_img) // ảnh khi load lỗi
                .into(holder.getIvIcon());
        holder.getTvName().setText(u.getFullName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
