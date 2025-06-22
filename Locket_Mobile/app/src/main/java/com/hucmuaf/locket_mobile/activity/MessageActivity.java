package com.hucmuaf.locket_mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.adapter.MessageAdapter;
import com.hucmuaf.locket_mobile.inteface.onMessageLoaded;
import com.hucmuaf.locket_mobile.model.Message;
import com.hucmuaf.locket_mobile.repo.MessageRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageActivity extends AppCompatActivity {
    private MessageRepository messageRepository;
    private List<Message> messageList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private DatabaseReference db;
    private String currentUserId;
    private ImageView backButton;
    private TextView title;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<String> receiverIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        messageRepository = new MessageRepository();

        initializeViews();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getUid() != null) {
            currentUserId = currentUser.getUid();
            Log.d("MessageActivity", "Current user ID: " + currentUserId);

            messageRepository.getReceiverByUserId(currentUserId, new onMessageLoaded() {
                @Override
                public void onSuccess(List<Message> messages) {
                    Set<String> tempReceiverIds = new HashSet<>();
                    for (Message message : messages) {
                        if (message.getReceiverId() != null) {
                            tempReceiverIds.add(message.getReceiverId());
                        }
                    }
                    receiverIds.addAll(tempReceiverIds);
                    Log.w("MessageActivity", "Receiver IDs: " + receiverIds + ", Size: " + receiverIds.size());

                    if (recyclerViewMessages != null) {
                        if (messageAdapter == null) {
                            messageAdapter = new MessageAdapter(MessageActivity.this, messageList, receiverIds, currentUserId, recyclerViewMessages);
                            recyclerViewMessages.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                            recyclerViewMessages.setAdapter(messageAdapter);
                            Log.d("MessageActivity", "RecyclerView initialized with size: " + receiverIds.size());
                        } else {
                            messageAdapter.notifyDataSetChanged();
                        }
                    }

                    for (String id : receiverIds) {
                        loadUserProfile(id);
                        loadLastMessages(id);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("MessageActivity", "Failed to load receivers: " + e.getMessage());
                    setDefaultState();
                }
            });
        } else {
            Log.w("MessageActivity", "No user logged in");
            setDefaultState();
        }

        backButton.setOnClickListener(v -> onBackPressed());
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        title = findViewById(R.id.title);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
    }

    private void loadUserProfile(String userId) {
        if (db == null) {
            Log.e("MessageActivity", "Database is not initialized");
            return;
        }

        db.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("fullName").getValue(String.class);
                    String avtUrl = dataSnapshot.child("urlAvatar").getValue(String.class);

                    int position = receiverIds.indexOf(userId);
                    if (position != -1 && messageAdapter != null) {
                        messageAdapter.updateUserInfo(position, name, avtUrl);
                        messageAdapter.notifyItemChanged(position);
                    }
                } else {
                    Log.w("MessageActivity", "User not found: " + userId);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e("MessageActivity", "Error getting user profile: " + databaseError.getMessage());
            }
        });
    }

    private void loadLastMessages(String userId) {
        messageRepository.getLastMessageWithUserId(userId, new onMessageLoaded() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (!messages.isEmpty()) {
                    Message lastMessage = messages.get(0);
                    int position = receiverIds.indexOf(userId);
                    if (position != -1 && messageAdapter != null) {
                        messageList.add(lastMessage);
                        messageAdapter.updateMessage(position, lastMessage);
                        messageAdapter.notifyItemChanged(position);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MessageActivity", "Error loading last message: " + e.getMessage());
            }
        });
    }

    private int findReceiverPosition(String receiverId) {
        return receiverIds.indexOf(receiverId);
    }

    private void setDefaultState() {
        title.setText("Messages");
        if (recyclerViewMessages != null && messageAdapter != null) {
            messageAdapter.notifyDataSetChanged();
        }
    }
}