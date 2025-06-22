package com.hucmuaf.locket_mobile.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.adapter.ChatMessageAdapter;
import com.hucmuaf.locket_mobile.inteface.onMessageLoaded;
import com.hucmuaf.locket_mobile.model.Message;
import com.hucmuaf.locket_mobile.model.MessageType;
import com.hucmuaf.locket_mobile.repo.MessageRepository;
import com.hucmuaf.locket_mobile.service.ApiClient;
import com.hucmuaf.locket_mobile.service.MessageListAPIService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private FirebaseAuth mAuth;
    private MessageListAPIService apiService;
    private ImageView avt;
    private TextView name;
    private TextView timestamp;
    private EditText editTextMessage;
    private ImageView backbutton;
    private ChatMessageAdapter chatMessageAdapter;
    private ImageView sendButton;
    private RecyclerView recyclerViewMessages;
    private ChatMessageAdapter messageAdapter;
    private List<Message> messageList;
    private String currentUserId;
    private String otherUserId;
    private MessageListAPIService api;
    private DatabaseReference db;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private WebSocket webSocket;
    private OkHttpClient client;
    private String imageId;
    private MessageRepository messageRepository;

//    private static final String WS_SERVER_URL = "ws://192.168.181.190:8080/ws"; // Fixed URL format

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        api = ApiClient.getMessageListApiService();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getUid() != null) {
            currentUserId = currentUser.getUid();
            otherUserId = getIntent().getStringExtra("otherUserId");

            if (otherUserId == null || otherUserId.isEmpty()) {
                Log.e(TAG, "otherUserId not provided or empty");
                Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Current user: " + currentUserId + ", Other user: " + otherUserId);
            initializeViews();
            loadUserProfile(otherUserId);
            setupRecyclerView();
            loadInitialMessages();
            new android.os.Handler().postDelayed(this::connectWebSocket, 1000);

        } else {
            currentUserId = "";
            Log.w(TAG, "No user logged in");
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClickListeners();
    }

    private void initializeViews() {
        backbutton = findViewById(R.id.account13);
        avt = findViewById(R.id.account6);
        name = findViewById(R.id.textView16);
        editTextMessage = findViewById(R.id.editTextText);
        sendButton = findViewById(R.id.account15);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);

        if (name != null) {
            name.setText("Unknown");
        }

        backbutton.setOnClickListener(v -> onBackPressed());

        Log.d(TAG, "Views initialized");
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(this, messageList, currentUserId, recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);
        Log.d(TAG, "RecyclerView initialized");
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> {
            String messageContent = editTextMessage.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessage(messageContent);
                editTextMessage.setText("");
            }
        });

        editTextMessage.setOnLongClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (pickImageLauncher != null) {
                pickImageLauncher.launch(intent);
            }
            return true;
        });
    }

    private void loadInitialMessages() {
        messageRepository = new MessageRepository();
        messageRepository.getMessageBetweenUserWithReceiver(currentUserId, otherUserId, new onMessageLoaded() {
            @Override
            public void onSuccess(List<Message> mess) {
                runOnUiThread(() -> {
                    if (!mess.isEmpty()) {
                        messageList.clear();
                        messageList.addAll(mess);
                        messageList.sort((m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
                        messageAdapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                        }
                        Log.d(TAG, "Load initial messages successfully: " + messageList.size() + " between " + currentUserId + " and " + otherUserId);
                    } else {
                        Log.d(TAG, "No messages found between " + currentUserId + " and " + otherUserId);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load initial messages: " + e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadUserProfile(String userId) {
        if (db == null) {
            Log.e(TAG, "Database is not initialized");
            return;
        }

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "userId is null or empty");
            return;
        }

        Log.d(TAG, "Loading user profile for: " + userId);

        db.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userName = dataSnapshot.child("fullName").getValue(String.class);
                    String avtUrl = dataSnapshot.child("urlAvatar").getValue(String.class);

                    Log.d(TAG, "User data found - Name: " + userName + ", Avatar: " + avtUrl);

                    if (ChatActivity.this.name != null) {
                        String displayName = (userName != null && !userName.isEmpty()) ? userName : "Unknown User";
                        ChatActivity.this.name.setText(displayName);
                        Log.d(TAG, "Set username to: " + displayName);
                    } else {
                        Log.e(TAG, "Name TextView is null");
                    }

                    if (avtUrl != null && !avtUrl.isEmpty() && avt != null) {
                        Log.d(TAG, "Avatar URL available: " + avtUrl);
                    }

                    Log.d(TAG, "Loaded user profile successfully for: " + userId);
                } else {
                    Log.w(TAG, "User not found in database: " + userId);
                    if (ChatActivity.this.name != null) {
                        ChatActivity.this.name.setText("User Not Found");
                    }
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e(TAG, "Error getting user profile: " + databaseError.getMessage());
                if (ChatActivity.this.name != null) {
                    ChatActivity.this.name.setText("Error Loading User");
                }
            }
        });
    }

    private void connectWebSocket() {
        client = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url("ws://172.16.1.37:8080/ws")
//                .url("ws://192.168.0.112:8080/ws")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                Log.d("WebSocket", "Connection opened");
                // Register user
                Message registerMessage = new Message();
                registerMessage.setSenderId(currentUserId);
                registerMessage.setType(String.valueOf(MessageType.JOIN));
                sendWebSocketMessage(registerMessage);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        Message message = new Message();
                        message.setSenderId(jsonObject.getString("senderId"));
                        message.setReceiverId(jsonObject.getString("receiverId"));
                        message.setContent(jsonObject.optString("content", null));
//                        message.setImageUrl(jsonObject.optString("imageUrl", null));
//                        message.setCaption(jsonObject.optString("caption", null));
                        message.setTimestamp(jsonObject.getLong("timestamp"));
                        message.setType(String.valueOf(MessageType.valueOf(jsonObject.getString("type"))));

                        if (!message.getSenderId().equals(currentUserId) || message.getType().equals(MessageType.JOIN)) {
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            recyclerViewMessages.scrollToPosition(messageList.size() - 1);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                Log.d("WebSocket", "Closing: " + reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Log.e("WebSocket", "Error: " + t.getMessage());
            }
        });
    }

    private void sendWebSocketMessage(Message message) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("senderId", message.getSenderId());
            jsonObject.put("receiverId", message.getReceiverId());
            jsonObject.put("content", message.getContent());
            jsonObject.put("timestamp", message.getTimestamp());
            jsonObject.put("type", message.getType());

            if (webSocket != null) {
                webSocket.send(jsonObject.toString());
                Log.d("WebSocket", "Sent: " + jsonObject.toString());
            }
            if (api != null) {
                api.sendMessage(new Message(currentUserId, otherUserId, message.getContent(), System.currentTimeMillis(), "CHAT"))
//                api.sendMessage(currentUserId, otherUserId, message.getContent())
                        .enqueue(new retrofit2.Callback<Void>() {
                            @Override
                            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                                Log.d("API", "Message sent successfully");
                            }

                            @Override
                            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                                Log.e("API", "Failed to send message: " + t.getMessage());
                            }
                        });
            }
        } catch (Exception e) {
            Log.e("WebSocket", "Error sending message", e);
        }
    }


//    private void sendWebSocketMessage(Message message) {
//        try {
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("senderId", message.getSenderId());
//            jsonObject.put("receiverId", message.getReceiverId());
//            jsonObject.put("content", message.getContent());
////            jsonObject.put("imageUrl", message.getImageUrl());
////            jsonObject.put("caption", message.getCaption());
//            jsonObject.put("timestamp", message.getTimestamp());
//            jsonObject.put("type", message.getType().toString());
//
//            if (webSocket != null) {
//                webSocket.send(jsonObject.toString());
//                Log.d("WebSocket", "Sent: " + jsonObject.toString());
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private void sendMessage(String content) {
        Message newMessage = new Message(currentUserId, otherUserId, content, System.currentTimeMillis(), "JOIN");
        newMessage.setType(String.valueOf(MessageType.CHAT));
        sendWebSocketMessage(newMessage);
        messageList.add(newMessage);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
    }

//    private void sendImageMessage(Uri imageUri) {
//        // Upload image to server and get URL (example using Firebase Storage or your backend)
//        String imageUrl = "http://example.com/image.jpg"; // Replace with actual upload logic
//        String caption = editTextMessage.getText().toString().trim();
//        Message newMessage = new Message(currentUserId, otherUserId, null, imageUrl, caption, System.currentTimeMillis());
//        newMessage.setType(Message.MessageType.CHAT);
//        sendWebSocketMessage(newMessage);
//        messageList.add(newMessage);
//        messageAdapter.notifyItemInserted(messageList.size() - 1);
//        recyclerViewMessages.scrollToPosition(messageList.size() - 1);
//        editTextMessage.setText("");
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webSocket != null) {
            webSocket.close(1000, "Activity destroyed");
            client.dispatcher().executorService().shutdown();
        }
    }
}