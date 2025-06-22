package com.hucmuaf.locket_mobile.holder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.inteface.OnAddFriendClickListener;
import com.hucmuaf.locket_mobile.model.User;

public class SearchUserViewHolder extends RecyclerView.ViewHolder {
    private final ImageView userAvatar;
    private final TextView userName;
    private final TextView userFullName;
    private final Button addFriendButton;
    private final Context context;
    private final OnAddFriendClickListener listener;

    public SearchUserViewHolder(@NonNull View itemView, Context context, OnAddFriendClickListener listener) {
        super(itemView);
        this.context = context;
        this.listener = listener;
        userAvatar = itemView.findViewById(R.id.userAvatar);
        userName = itemView.findViewById(R.id.userName);
        userFullName = itemView.findViewById(R.id.userFullName);
        addFriendButton = itemView.findViewById(R.id.addFriendButton);
    }

    public void bind(User user) {
        userName.setText(user.getUserName());
        userFullName.setText(user.getFullName());

        if (user.getUrlAvatar() != null && !user.getUrlAvatar().isEmpty()) {
            Glide.with(context)
                    .load(user.getUrlAvatar())
                    .placeholder(R.drawable.avt)
                    .error(R.drawable.avt)
                    .circleCrop()
                    .into(userAvatar);
        } else {
            userAvatar.setImageResource(R.drawable.avt);
        }

        addFriendButton.setOnClickListener(v -> {
            if (listener != null) {
                addFriendButton.setText("Đã gửi");
                addFriendButton.setEnabled(false);
                addFriendButton.setBackgroundResource(R.drawable.bg_gray_chip);
                
                listener.onAddFriendClick(user);
            }
        });
    }
}