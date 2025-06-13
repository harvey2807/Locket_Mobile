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

import java.util.List;

public class ItemFriendAdapter extends RecyclerView.Adapter<ItemFriendAdapter.ItemFriendViewHolder> {
    private Context context;
    private List<ItemFriend> itemList;

    public ItemFriendAdapter(Context context, List<ItemFriend> itemList) {
        this.context = context;
        this.itemList = itemList;
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
        ItemFriend item = itemList.get(position);
        String imageName = item.getIcon();
        @SuppressLint("DiscouragedApi")
        int resId = context.getResources().getIdentifier(imageName, "mipmap", context.getPackageName());
        if (resId != 0) {
            holder.ivIcon.setImageResource(resId);
        } else {
            // Xử lý khi không tìm thấy resource
            Log.e("ImageError", "Không tìm thấy hình " + imageName);
        }
        holder.tvName.setText(item.getName());
        Log.e("ImageError", item.getName());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}
