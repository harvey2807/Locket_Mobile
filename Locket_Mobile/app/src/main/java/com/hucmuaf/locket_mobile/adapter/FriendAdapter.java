package com.hucmuaf.locket_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.model.User;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {
    
    private List<User> friendsList;
    private OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onRemoveFriend(User user);
    }

    public FriendAdapter(List<User> friendsList, OnFriendActionListener listener) {
        this.friendsList = friendsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        if (friendsList != null && position < friendsList.size()) {
            User friend = friendsList.get(position);
            if (friend != null) {
                holder.bind(friend);
            }
        }
    }

    @Override
    public int getItemCount() {
        return friendsList != null ? friendsList.size() : 0;
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView name;
        private ImageView removeButton;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.friendAvatar);
            name = itemView.findViewById(R.id.friendName);
            removeButton = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(User friend) {
            if (friend == null) return;
            
            if (name != null) {
                name.setText(friend.getFullName() != null ? friend.getFullName() : friend.getUserName());
            }
            
            // Load avatar image using Glide
            if (avatar != null) {
                if (friend.getUrlAvatar() != null && !friend.getUrlAvatar().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(friend.getUrlAvatar())
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.avt)
                            .error(R.drawable.avt)
                            .into(avatar);
                } else {
                    // Set default avatar
                    avatar.setImageResource(R.drawable.avt);
                }
            }
            
            if (removeButton != null) {
                removeButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRemoveFriend(friend);
                    }
                });
            }
        }
    }
} 