package vn.edu.hcumuaf.locket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcumuaf.locket.dto.FriendListResponse;
import vn.edu.hcumuaf.locket.dto.SearchUserRequest;
import vn.edu.hcumuaf.locket.dto.FriendRequestDto;
import vn.edu.hcumuaf.locket.dto.ShareRequest;
import vn.edu.hcumuaf.locket.dto.ImportRequest;
import vn.edu.hcumuaf.locket.model.entity.User;
import vn.edu.hcumuaf.locket.model.FriendRequest;
import vn.edu.hcumuaf.locket.service.FriendListService;
import vn.edu.hcumuaf.locket.responsitory.UserDao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/friend-list")
@CrossOrigin(origins = "*")
public class FriendListController {

    // Service để xử lý business logic
    @Autowired
    private FriendListService friendListService;
    
    // DAO để truy cập dữ liệu User
    @Autowired
    private UserDao userDao;

    /**
     * Lấy danh sách bạn bè của user
     * 
     * @param userId ID của user cần lấy danh sách bạn bè
     * @return CompletableFuture<ResponseEntity<FriendListResponse>> - Danh sách bạn bè và gợi ý
     */
    @GetMapping("/friends/{userId}")
    public CompletableFuture<ResponseEntity<FriendListResponse>> getFriendList(@PathVariable String userId) {
        return friendListService.getFriendList(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    /**
     * Tìm kiếm bạn bè theo tên hoặc username
     * 
     * @param request SearchUserRequest chứa query và currentUserId
     * @return CompletableFuture<ResponseEntity<List<User>>> - Danh sách users phù hợp
     */
    @PostMapping("/search")
    public CompletableFuture<ResponseEntity<List<User>>> searchUsers(@RequestBody SearchUserRequest request) {
        return friendListService.searchUsers(request.getQuery(), request.getUserId())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    /**
     * Gửi lời mời kết bạn
     * 
     * @param request FriendRequestDto chứa senderId và receiverId
     * @return CompletableFuture<ResponseEntity<String>> - Kết quả gửi lời mời
     */
    @PostMapping("/send-request")
    public CompletableFuture<ResponseEntity<String>> sendFriendRequest(@RequestBody FriendRequestDto request) {
        return friendListService.sendFriendRequest(request.getSenderId(), request.getReceiverId())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to send friend request: " + ex.getMessage()));
    }

    /**
     * Chấp nhận lời mời kết bạn
     * 
     * @param requestId ID của lời mời cần chấp nhận
     * @return CompletableFuture<ResponseEntity<String>> - Kết quả chấp nhận
     */
    @PutMapping("/accept-request/{requestId}")
    public CompletableFuture<ResponseEntity<String>> acceptFriendRequest(@PathVariable String requestId) {
        return friendListService.acceptFriendRequest(requestId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to accept friend request: " + ex.getMessage()));
    }

    /**
     * Từ chối lời mời kết bạn
     * 
     * @param requestId ID của lời mời cần từ chối
     * @return CompletableFuture<ResponseEntity<String>> - Kết quả từ chối
     */
    @PutMapping("/reject-request/{requestId}")
    public CompletableFuture<ResponseEntity<String>> rejectFriendRequest(@PathVariable String requestId) {
        return friendListService.rejectFriendRequest(requestId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to reject friend request: " + ex.getMessage()));
    }

    /**
     * Xóa bạn bè
     * 
     * @param userId ID của user thực hiện xóa
     * @param friendId ID của bạn bè cần xóa
     * @return CompletableFuture<ResponseEntity<String>> - Kết quả xóa bạn bè
     */
    @DeleteMapping("/remove-friend/{userId}/{friendId}")
    public CompletableFuture<ResponseEntity<String>> removeFriend(@PathVariable String userId, @PathVariable String friendId) {
        return friendListService.removeFriend(userId, friendId)
                .thenApply(result -> ResponseEntity.ok("Friend removed successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to remove friend: " + ex.getMessage()));
    }

    /**
     * Lấy danh sách gợi ý bạn bè
     * 
     * @param userId ID của user cần lấy gợi ý
     * @return CompletableFuture<ResponseEntity<List<User>>> - Danh sách gợi ý
     */
    @GetMapping("/suggestions/{userId}")
    public CompletableFuture<ResponseEntity<List<User>>> getFriendSuggestions(@PathVariable String userId) {
        return friendListService.getFriendSuggestions(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    /**
     * Lấy danh sách lời mời kết bạn đang chờ
     * 
     * @param userId ID của user cần lấy lời mời
     * @return CompletableFuture<ResponseEntity<List<FriendRequest>>> - Danh sách lời mời
     */
    @GetMapping("/pending-requests/{userId}")
    public CompletableFuture<ResponseEntity<List<FriendRequest>>> getPendingRequests(@PathVariable String userId) {
        return friendListService.getPendingRequests(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    /**
     * Chia sẻ qua social media
     * 
     * @param platform Platform để share (facebook, instagram, etc.)
     * @param request ShareRequest chứa userId và message
     * @return CompletableFuture<ResponseEntity<String>> - Kết quả share
     */
    @PostMapping("/share/{platform}")
    public CompletableFuture<ResponseEntity<String>> shareToSocialMedia(@PathVariable String platform, @RequestBody ShareRequest request) {
        return friendListService.shareToSocialMedia(platform, request.getUserId(), request.getMessage())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to share: " + ex.getMessage()));
    }

    /**
     * Import bạn bè từ ứng dụng khác
     * 
     * @param platform Platform để import (facebook, contacts, etc.)
     * @param request ImportRequest chứa userId và danh sách contacts
     * @return CompletableFuture<ResponseEntity<List<User>>> - Danh sách users có thể thêm
     */
    @PostMapping("/import/{platform}")
    public CompletableFuture<ResponseEntity<List<User>>> importFriendsFromPlatform(@PathVariable String platform, @RequestBody ImportRequest request) {
        return friendListService.importFriendsFromPlatform(platform, request.getUserId(), request.getContacts())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }
    
    /**
     * Lấy user theo email
     * THÊM MỚI: Để lấy data user với email camt91990@gmail.com
     * 
     * @param email Email của user cần tìm
     * @return CompletableFuture<ResponseEntity<User>> - User object hoặc 404 nếu không tìm thấy
     */
    @GetMapping("/user/email/{email}")
    public CompletableFuture<ResponseEntity<User>> getUserByEmail(@PathVariable String email) {
        return userDao.findUserByEmail(email)
                .thenApply(user -> {
                    if (user != null) {
                        return ResponseEntity.ok(user);
                    } else {
                        return ResponseEntity.notFound().<User>build();
                    }
                })
                .exceptionally(ex -> ResponseEntity.badRequest().<User>build());
    }
    
    /**
     * Lấy user ID từ email
     * THÊM MỚI: Để app có thể lấy đúng user ID từ email đăng nhập
     * 
     * @param email Email của user cần tìm
     * @return CompletableFuture<ResponseEntity<String>> - User ID hoặc 404 nếu không tìm thấy
     */
    @GetMapping("/user-id/email/{email}")
    public CompletableFuture<ResponseEntity<String>> getUserIdByEmail(@PathVariable String email) {
        return userDao.findUserByEmail(email)
                .thenApply(user -> {
                    if (user != null) {
                        return ResponseEntity.ok(user.getUserId());
                    } else {
                        return ResponseEntity.notFound().<String>build();
                    }
                })
                .exceptionally(ex -> ResponseEntity.badRequest().<String>build());
    }
} 