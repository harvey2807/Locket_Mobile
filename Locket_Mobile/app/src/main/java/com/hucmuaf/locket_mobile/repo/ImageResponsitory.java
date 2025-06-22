package com.hucmuaf.locket_mobile.repo;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.hucmuaf.locket_mobile.modedb.Image;
import com.hucmuaf.locket_mobile.service.FirebaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ImageResponsitory {
    private final DatabaseReference imagesRef;
    private ValueEventListener currentListener;
    public ImageResponsitory(FirebaseService firebaseService) {
        this.imagesRef = firebaseService.getDatabase().getReference("images");
    }

    //lấy toàn bộ ảnh của user và ảnh từ bạn bè gửi tới user
    public void getAllImagesByUserId(String userId, Set<String> friendIds, ImageLoadCallback callback) {
        if (currentListener != null) {
            imagesRef.removeEventListener(currentListener);
        }

        currentListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Image> result = new ArrayList<>();
                for (DataSnapshot snap: snapshot.getChildren()){
                    Image image = snap.getValue(Image.class);
                    if (image == null) continue;

                    boolean isSentByUser = userId.equals(image.getSenderId());
                    boolean isSentByFriend = friendIds.contains(image.getSenderId()) &&
                            image.getReceiverIds() != null && image.getReceiverIds().contains(userId);

                    if (isSentByUser || isSentByFriend)
                        result.add(image);

                }
                // Sắp xếp theo timestamp giảm dần
                Collections.sort(result, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                callback.onSuccess(result);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(new Exception(error.getMessage()));
            }
        };
        imagesRef.addValueEventListener(currentListener);
    }

    //Gỡ listener khi không cần nữa
    public void removeListener() {
        if (currentListener != null) {
            imagesRef.removeEventListener(currentListener);
            currentListener = null;
        }
    }
}
