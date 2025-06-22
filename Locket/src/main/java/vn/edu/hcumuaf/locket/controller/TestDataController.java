package vn.edu.hcumuaf.locket.controller;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcumuaf.locket.model.entity.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/test-data")
public class TestDataController {

    @Autowired
    private FirebaseDatabase firebaseDatabase;

    /**
     * API test đơn giản để kiểm tra kết nối
     */
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Backend is running! Current time: " + System.currentTimeMillis());
    }

    /**
     * Thêm dữ liệu test cho user camt91990@gmail.com với 3 bạn bè
     * SỬA: Chỉ thêm từng phần tử vào đúng node, không ghi đè toàn bộ node
     */
    @PostMapping("/add-friends-for-camt")
    public CompletableFuture<ResponseEntity<String>> addFriendsForCamt() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
        try {
            DatabaseReference dbRef = firebaseDatabase.getReference();
            long currentTime = System.currentTimeMillis();

            // ===== 1. THÊM USER CAMT91990 VÀ 3 BẠN BÈ =====
            User camtUser = new User(
                "camt91990", "camt91990", "Camt User", "camt91990@gmail.com", "0123456789", "https://www.meliawedding.com.vn/wp-content/uploads/2022/03/avatar-gai-xinh-28.jpg", "1234567890CTu@"
            );
            User friend1 = new User(
                "friend1", "ngocdiem", "Ngọc Diễm", "ngocdiem@gmail.com", "0379891777", "https://example.com/avatars/ngocdiem.jpg", "123"
            );
            User tanluc = new User(
                "JsnmEU9dB0MKXXNzTzWL8gDZWJs1", "tanluc", "Đặng Trần Tấn Lực", "dttanluc2004@gmail.com", "0987654321", "https://cellphones.com.vn/sforum/wp-content/uploads/2024/01/avartar-anime-6.jpg", "123456"
            );
            User friend3 = new User(
                "friend3", "linhphan", "Phan Linh", "linh@gmail.com", "0909123456", "https://example.com/avatars/linh.jpg", "123"
            );
            User luc = new User(
                "luc", "luc", "Lực đẹp trai", "luc@gmail.com", "0909123456", "https://img.tripi.vn/cdn-cgi/image/width=700,height=700/https://gcs.tripi.vn/public-tripi/tripi-feed/img/482740ReE/anh-mo-ta.png", "123456"
            );

            Map<String, Object> dataToAdd = new HashMap<>();
            // Thêm từng user
            dataToAdd.put("users/camt91990", camtUser);
            dataToAdd.put("users/friend1", friend1);
            dataToAdd.put("users/JsnmEU9dB0MKXXNzTzWL8gDZWJs1", tanluc);
            dataToAdd.put("users/friend3", friend3);
            dataToAdd.put("users/luc", luc);

            // ===== 2. THÊM FRIEND REQUESTS (req1, req2 đã accept, req3 pending) =====
            dataToAdd.put("friendRequests/req1", createFriendRequest("camt91990", "friend1", "ACCEPTED", currentTime - 100000));
            dataToAdd.put("friendRequests/req2", createFriendRequest("camt91990", "friend2", "ACCEPTED", currentTime - 200000));
            dataToAdd.put("friendRequests/req3", createFriendRequest("friend3", "camt91990", "PENDING", currentTime - 300000));

            // ===== 3. THÊM IMAGES =====
            dataToAdd.put("images/img1", createImageData("img1", "https://example.com/photo1.jpg", "Buổi chiều đẹp 🌇", currentTime - 50000, "camt91990", Arrays.asList("friend1", "friend2", "friend3")));
            dataToAdd.put("images/img2", createImageData("img2", "https://example.com/photo2.jpg", "Chụp ảnh cùng bạn bè 📸", currentTime - 100000, "friend1", Arrays.asList("camt91990")));

            // ===== 4. THÊM MESSAGES =====
            dataToAdd.put("messages/camt91990_friend1/msg1", createMessageData("msg1", "Hello! Chào bạn 😄", currentTime - 150000, "camt91990", "friend1"));
            dataToAdd.put("messages/camt91990_friend1/msg2", createMessageData("msg2", "Chào bạn! Khỏe không? 😊", currentTime - 140000, "friend1", "camt91990"));

            // ===== 5. THÊM REACTIONS =====
            dataToAdd.put("reactions/img1/friend1", createReactionData("friend1", "img1", "❤️", currentTime - 40000));
            dataToAdd.put("reactions/img1/friend2", createReactionData("friend2", "img1", "👍", currentTime - 30000));

            // ===== 6. GHI DỮ LIỆU LÊN FIREBASE (KHÔNG GHI ĐÈ TOÀN NODE) =====
            dbRef.updateChildren(dataToAdd, (error, ref) -> {
                if (error == null) {
                    future.complete(ResponseEntity.ok("Đã thêm thành công dữ liệu test cho camt91990@gmail.com với 3 bạn bè!"));
                } else {
                    future.complete(ResponseEntity.badRequest().body("Lỗi khi thêm dữ liệu: " + error.getMessage()));
                }
            });
        } catch (Exception e) {
            future.complete(ResponseEntity.badRequest().body("Lỗi: " + e.getMessage()));
        }
        return future;
    }
    
    /**
     * Tạo dữ liệu friend request theo cấu trúc yêu cầu  
     */
    private Map<String, Object> createFriendRequest(String senderId, String receiverId, String status, long timestamp) {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("senderId", senderId);
        requestData.put("receiverId", receiverId);
        requestData.put("status", status);
        requestData.put("timestamp", timestamp);
        return requestData;
    }
    
    /**
     * Tạo dữ liệu image theo cấu trúc yêu cầu
     */
    private Map<String, Object> createImageData(String imageId, String urlImage, String caption, long timestamp, String senderId, java.util.List<String> receiverIds) {
        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageId", imageId);
        imageData.put("urlImage", urlImage);
        imageData.put("caption", caption);
        imageData.put("timestamp", timestamp);
        imageData.put("senderId", senderId);
        imageData.put("receiverIds", receiverIds);
        return imageData;
    }
    
    /**
     * Tạo dữ liệu message theo cấu trúc yêu cầu
     */
    private Map<String, Object> createMessageData(String messageId, String content, long timestamp, String senderId, String receiverId) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", messageId);
        messageData.put("content", content);
        messageData.put("timestamp", timestamp);
        messageData.put("senderId", senderId);
        messageData.put("receiverId", receiverId);
        messageData.put("type", "text");
        return messageData;
    }
    
    private Map<String, Object> createReactionData(String userId, String imageId, String icon, long timestamp) {
        Map<String, Object> reactionData = new HashMap<>();
        reactionData.put("userId", userId);
        reactionData.put("imageId", imageId);
        reactionData.put("icon", icon);
        reactionData.put("timestamp", timestamp);
        return reactionData;
    }

    @PostMapping("/add-pending-request-for-camt")
    public CompletableFuture<ResponseEntity<String>> addPendingRequestForCamt() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
        
        try {
            DatabaseReference dbRef = firebaseDatabase.getReference();
            long currentTime = System.currentTimeMillis();
            
            User newUser = new User(
                "newuser123", // userId
                "newuser123", // userName  
                "Nguyễn Văn A", // fullName
                "newuser123@gmail.com", // email
                "0123456789", // phoneNumber
                "https://example.com/avatars/newuser.jpg", // urlAvatar
                "123456" // password
            );
            
            Map<String, Object> dataToAdd = new HashMap<>();
            
            // Thêm user mới vào users node - SỬA: sử dụng đường dẫn cụ thể để không ghi đè
            dataToAdd.put("users/newuser123", newUser);
            
            // Thêm lời mời kết bạn với status "pending" - SỬA: sử dụng đường dẫn cụ thể
            dataToAdd.put("friendRequests/req4", createFriendRequest("newuser123", "camt91990", "PENDING", currentTime));
            
            dbRef.updateChildren(dataToAdd, (error, ref) -> {
                if (error == null) {
                    future.complete(ResponseEntity.ok("Đã thêm thành công user Lam đang gửi lời mời kết bạn cho camt91990!"));
                } else {
                    future.complete(ResponseEntity.badRequest().body("Lỗi khi thêm dữ liệu: " + error.getMessage()));
                }
            });
            
        } catch (Exception e) {
            future.complete(ResponseEntity.badRequest().body("Lỗi: " + e.getMessage()));
        }
        
        return future;
    }

    @PostMapping("/modify-req3-to-pending")
    public CompletableFuture<ResponseEntity<String>> modifyReq3ToPending() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
        
        try {
            DatabaseReference dbRef = firebaseDatabase.getReference();
            
            // Sửa status của req3 từ ACCEPTED thành PENDING
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("friendRequests/req3/status", "PENDING");
            
            dbRef.updateChildren(updateData, (error, ref) -> {
                if (error == null) {
                    future.complete(ResponseEntity.ok("Đã sửa req3 thành PENDING thành công!"));
                } else {
                    future.complete(ResponseEntity.badRequest().body("Lỗi khi sửa dữ liệu: " + error.getMessage()));
                }
            });
            
        } catch (Exception e) {
            future.complete(ResponseEntity.badRequest().body("Lỗi: " + e.getMessage()));
        }
        
        return future;
    }

    @PostMapping("/add-multiple-requests")
    public CompletableFuture<ResponseEntity<String>> addMultipleRequests() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
        
        try {
            DatabaseReference dbRef = firebaseDatabase.getReference();
            long currentTime = System.currentTimeMillis();
            
            // ===== 1. THÊM CÁC USER MỚI =====
            User user4 = new User(
                "user4", "user4", "Trần Thị B", "user4@gmail.com", "0123456780", "https://example.com/avatars/user4.jpg", "123456"
            );
            User user5 = new User(
                "user5", "user5", "Lê Văn C", "user5@gmail.com", "0123456781", "https://example.com/avatars/user5.jpg", "123456"
            );
            User user6 = new User(
                "user6", "user6", "Phạm Thị D", "user6@gmail.com", "0123456782", "https://example.com/avatars/user6.jpg", "123456"
            );
            User user7 = new User(
                "user7", "user7", "Hoàng Văn E", "user7@gmail.com", "0123456783", "https://example.com/avatars/user7.jpg", "123456"
            );
            
            Map<String, Object> dataToAdd = new HashMap<>();
            
            // Thêm các user mới
            dataToAdd.put("users/user4", user4);
            dataToAdd.put("users/user5", user5);
            dataToAdd.put("users/user6", user6);
            dataToAdd.put("users/user7", user7);
            
            // req1: camt91990 -> friend1 (ACCEPTED)
            dataToAdd.put("friendRequests/req1", createFriendRequest("camt91990", "friend1", "ACCEPTED", currentTime - 100000));
            
            // req2: camt91990 -> friend2 (ACCEPTED)  
            dataToAdd.put("friendRequests/req2", createFriendRequest("camt91990", "friend2", "ACCEPTED", currentTime - 200000));
            
            // req3: friend3 -> camt91990 (PENDING) - lời mời đang chờ
            dataToAdd.put("friendRequests/req3", createFriendRequest("friend3", "camt91990", "PENDING", currentTime - 300000));
            
            // req4: user4 -> camt91990 (PENDING) - lời mời mới
            dataToAdd.put("friendRequests/req4", createFriendRequest("user4", "camt91990", "PENDING", currentTime - 400000));
            
            // req5: user5 -> camt91990 (PENDING) - lời mời mới
            dataToAdd.put("friendRequests/req5", createFriendRequest("user5", "camt91990", "PENDING", currentTime - 500000));
            
            // req6: user6 -> camt91990 (PENDING) - lời mời mới
            dataToAdd.put("friendRequests/req6", createFriendRequest("user6", "camt91990", "PENDING", currentTime - 600000));
            
            // req7: user7 -> camt91990 (PENDING) - lời mời mới
            dataToAdd.put("friendRequests/req7", createFriendRequest("user7", "camt91990", "PENDING", currentTime - 700000));
            
            // req8: camt91990 -> user4 (PENDING) - camt gửi lời mời
            dataToAdd.put("friendRequests/req8", createFriendRequest("camt91990", "user4", "PENDING", currentTime - 800000));
            
            // req9: friend1 -> user5 (PENDING) - friend1 gửi lời mời
            dataToAdd.put("friendRequests/req9", createFriendRequest("friend1", "user5", "PENDING", currentTime - 900000));
            
            // req10: friend2 -> user6 (PENDING) - friend2 gửi lời mời
            dataToAdd.put("friendRequests/req10", createFriendRequest("friend2", "user6", "PENDING", currentTime - 1000000));
            
            dbRef.updateChildren(dataToAdd, (error, ref) -> {
                if (error == null) {
                    future.complete(ResponseEntity.ok("Đã thêm thành công 10 friend requests! " +
                        "Bao gồm: req1, req2 (ACCEPTED), req3-req10 (PENDING)"));
                } else {
                    future.complete(ResponseEntity.badRequest().body("Lỗi khi thêm dữ liệu: " + error.getMessage()));
                }
            });
            
        } catch (Exception e) {
            future.complete(ResponseEntity.badRequest().body("Lỗi: " + e.getMessage()));
        }
        
        return future;
    }

    @PostMapping("/add-simple-requests")
    public CompletableFuture<ResponseEntity<String>> addSimpleRequests() {
        CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();
        
        try {
            DatabaseReference dbRef = firebaseDatabase.getReference();
            long currentTime = System.currentTimeMillis();
            
            Map<String, Object> dataToAdd = new HashMap<>();
            
            // Thêm lại req1 và req2 (ACCEPTED)
            dataToAdd.put("friendRequests/req1", createFriendRequest("camt91990", "friend1", "ACCEPTED", currentTime - 100000));
            dataToAdd.put("friendRequests/req2", createFriendRequest("camt91990", "friend2", "ACCEPTED", currentTime - 200000));
            
            // Thêm req3 (PENDING) - friend3 gửi lời mời cho camt91990
            dataToAdd.put("friendRequests/req3", createFriendRequest("friend3", "camt91990", "PENDING", currentTime - 300000));
            
            dbRef.updateChildren(dataToAdd, (error, ref) -> {
                if (error == null) {
                    future.complete(ResponseEntity.ok("Đã thêm thành công req1, req2 (ACCEPTED) và req3 (PENDING) cho camt91990!"));
                } else {
                    future.complete(ResponseEntity.badRequest().body("Lỗi khi thêm dữ liệu: " + error.getMessage()));
                }
            });
            
        } catch (Exception e) {
            future.complete(ResponseEntity.badRequest().body("Lỗi: " + e.getMessage()));
        }
        
        return future;
    }
} 