package vn.edu.hcumuaf.locket.responsitory;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hcumuaf.locket.model.entity.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Repository
public class ImageDao {

    private final DatabaseReference dbRef;

    @Autowired
    public ImageDao(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference("images");
    }

    // Tìm ảnh theo senderId
    public CompletableFuture<List<Image>> findImagesBySenderId(String senderId) {
        CompletableFuture<List<Image>> future = new CompletableFuture<>();

        dbRef.orderByChild("senderId").equalTo(senderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Image> result = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Image image = snap.getValue(Image.class);
                            if (image != null) result.add(image);
                        }
                        future.complete(result);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }

    // Tìm ảnh theo senderId và receiverId
    public CompletableFuture<List<Image>> findImageBySenderIdAndReceiverId(String senderId, String receiverId) {
        CompletableFuture<List<Image>> future = new CompletableFuture<>();

        dbRef.orderByChild("senderId").equalTo(senderId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Image> result = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Image image = snap.getValue(Image.class);
                            if (image != null && image.getReceiverIds() != null &&
                                    image.getReceiverIds().contains(receiverId)) {
                                result.add(image);
                            }
                        }
                        future.complete(result);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }

    // Tìm ảnh theo imageId
    public CompletableFuture<Image> findImageById(String imageId) {
        CompletableFuture<Image> future = new CompletableFuture<>();

        dbRef.orderByChild("imageId").equalTo(imageId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Image image = snap.getValue(Image.class);
                            if (image != null) {
                                future.complete(image);
                                return;
                            }
                        }
                        future.complete(null); // không tìm thấy
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }

    // Lưu ảnh
    public CompletableFuture<Void> saveImage(Image image) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String key = dbRef.push().getKey();
        image.setImageId(key);

        dbRef.child(key).setValue(image, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new RuntimeException("Lỗi lưu ảnh: " + error.getMessage()));
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    // Xoá ảnh theo imageId
    public CompletableFuture<Void> deleteImageById(String imageId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        dbRef.orderByChild("imageId").equalTo(imageId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                child.getRef().removeValue((error, ref) -> {
                                    if (error != null) {
                                        future.completeExceptionally(
                                                new RuntimeException("Lỗi xóa ảnh: " + error.getMessage()));
                                    } else {
                                        future.complete(null);
                                    }
                                });
                                return;
                            }
                        } else {
                            future.completeExceptionally(
                                    new RuntimeException("Không tìm thấy ảnh với imageId: " + imageId));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }
}
