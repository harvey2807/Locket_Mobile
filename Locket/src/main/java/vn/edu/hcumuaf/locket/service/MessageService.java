package vn.edu.hcumuaf.locket.service;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import vn.edu.hcumuaf.locket.model.Message;
//import vn.edu.hcumuaf.locket.model.Message;
import org.springframework.stereotype.Service;
import vn.edu.hcumuaf.locket.responsitory.MessageDao;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class MessageService {
    @Autowired
    private MessageDao messageDao;
    private FirebaseDatabase database;
    private Random random;

    public MessageService(FirebaseDatabase database) {
        this.database = database;
        random = new Random();
    }

    //just write data on database
    public void saveMessage(Message message) {
        DatabaseReference ref = database.getReference("messages").push();
        Map<String, Object> messageData = new HashMap<>();
        message.getId();
//        message.setId(message.getSenderId() + "_" + message.getReceiverId() + ":" + random.nextInt());
        message.setTimestamp(System.currentTimeMillis());

        messageData.put("id", message.getId());
        messageData.put("senderId", message.getSenderId());
        messageData.put("content", message.getContent());
        messageData.put("timestamp", message.getTimestamp());
        ref.setValueAsync(message);
    }


    public CompletableFuture<List<Message>> getMessagesByUserId(String senderId) {
        return messageDao.getMessagesByUserId(senderId)
                .thenApply(messages -> {
                    messages.sort(Comparator.comparingLong(Message::getTimestamp).reversed());
                    return messages;
                });
    }

    public CompletableFuture<List<Message>> getReceiverByUserId(String senderId) {
        return messageDao.getReceiverIdByUserID(senderId);
    }

    public CompletableFuture<List<Message>> getAllMessages() {
        return messageDao.getAllMessage();
    }

}

