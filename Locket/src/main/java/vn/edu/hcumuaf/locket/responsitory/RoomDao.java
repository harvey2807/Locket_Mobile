package vn.edu.hcumuaf.locket.responsitory;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hcumuaf.locket.model.entity.Rooms;

import java.util.*;

import java.util.concurrent.CompletableFuture;

@Repository
public class RoomDao {
    private final DatabaseReference dbRef;

    @Autowired
    public RoomDao(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference("rooms");
    }

    public CompletableFuture<List<Rooms>> getRomsByUserIdWithReceiverId(String userId, String receiverId) {
        CompletableFuture<List<Rooms>> future = new CompletableFuture<>();
        String compositeKey = userId + "_" + receiverId;
        dbRef.orderByChild("owner_receiver").equalTo(compositeKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Rooms> results = new ArrayList<>();
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Rooms room = child.getValue(Rooms.class);
                            if (room != null && room.getOwnerId().equals(userId) && room.getReceiverId().equals(receiverId)) {
                                results.add(room);
                            }
                        }
                        future.complete(results);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        future.completeExceptionally(databaseError.toException());
                    }
                });
        return future;
    }
}
