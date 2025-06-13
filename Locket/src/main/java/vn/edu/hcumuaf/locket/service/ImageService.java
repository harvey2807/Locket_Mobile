package vn.edu.hcumuaf.locket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hcumuaf.locket.model.entity.Image;
import vn.edu.hcumuaf.locket.responsitory.FriendRequestDao;
import vn.edu.hcumuaf.locket.responsitory.ImageDao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private FriendRequestDao friendRequestDao;

    // Trả về ảnh do user gửi
    public CompletableFuture<List<Image>> getImagesByUserId(String userId) {
        return imageDao.findImagesBySenderId(userId)
                .thenApply(images -> {
                    images.sort(Comparator.comparingLong(Image::getTimestamp).reversed());
                    return images;
                });
    }

    // Trả về ảnh gửi riêng giữa 2 người
    public CompletableFuture<List<Image>> getImagesBySenderIdAndReceiverId(String senderId, String receiverId) {
        return imageDao.findImageBySenderIdAndReceiverId(senderId, receiverId)
                .thenApply(images -> {
                    images.sort(Comparator.comparingLong(Image::getTimestamp).reversed());
                    return images;
                });
    }

    // Trả về ảnh theo id
    public CompletableFuture<Image> getImageById(String imageId) {
        return imageDao.findImageById(imageId);
    }

    // Trả về toàn bộ ảnh của user (gửi và nhận từ bạn bè)
    public CompletableFuture<List<Image>> getAllImagesByUserId(String userId) {
        // 1. Lấy danh sách bạn bè
        return friendRequestDao.findListFriendByUserID(userId)
                .thenCompose(friendIds -> {
                    List<CompletableFuture<List<Image>>> friendImageFutures = friendIds.stream()
                            .map(friendId -> getImagesBySenderIdAndReceiverId(friendId, userId))
                            .collect(Collectors.toList());

                    // 2. Thêm ảnh do chính user gửi
                    friendImageFutures.add(getImagesByUserId(userId));

                    // 3. Khi tất cả ảnh đã load xong
                    return CompletableFuture.allOf(friendImageFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> friendImageFutures.stream()
                                    .flatMap(future -> future.join().stream())
                                    .sorted(Comparator.comparingLong(Image::getTimestamp).reversed())
                                    .collect(Collectors.toList()));
                });
    }
}