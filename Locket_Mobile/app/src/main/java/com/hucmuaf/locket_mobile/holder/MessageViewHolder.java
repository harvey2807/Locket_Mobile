package com.hucmuaf.locket_mobile.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hucmuaf.locket_mobile.R;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    ImageView avatar;
    TextView username;
    TextView lastMessage;
    TextView timestamp;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        avatar = itemView.findViewById(R.id.messageAvatar);
        username = itemView.findViewById(R.id.username);
        lastMessage = itemView.findViewById(R.id.lastMessage);
        timestamp = itemView.findViewById(R.id.timestamp);
    }

    public ImageView getAvatar() {
        return avatar;
    }

    public void setAvatar(ImageView avatar) {
        this.avatar = avatar;
    }

    public TextView getUsername() {
        return username;
    }

    public void setUsername(TextView username) {
        this.username = username;
    }

    public TextView getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(TextView lastMessage) {
        this.lastMessage = lastMessage;
    }

    public TextView getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(TextView timestamp) {
        this.timestamp = timestamp;
    }
}