package com.hucmuaf.locket_mobile.repo;

import android.util.Log;

import androidx.annotation.NonNull;

import com.airbnb.lottie.L;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.hucmuaf.locket_mobile.inteface.onMessageLoaded;
import com.hucmuaf.locket_mobile.model.Message;
import com.hucmuaf.locket_mobile.service.FirebaseService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageRepository implements onMessageLoaded {
    private DatabaseReference db;

    public MessageRepository() {
        this.db = FirebaseService.getInstance().getDatabase().getReference("messages");
    }

    public void getReceiverByUserId(String userId, onMessageLoaded callback) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> receiverIds = new HashSet<>();
                List<Message> relatedMessages = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    if (message == null) continue;

                    String sender = message.getSenderId();
                    String receiver = message.getReceiverId();

                    if (userId.equals(sender)) {
                        receiverIds.add(receiver);
                        relatedMessages.add(message);
                    } else if (userId.equals(receiver)) {
                        receiverIds.add(sender);
                        relatedMessages.add(message);
                    }
                }

                callback.onSuccess(relatedMessages);
                Log.d("MessageRepository", "Unique receiverIds: " + receiverIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
            }
        });
    }


    @Override
    public void onSuccess(List<Message> mess) {

    }

    @Override
    public void onFailure(Exception e) {

    }

    public void getLastMessageWithUserId(String userId, onMessageLoaded callback) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> relatedMessages = new ArrayList<>();
                Message latestMessage = null;
                long latestTimestamp = -1;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    if (message == null) continue;

                    String sender = message.getSenderId();
                    String receiver = message.getReceiverId();

                    if (userId.equals(sender) || userId.equals(receiver)) {
                        relatedMessages.add(message);
                        // Tìm tin nhắn có timestamp lớn nhất
                        if (message.getTimestamp() > latestTimestamp) {
                            latestTimestamp = message.getTimestamp();
                            latestMessage = message;
                        }
                    }
                }

                List<Message> result = new ArrayList<>();
                if (latestMessage != null) {
                    result.add(latestMessage);
                }
                callback.onSuccess(result);
                Log.d("MessageRepository", "Last message for user " + userId + ": " + (latestMessage != null ? latestMessage.getContent() : "null"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
                Log.e("MessageRepository", "Failed to load last message: " + error.getMessage());
            }
        });
    }

    public void getMessageBetweenUserWithReceiver(String userId, String receiverId, onMessageLoaded callback) {
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> listMess = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    if (message == null) continue;

                    String sender = message.getSenderId();
                    String receiver = message.getReceiverId();
                    if ((sender != null && receiver != null) &&
                            (sender.equals(userId) && receiver.equals(receiverId) ||
                                    sender.equals(receiverId) && receiver.equals(userId))) {
                        listMess.add(message);
                    }
                }

                callback.onSuccess(listMess);
                Log.d("MessageRepository", "Messages between " + userId + " and " + receiverId + ": " + listMess.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.toException());
                Log.w("MessageRepository", "Failed to get message between " + userId + " and " + receiverId + ": " + error.getMessage());
            }
        });
    }
}
