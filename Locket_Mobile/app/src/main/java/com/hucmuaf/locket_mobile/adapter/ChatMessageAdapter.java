package com.hucmuaf.locket_mobile.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.holder.ChatMessageViewHolder;
import com.hucmuaf.locket_mobile.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
    private static final String TAG = "ChatMessageAdapter";
    private List<Message> listMess;
    private String currentUser;
    private RecyclerView recyclerView;
    private Context context;
    private MessageAdapter messageAdapter;

    public ChatMessageAdapter(Context context, List<Message> listMess, String currentUser, RecyclerView recyclerView) {
        this.context = context;
        this.listMess = listMess != null ? listMess : new ArrayList<>();
        this.currentUser = currentUser;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        Message message = listMess.get(position);
        if (message == null) {
            Log.w(TAG, "Message at position " + position + " is null");
            return;
        }

        try {
            if (holder.getMessageContent() != null) {
                String content = message.getContent();
                Log.w("ChatMessageAdapter", "Content: " + content);
                if (content == null) {
                    holder.getMessageContent().setText("No content");
                } else {
                    holder.getMessageContent().setText(content);
                    Log.w(TAG, "Dcm message" + position);
                }
//                holder.getMessageContent().setText(content);
                Log.w(TAG, "Set successfully message" + position);
            } else {
                Log.e(TAG, "MessageContent TextView is null at position " + position);
                return;
            }

            if (holder.getMessageTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String formattedTime = sdf.format(message.getTimestamp());
                holder.getMessageTimestamp().setText(formattedTime);
            } else {
                Log.w(TAG, "MessageTimestamp TextView is null at position " + position);
            }

            if (message.getSenderId() != null && message.getSenderId().equals(currentUser)) {
                Log.w("ChatMessageAdapter", "THis is current user" + currentUser);
                styleSentMessage(holder);
            } else {
                styleReceivedMessage(holder);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding message at position " + position, e);
        }
    }

    private void styleSentMessage(ChatMessageViewHolder holder) {
        try {
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
//            holder.itemView.setLayoutParams(params);

            if (holder.getMessageContent() != null) {
                holder.getMessageContent().setBackgroundResource(R.drawable.message_background_sent);
                holder.getMessageAvatar().setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                holder.getMessageContent().setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                Log.w(TAG, "Set sucessfully layout for this message");
            }

//            if (holder.getMessageAvatar() != null) {
//                holder.getMessageAvatar().setVisibility(View.GONE);
//            }

            holder.itemView.setPadding(50, 8, 10, 8);
        } catch (Exception e) {
            Log.e(TAG, "Error styling sent message", e);
        }
    }

    private void styleReceivedMessage(ChatMessageViewHolder holder) {
        try {
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
//            holder.itemView.setLayoutParams(params);
//
            if (holder.getMessageContent() != null) {
                holder.getMessageContent().setBackgroundResource(R.drawable.message_background_received);
                holder.getMessageContent().setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            }
            if (holder.getMessageAvatar() != null) {
                holder.getMessageAvatar().setVisibility(View.GONE);
            }
//
//            // Set padding
            holder.itemView.setPadding(400, 8, 20, 8);
        } catch (Exception e) {
            Log.e(TAG, "Error styling received message", e);
        }
    }


    @Override
    public int getItemCount() {
        return listMess != null ? listMess.size() : 0;
    }

    public void updateMessages(List<Message> messages) {
        if (messages != null) {
            this.listMess = new ArrayList<>(messages);
            notifyDataSetChanged();
        } else {
            Log.w(TAG, "Attempted to update with null messages list");
        }
    }

    public void addMessage(Message message) {
        if (message != null && listMess != null) {
            listMess.add(message);
            notifyItemInserted(listMess.size() - 1);

            // Scroll to bottom to show new message
            if (recyclerView != null) {
                recyclerView.scrollToPosition(listMess.size() - 1);
            }
        }
    }

    public void clearMessages() {
        if (listMess != null) {
            listMess.clear();
            notifyDataSetChanged();
        }
    }
}