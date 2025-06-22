package com.hucmuaf.locket_mobile.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hucmuaf.locket_mobile.R;

public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView messageText;
    private TextView time;
    private ImageView messageAvatar;

    public ChatMessageViewHolder(View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.messageText);
        time = itemView.findViewById(R.id.time);
        messageAvatar = itemView.findViewById(R.id.messageAvatar);
    }

    public TextView getMessageContent() { return messageText; }
    public TextView getMessageTimestamp() { return time; }
    public ImageView getMessageAvatar() { return messageAvatar; }
}