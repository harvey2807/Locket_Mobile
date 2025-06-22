package com.hucmuaf.locket_mobile.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.inteface.OnSentRequestActionListener;
import com.hucmuaf.locket_mobile.model.FriendRequest;

public class SentRequestViewHolder extends RecyclerView.ViewHolder {
    ImageView avatarImageView;
    TextView nameTextView;
    private final Context context;
    private final OnSentRequestActionListener listener;
    ImageButton cancelButton;

    public SentRequestViewHolder(@NonNull View itemView, Context context, OnSentRequestActionListener listener) {
        super(itemView);
        this.context = context;
        this.listener = listener;
        avatarImageView = itemView.findViewById(R.id.sentRequestAvatar);
        nameTextView = itemView.findViewById(R.id.sentRequestName);
        cancelButton = itemView.findViewById(R.id.btnCancel);
    }

    @SuppressLint("SetTextI18n")
    public void bind(FriendRequest request) {
        // Hiển thị tên người nhận lời mời
        if (request.getSenderName() != null && !request.getSenderName().isEmpty()) {
            nameTextView.setText(request.getSenderName());
        } else if (request.getSender() != null && request.getSender().getFullName() != null && !request.getSender().getFullName().isEmpty()) {
            nameTextView.setText(request.getSender().getFullName());
        } else if (request.getSender() != null && request.getSender().getUserName() != null) {
            nameTextView.setText(request.getSender().getUserName());
        } else {
            nameTextView.setText("Unknown User");
        }
        
        loadAvatarSafely(request);
        
        // Xử lý sự kiện hủy lời mời
        cancelButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelRequest(request);
            }
        });
    }

    private void loadAvatarSafely(FriendRequest request) {
        Context safeContext = getSafeContext();
        if (safeContext == null) {
            avatarImageView.setImageResource(R.drawable.avt);
            return;
        }

        if (request.getSender() != null && request.getSender().getUrlAvatar() != null && !request.getSender().getUrlAvatar().isEmpty()) {
            try {
                Glide.with(safeContext)
                        .load(request.getSender().getUrlAvatar())
                        .placeholder(R.drawable.avt)
                        .error(R.drawable.avt)
                        .circleCrop()
                        .into(avatarImageView);
            } catch (Exception e) {
                avatarImageView.setImageResource(R.drawable.avt);
            }
        } else {
            avatarImageView.setImageResource(R.drawable.avt);
        }
    }

    private Context getSafeContext() {
        if (isContextValid()) {
            return context;
        }
        
        try {
            return itemView.getContext();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isContextValid() {
        return context != null && 
               !(context instanceof android.app.Activity && ((android.app.Activity) context).isFinishing()) &&
               !(context instanceof android.app.Activity && ((android.app.Activity) context).isDestroyed());
    }
}
