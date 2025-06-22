//package com.hucmuaf.locket_mobile.adapter;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//import com.hucmuaf.locket_mobile.R;
//import com.hucmuaf.locket_mobile.activity.ChatActivity;
//import com.hucmuaf.locket_mobile.holder.MessageViewHolder;
//import com.hucmuaf.locket_mobile.model.Message;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
//    private List<Message> messageList;
//    private Context context;
//    private List<String> receiverIds;
//    private String currentUserId;
//    private RecyclerView recyclerView;
//
//
//    public MessageAdapter(Context context, List<Message> messageList, List<String> receiverIds, String currentUserId, RecyclerView recyclerView) {
//        this.context = context;
//        this.messageList = messageList != null ? messageList : new ArrayList<>();
//        this.receiverIds = receiverIds != null ? receiverIds : new ArrayList<>();
//        this.currentUserId = currentUserId;
//        this.recyclerView = recyclerView;
//    }
//
//    @NonNull
//    @Override
//    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
//        return new MessageViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
//        String receiverId = receiverIds.get(position);
//        Log.w("MessageAdapter", "Position" + position);
//        Log.w("MessageAdapter", "Receiver ID: " + receiverId);
//
//        holder.getUsername().setText(receiverId);
//        holder.getLastMessage().setText("");
//        holder.getTimestamp().setText("");
//
//        Glide.with(context)
//                .load(R.drawable.default_avatar)
//                .circleCrop()
//                .placeholder(R.drawable.default_avatar)
//                .error(R.drawable.default_avatar)
//                .into(holder.getAvatar());
//
//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, ChatActivity.class);
//            intent.putExtra("otherUserId", receiverId);
//            context.startActivity(intent);
//        });
//
//        if (position < messageList.size() && messageList.get(position) != null) {
//            Message message = messageList.get(position);
//            holder.getLastMessage().setText(message.getContent());
//            String timeDiff = getTimeDifference(message.getTimestamp());
//            holder.getTimestamp().setText(timeDiff);
//        }
//    }
//
//    private String getTimeDifference(long timestamp) {
//        long diff = (System.currentTimeMillis() - timestamp) / 1000;
//        if (diff < 3600) {
//            return (diff / 60) + "m";
//        } else if (diff < 86400) {
//            return (diff / 3600) + "h";
//        } else { // Trên 1 ngày
//            return (diff / 86400) + "d";
//        }
//    }
//
//    public void updateUserInfo(int position, String name, String avtUrl) {
//        if (position >= 0 && position < messageList.size()) {
//            Message message = messageList.get(position);
//            MessageViewHolder holder = (MessageViewHolder) recyclerViewFindViewHolder(position);
//            if (holder != null) {
//                if (name != null) {
//                    holder.getUsername().setText(name);
//                } else {
//                    holder.getUsername().setText(message.getReceiverId());
//                }
//
//                if (avtUrl != null && !avtUrl.isEmpty()) {
//                    Glide.with(context)
//                            .load(avtUrl)
//                            .circleCrop()
//                            .placeholder(R.drawable.default_avatar)
//                            .into(holder.getAvatar());
//                } else {
//                    Glide.with(context)
//                            .load(R.drawable.default_avatar)
//                            .circleCrop()
//                            .into(holder.getAvatar());
//                }
//                notifyItemChanged(position);
//            }
//        } else {
//            Log.w("MessageAdapter", "Invalid position for updateUserInfo: " + position);
//        }
//    }
//
//    private RecyclerView.ViewHolder recyclerViewFindViewHolder(int position) {
//        RecyclerView recyclerView = ((Activity) context).findViewById(R.id.recyclerViewMessages);
//        if (recyclerView != null) {
//            return recyclerView.findViewHolderForAdapterPosition(position);
//        }
//        return null;
//    }
//    public void updateMessage(int position, Message message) {
//        if (position >= 0 && position < receiverIds.size()) {
//            while (messageList.size() <= position) {
//                messageList.add(null);
//            }
//            messageList.set(position, message);
//            notifyItemChanged(position);
//        }
//    }
//    @Override
//    public int getItemCount() {
//        return messageList.size();
//    }
//
//}

package com.hucmuaf.locket_mobile.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.hucmuaf.locket_mobile.activity.ChatActivity;
import com.hucmuaf.locket_mobile.holder.MessageViewHolder;
import com.hucmuaf.locket_mobile.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private List<Message> messageList;
    private Context context;
    private List<String> receiverIds;
    private String currentUserId;
    private RecyclerView recyclerView;

    public MessageAdapter(Context context, List<Message> messageList, List<String> receiverIds, String currentUserId, RecyclerView recyclerView) {
        this.context = context;
        this.messageList = messageList != null ? messageList : new ArrayList<>();
        this.receiverIds = receiverIds != null ? receiverIds : new ArrayList<>();
        this.currentUserId = currentUserId;
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message_list, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        // Validate position bounds for receiverIds
        if (position >= receiverIds.size()) {
            Log.e("MessageAdapter", "Position " + position + " is out of bounds for receiverIds (size: " + receiverIds.size() + ")");
            return;
        }

        String receiverId = receiverIds.get(position);
        Log.w("MessageAdapter", "Position: " + position);
        Log.w("MessageAdapter", "Receiver ID: " + receiverId);

        // Set default values
        holder.getUsername().setText(receiverId);
        holder.getLastMessage().setText("");
        holder.getTimestamp().setText("");

        // Load default avatar
        Glide.with(context)
                .load(R.drawable.default_avatar)
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .into(holder.getAvatar());

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("otherUserId", receiverId);
            context.startActivity(intent);
        });

        // Update with message data if available
        if (position < messageList.size() && messageList.get(position) != null) {
            Message message = messageList.get(position);
            holder.getLastMessage().setText(message.getContent());
            String timeDiff = getTimeDifference(message.getTimestamp());
            holder.getTimestamp().setText(timeDiff);
        }
    }

    private String getTimeDifference(long timestamp) {
        long diff = (System.currentTimeMillis() - timestamp) / 1000;
        if (diff < 60) {
            return "now";
        } else if (diff < 3600) {
            return (diff / 60) + "m";
        } else if (diff < 86400) {
            return (diff / 3600) + "h";
        } else {
            return (diff / 86400) + "d";
        }
    }

    public void updateUserInfo(int position, String name, String avtUrl) {
        if (position < 0 || position >= receiverIds.size()) {
            Log.w("MessageAdapter", "Invalid position for updateUserInfo: " + position + " (receiverIds size: " + receiverIds.size() + ")");
            return;
        }
        while (messageList.size() <= position) {
            messageList.add(null);
        }

        MessageViewHolder holder = (MessageViewHolder) recyclerView.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            if (name != null && !name.trim().isEmpty()) {
                holder.getUsername().setText(name);
            } else {
                holder.getUsername().setText(receiverIds.get(position));
            }

                if (avtUrl != null && !avtUrl.trim().isEmpty()) {
                    Glide.with(context)
                            .asBitmap()
                            .load(avtUrl)
                            .circleCrop()
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(holder.getAvatar());
                    Log.w("MessafeAdapter", "Load avatar: " + avtUrl);
                } else {
                    Glide.with(context)
                            .load(R.drawable.default_avatar)
                            .circleCrop()
                            .into(holder.getAvatar());
                }
            holder.getAvatar().setOnClickListener(v -> {
                String receiverId = receiverIds.get(position);
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("otherUserId", receiverId);
                context.startActivity(intent);
            });
            holder.getUsername().setOnClickListener(v -> {
                String receiverId = receiverIds.get(position);
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("otherUserId", receiverId);
                context.startActivity(intent);
            });
            holder.getLastMessage().setOnClickListener(v -> {
                String receiverId = receiverIds.get(position);
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("otherUserId", receiverId);
                context.startActivity(intent);
            });
            notifyItemChanged(position);
        }
    }

    public void updateMessage(int position, Message message) {
        if (position < 0 || position >= receiverIds.size()) {
            Log.w("MessageAdapter", "Invalid position for updateMessage: " + position + " (receiverIds size: " + receiverIds.size() + ")");
            return;
        }

        while (messageList.size() <= position) {
            messageList.add(null);
        }

        messageList.set(position, message);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return receiverIds.size();
    }

    public void addReceiver(String receiverId) {
        receiverIds.add(receiverId);
        messageList.add(null); // Add placeholder message
        notifyItemInserted(receiverIds.size() - 1);
    }

    public void removeReceiver(int position) {
        if (position >= 0 && position < receiverIds.size()) {
            receiverIds.remove(position);
            if (position < messageList.size()) {
                messageList.remove(position);
            }
            notifyItemRemoved(position);
        }
    }

    public void clearAll() {
        int size = receiverIds.size();
        receiverIds.clear();
        messageList.clear();
        notifyItemRangeRemoved(0, size);
    }
}