package vn.edu.hcumuaf.locket.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcumuaf.locket.model.entity.Image;
import vn.edu.hcumuaf.locket.responsitory.FriendRequestDao;
import vn.edu.hcumuaf.locket.responsitory.ImageDao;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ImageService {

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private FriendRequestDao friendRequestDao;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private AuthService authService;

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

    public CompletableFuture<Void> saveImage(Image image) {
        return imageDao.saveImage(image);
    }


    public ResponseEntity<?> uploadImage(String uid, String authHeader, MultipartFile file) {
        try {
            authService.verifyToken(uid, authHeader);
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File ảnh rỗng hoặc không tồn tại");
            }

            Map options = ObjectUtils.asMap("folder", "Modis");

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            Object secureUrl = uploadResult.get("secure_url");

            if (secureUrl == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload không thành công");
            }

            return ResponseEntity.ok(Map.of("url", secureUrl));

        } catch (IllegalArgumentException | SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Lỗi xác thực: " + e.getMessage());
//        } catch (FirebaseAuthException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Firebase lỗi: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload ảnh: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi không xác định: " + e.getMessage());
        }
    }

    //Xóa ảnh theo imageId
    public CompletableFuture<Void> deleteImageById(String imageId) {
        return imageDao.deleteImageById(imageId);
    }
}