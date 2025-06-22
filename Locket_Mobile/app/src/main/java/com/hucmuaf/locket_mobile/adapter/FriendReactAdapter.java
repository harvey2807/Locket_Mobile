package com.hucmuaf.locket_mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.holder.ItemFriendReactHolder;
import com.hucmuaf.locket_mobile.modedb.User;

import java.util.List;

public class FriendReactAdapter  extends RecyclerView.Adapter<ItemFriendReactHolder> {
    private Context context;
    private List<User> itemList;

    public FriendReactAdapter(Context context, List<User> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void updateList(List<User> newList) {
        itemList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemFriendReactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_react, parent, false);
        return new ItemFriendReactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemFriendReactHolder holder, int position) {
        User u = itemList.get(position);
        String imageName = u.getUrlAvatar();

        // Load ảnh từ URL bằng Glide
        Glide.with(context)
                .load(imageName)
                .placeholder(R.drawable.avt) // ảnh tạm khi đang load
                .error(R.drawable.avt) // ảnh khi load lỗi
                .into(holder.getIvAvt());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
