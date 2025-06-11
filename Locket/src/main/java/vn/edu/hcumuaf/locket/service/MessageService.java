package vn.edu.hcumuaf.locket.service;

import com.google.firebase.database.*;
import vn.edu.hcumuaf.locket.model.entity.Message;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
public class MessageService {

    private FirebaseDatabase database;

    public MessageService(FirebaseDatabase database) {
        this.database = database;
    }
//just write data on database
    public CompletableFuture<String> sendMessage(Message message) {
        DatabaseReference ref = database.getReference("messages").push();
        message.setTimestamp(System.currentTimeMillis());
        message.setMessageId(ref.getKey());
        CompletableFuture<String> future = new CompletableFuture<>();
        ref.setValue(message, (error, ref1) -> {
            if (error == null) {
                future.complete(message.getMessageId());
            } else {
                future.completeExceptionally(new RuntimeException("Failed to send message: " + error.getMessage()));
            }
        });
        return future;
    }

    public CompletableFuture<List<Message>> getMessages(String chatId) {
        DatabaseReference ref = database.getReference("messages").child(chatId);
        CompletableFuture<List<Message>> future = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Message message = child.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                future.complete(messages);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Error fetching messages: " + error.getMessage()));
            }
        });

        return future;
    }

    public void listenForMessages(String chatId, Consumer<List<Message>> onUpdate) {
        DatabaseReference ref = database.getReference("messages").child(chatId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Message message = child.getValue(Message.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                onUpdate.accept(messages);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                throw new RuntimeException("Error listening for messages: " + error.getMessage());
            }
        });
    }
}

