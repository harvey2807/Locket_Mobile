package vn.edu.hcumuaf.locket.responsitory;

import com.google.firebase.database.*;
import com.google.firebase.internal.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hcumuaf.locket.model.entity.Reaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository

public class ReactionDao {
    private final DatabaseReference dbRef;

    @Autowired
    public ReactionDao(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference("reactions");
    }

    //Thêm reactions
    public CompletableFuture<Void> addReaction(Reaction reaction) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        dbRef.child(reaction.getImageId())
                .child(reaction.getUserId())
                .setValue(reaction, (error, ref) -> {
                    if (error != null) {
                        future.completeExceptionally(new RuntimeException("Lỗi them reaction" + error.getMessage()));
                    } else {
                        future.complete(null);
                    }
                });
        return future;
    }

    //Lay danh sach user da gui emoji cho nguoi dung
    public CompletableFuture<List<String>> getFriendSendReact(String imageId) {
        CompletableFuture<List<String>> future = new CompletableFuture<>();

        dbRef.child(imageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> result = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Reaction reaction = child.getValue(Reaction.class);
                    if (reaction != null && reaction.getUserId() != null) {
                        result.add(reaction.getUserId());
                    }
                }
                future.complete(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.completeExceptionally(new RuntimeException(error.getMessage()));
            }
        });
        return future;
    }
}
