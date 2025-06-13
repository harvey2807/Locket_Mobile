package com.hucmuaf.locket_mobile.model;

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

import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.ModelDB.User;

import java.util.List;

public class ItemFriendAdapter extends RecyclerView.Adapter<ItemFriendAdapter.ItemFriendViewHolder> {
    private Context context;
    private List<User> itemList;
    private OnFriendClickListener listener;

    public interface OnFriendClickListener{
        void onFriendClick(User user);
    }

    public ItemFriendAdapter(Context context, List<User> itemList
            , OnFriendClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.listener = listener;
    }

    public void updateList(List<User> newList){
        itemList = newList;
        notifyDataSetChanged();
    }

    // ViewHolder class
    public static class ItemFriendViewHolder extends RecyclerView.ViewHolder{
        ImageView ivIcon;
        TextView tvName;
        @SuppressLint("WrongViewCast")
        public ItemFriendViewHolder(@NonNull View itemView) {
            super(itemView);
//            ivIcon = itemView.findViewById(R.id.ivIcon);
//            tvName = itemView.findViewById(R.id.tvName);
        }

        public void bind(User user, OnFriendClickListener listener){
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFriendClick(user);
                }
            });
        }
    }

    @NonNull
    @Override
    public ItemFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new ItemFriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemFriendViewHolder holder, int position) {
        User u = itemList.get(position);
        holder.bind(u, listener);
        String imageName = u.getUrlAvatar();
        @SuppressLint("DiscouragedApi")
        int resId = context.getResources().getIdentifier(imageName, "mipmap", context.getPackageName());
        if (resId != 0) {
            holder.ivIcon.setImageResource(resId);
        } else {
            // Xử lý khi không tìm thấy resource
            Log.e("ImageError", "Không tìm thấy hình " + imageName);
        }
        holder.tvName.setText(u.getFullName());
        Log.e("ImageError", u.getFullName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
