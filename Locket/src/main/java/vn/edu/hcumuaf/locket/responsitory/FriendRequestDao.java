package vn.edu.hcumuaf.locket.responsitory;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hcumuaf.locket.model.FriendRequestStatus;
import vn.edu.hcumuaf.locket.model.entity.FriendRequest;


import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class FriendRequestDao {
    private final DatabaseReference dbRef;

    @Autowired
    public FriendRequestDao(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference("friendRequests");
    }

    //Tìm danh sách bạn bè theo user id
    public CompletableFuture<Set<String>> findListFriendByUserID(String userID) {
        CompletableFuture<Set<String>> senderFuture = new CompletableFuture<>();
        CompletableFuture<Set<String>> receiverFuture = new CompletableFuture<>();


        dbRef.orderByChild("senderId").equalTo(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Set<String> senderSet  = ConcurrentHashMap.newKeySet();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            FriendRequest fq = parseFriendRequest(snap);
                            if (fq != null && fq.getStatus().equals(FriendRequestStatus.ACCEPTED)) {
                                senderSet.add(fq.getReceiverId());
                            }
                        }
                        System.out.println("Sender query done");
                        senderFuture.complete(senderSet);

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Sender query failed: " + error.getMessage());
                        senderFuture.completeExceptionally(new RuntimeException(error.getMessage()));

                    }
                });
        dbRef.orderByChild("receiverId").equalTo(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Set<String> receiverSet  = ConcurrentHashMap.newKeySet();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            FriendRequest fq = parseFriendRequest(snap);
                            if (fq != null && fq.getStatus().equals(FriendRequestStatus.ACCEPTED)) {
                                receiverSet .add(fq.getSenderId());
                            }
                        }
                        System.out.println("Receiver query done");
                        receiverFuture.complete(receiverSet );

                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Receiver query failed: " + error.getMessage());
                        receiverFuture.completeExceptionally(new RuntimeException(error.getMessage()));

                    }
                });
        return senderFuture.thenCombine(receiverFuture, (senderSet, receiverSet) -> {
            senderSet.addAll(receiverSet);
            return senderSet;
        });
    }

    //Chuyển enum ở bean sang String khi parse từ DataSnapshot
    private FriendRequest parseFriendRequest(DataSnapshot snap) {
        try {
            String statusStr = snap.child("status").getValue(String.class);
            if (statusStr == null) return null;

            FriendRequestStatus status = FriendRequestStatus.valueOf(statusStr.toUpperCase());
            String friendRequestId = snap.child("friendRequestId").getValue(String.class);
            String senderId = snap.child("senderId").getValue(String.class);
            String receiverId = snap.child("receiverId").getValue(String.class);
            Long timestamp = snap.child("timestamp").getValue(Long.class);

            if (senderId == null || receiverId == null || timestamp == null) return null;

            return new FriendRequest(friendRequestId, senderId, receiverId, status, timestamp);
        } catch (Exception e) {
            System.err.println("Error parsing friend request: " + e.getMessage());
            return null;
        }
    }

}