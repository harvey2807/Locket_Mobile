package com.hucmuaf.locket_mobile.service;


import android.util.Log;

import com.hucmuaf.locket_mobile.model.Message;
import com.hucmuaf.locket_mobile.model.MessageType;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MessageService {
    private static MessageService instance;
    private WebSocket webSocket;
    private OkHttpClient client;
    private MessageListAPIService api;

    private MessageService() {
        client = new OkHttpClient.Builder()
                .readTimeout(3, TimeUnit.SECONDS)
                .build();
        connectWebSocket();
        api = ApiClient.getMessageListApiService();
    }

    public static synchronized MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }

    private void connectWebSocket() {
        Request request = new Request.Builder()
                .url("ws://192.168.0.113:8080/ws")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("MessageService", "WebSocket connection opened");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e("MessageService", "WebSocket error: " + t.getMessage());
            }
        });
    }

    public void sendMessage(String senderId, String receiverId, String content) {
        if (webSocket == null) {
            Log.w("MessageService", "WebSocket is null - attempting to reconnect");
            connectWebSocket();
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            Log.w("MessageService", "Cannot send empty message");
            return;
        }

        Message message = new Message(senderId, receiverId, content, System.currentTimeMillis(), String.valueOf(MessageType.CHAT));
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("content", message.getContent());
            jsonObject.put("senderId", message.getSenderId());
            jsonObject.put("receiverId", message.getReceiverId());
            jsonObject.put("timestamp", message.getTimestamp());
            jsonObject.put("type", message.getType());

            if (webSocket != null) {
                webSocket.send(jsonObject.toString());
                Log.d("WebSocket", "Sent: " + jsonObject.toString());
            }
            if (api != null) {
                api.sendMessage(new Message(message.getSenderId(), message.getReceiverId(), message.getContent(), message.getTimestamp(), message.getType()))
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
            Log.e("MessageService", "Error sending message", e);
        }
    }

    public void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Service shutdown");
            webSocket = null;
        }
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}