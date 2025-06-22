package vn.edu.hcumuaf.locket.responsitory;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hcumuaf.locket.model.Message;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class MessageDao {
    private final DatabaseReference dbRef;

    @Autowired
    public MessageDao(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference("messages");
    }

    // find list message by senderid
    public CompletableFuture<List<Message>> getMessagesByUserId(String userId) {
        CompletableFuture<List<Message>> future = new CompletableFuture<>();
        dbRef.orderByChild("senderId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Message> result = new ArrayList<>();
//                        Message chatMessage = dataSnapshot.getValue(Message.class);
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Message message = child.getValue(Message.class);
                            if (message != null) {
                                result.add(message);
                            }
                        }
                        future.complete(result);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });
        return future;
    }

    public CompletableFuture<List<Message>> getReceiverIdByUserID(String userId) {
        CompletableFuture<List<Message>> future = new CompletableFuture<>();
        Query query = dbRef.orderByChild("receiverId").equalTo(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> result = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Message message = child.getValue(Message.class);
                    if (message != null) {
                        result.add(message);
                    }
                }
                future.complete(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }

    public CompletableFuture<List<Message>> getAllMessage() {
        CompletableFuture<List<Message>> future = new CompletableFuture<>();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> result = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Message message = child.getValue(Message.class);
                    if (message != null) {
                        result.add(message);
                    }

                }
                future.complete(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });
        return future;
    }

    //load message by senderId and receiverId
//    public CompletableFuture<List<ChatMessage>> load
    public static void main(String[] args) {
        try {
            FileInputStream serviceAccount = new FileInputStream("D://DangTranTanLuc//modis-8f5f6-firebase-adminsdk-fbsvc-f76bd29f1f.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://modis-8f5f6-default-rtdb.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            MessageDao messageDao = new MessageDao(FirebaseDatabase.getInstance());
            CompletableFuture<List<Message>> future = messageDao.getAllMessage();
            List<Message> messages = future.join();
            if (!messages.isEmpty()) {
                for (Message message : messages) {
                    System.out.println(message);
                }
            } else {
                System.out.println("No messages found for userId");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
