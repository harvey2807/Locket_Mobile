package vn.edu.hcumuaf.locket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcumuaf.locket.dto.FriendListResponse;
import vn.edu.hcumuaf.locket.dto.SearchUserRequest;
import vn.edu.hcumuaf.locket.dto.FriendRequestDto;
import vn.edu.hcumuaf.locket.dto.ShareRequest;
import vn.edu.hcumuaf.locket.dto.ImportRequest;
import vn.edu.hcumuaf.locket.model.entity.FriendRequest;
import vn.edu.hcumuaf.locket.model.entity.User;
import vn.edu.hcumuaf.locket.service.FriendListService;
import vn.edu.hcumuaf.locket.responsitory.UserDao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/friend-list")
@CrossOrigin(origins = "*")
public class FriendListController {

    private final FriendListService friendListService;

    private final UserDao userDao;

    public FriendListController(FriendListService friendListService, UserDao userDao) {
        this.friendListService = friendListService;
        this.userDao = userDao;
    }

    @GetMapping("/friends/{userId}")
    public CompletableFuture<ResponseEntity<FriendListResponse>> getFriendList(@PathVariable String userId) {
        return friendListService.getFriendList(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/search")
    public CompletableFuture<ResponseEntity<List<User>>> searchUsers(@RequestBody SearchUserRequest request) {
        return friendListService.searchUsers(request.getQuery(), request.getUserId())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/send-request")
    public CompletableFuture<ResponseEntity<String>> sendFriendRequest(@RequestBody FriendRequestDto request) {
        return friendListService.sendFriendRequest(request.getSenderId(), request.getReceiverId())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to send friend request: " + ex.getMessage()));
    }

    @PutMapping("/accept-request/{requestId}")
    public CompletableFuture<ResponseEntity<String>> acceptFriendRequest(@PathVariable String requestId) {
        return friendListService.acceptFriendRequest(requestId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to accept friend request: " + ex.getMessage()));
    }

    @PutMapping("/reject-request/{requestId}")
    public CompletableFuture<ResponseEntity<String>> rejectFriendRequest(@PathVariable String requestId) {
        return friendListService.rejectFriendRequest(requestId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to reject friend request: " + ex.getMessage()));
    }

    @DeleteMapping("/remove-friend/{userId}/{friendId}")
    public CompletableFuture<ResponseEntity<String>> removeFriend(@PathVariable String userId, @PathVariable String friendId) {
        return friendListService.removeFriend(userId, friendId)
                .thenApply(result -> ResponseEntity.ok("Friend removed successfully"))
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to remove friend: " + ex.getMessage()));
    }

    @GetMapping("/suggestions/{userId}")
    public CompletableFuture<ResponseEntity<List<User>>> getFriendSuggestions(@PathVariable String userId) {
        return friendListService.getFriendSuggestions(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/pending-requests/{userId}")
    public CompletableFuture<ResponseEntity<List<FriendRequest>>> getPendingRequests(@PathVariable String userId) {
        return friendListService.getPendingRequests(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/sent-requests/{userId}")
    public CompletableFuture<ResponseEntity<List<FriendRequest>>> getSentRequests(@PathVariable String userId) {
        return friendListService.getSentRequests(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/share/{platform}")
    public CompletableFuture<ResponseEntity<String>> shareToSocialMedia(@PathVariable String platform, @RequestBody ShareRequest request) {
        return friendListService.shareToSocialMedia(platform, request.getUserId(), request.getMessage())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().body("Failed to share: " + ex.getMessage()));
    }

    @PostMapping("/import/{platform}")
    public CompletableFuture<ResponseEntity<List<User>>> importFriendsFromPlatform(@PathVariable String platform, @RequestBody ImportRequest request) {
        return friendListService.importFriendsFromPlatform(platform, request.getUserId(), request.getContacts())
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.badRequest().build());
    }

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

    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<User>> getUserById(@PathVariable String userId) {
        return userDao.findUserById(userId)
                .thenApply(user -> {
                    if (user != null) {
                        return ResponseEntity.ok(user);
                    } else {
                        return ResponseEntity.notFound().<User>build();
                    }
                })
                .exceptionally(ex -> ResponseEntity.badRequest().<User>build());
    }

    @GetMapping("/request-status/{userId1}/{userId2}")
    public CompletableFuture<ResponseEntity<FriendRequest>> getFriendRequestStatus(@PathVariable String userId1, @PathVariable String userId2) {
        return friendListService.getFriendRequestStatus(userId1, userId2)
                .thenApply(request -> {
                    if (request != null) {
                        return ResponseEntity.ok(request);
                    } else {
                        return ResponseEntity.notFound().<FriendRequest>build();
                    }
                })
                .exceptionally(ex -> ResponseEntity.badRequest().<FriendRequest>build());
    }

    @PutMapping("/cancel-request/{requestId}")
    public CompletableFuture<ResponseEntity<String>> cancelFriendRequest(@PathVariable String requestId) {
        return friendListService.cancelFriendRequest(requestId)
                .thenApply(result -> {
                    System.out.println("Successfully cancelled request: " + requestId);
                    return ResponseEntity.ok(result);
                })
                .exceptionally(ex -> {
                    System.out.println("Error cancelling request " + requestId + ": " + ex.getMessage());
                    return ResponseEntity.badRequest().body("Failed to cancel friend request: " + ex.getMessage());
                });
    }
}