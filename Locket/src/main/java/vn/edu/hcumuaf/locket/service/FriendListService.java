package vn.edu.hcumuaf.locket.service;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hcumuaf.locket.model.entity.User;
import vn.edu.hcumuaf.locket.model.FriendRequest;
import vn.edu.hcumuaf.locket.dto.FriendListResponse;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FriendListService {

    @Autowired
    private FirebaseDatabase firebaseDatabase;

    private static final int MAX_FRIENDS = 20;

    public CompletableFuture<FriendListResponse> getFriendList(String userId) {
        CompletableFuture<FriendListResponse> future = new CompletableFuture<>();
        
        System.out.println("=== DEBUG: Getting friend list for userId: " + userId + " ===");
        
        try {
            // Lấy danh sách bạn bè từ friendRequests với status ACCEPTED
            getAcceptedFriendsFromFirebase(userId).thenCompose(friends -> {
                System.out.println("=== DEBUG: Found " + friends.size() + " accepted friends for user " + userId + " ===");
                return getFriendSuggestions(userId).thenApply(suggestions -> {
                    System.out.println("=== DEBUG: Found " + suggestions.size() + " suggestions for user " + userId + " ===");
                    return new FriendListResponse(friends, friends.size(), MAX_FRIENDS, suggestions);
                });
            }).thenAccept(future::complete)
            .exceptionally(ex -> {
                System.out.println("=== DEBUG: Error getting friend list: " + ex.getMessage() + " ===");
                future.completeExceptionally(ex);
                return null;
            });
        } catch (Exception e) {
            System.out.println("=== DEBUG: Exception in getFriendList: " + e.getMessage() + " ===");
            future.completeExceptionally(new RuntimeException("Failed to get friend list: " + e.getMessage()));
        }
        
        return future;
    }

    public CompletableFuture<List<User>> searchUsers(String query, String currentUserId) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        
        try {
            DatabaseReference usersRef = firebaseDatabase.getReference("users");
            List<User> users = new ArrayList<>();
            
            // Search users by name or username
            usersRef.orderByChild("userName").startAt(query).endAt(query + "\uf8ff")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                User user = child.getValue(User.class);
                                if (user != null && !user.getUserId().equals(currentUserId)) {
                                    users.add(user);
                                }
                            }
                            future.complete(users);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            future.completeExceptionally(new RuntimeException("Error searching users: " + error.getMessage()));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to search users: " + e.getMessage()));
        }
        
        return future;
    }

    public CompletableFuture<String> sendFriendRequest(String senderId, String receiverId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            if (senderId.equals(receiverId)) {
                future.complete("Cannot send friend request to yourself");
                return future;
            }

            DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests").push();
            String requestId = requestsRef.getKey();
            
            Map<String, Object> request = new HashMap<>();
            request.put("senderId", senderId);
            request.put("receiverId", receiverId);
            request.put("status", "PENDING");
            request.put("timestamp", System.currentTimeMillis());
            
            requestsRef.setValue(request, (error, ref) -> {
                if (error != null) {
                    future.completeExceptionally(new RuntimeException("Failed to save friend request: " + error.getMessage()));
                } else {
                    future.complete("Friend request sent successfully");
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to send friend request: " + e.getMessage()));
        }
        
        return future;
    }

    public CompletableFuture<String> acceptFriendRequest(String requestId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            DatabaseReference requestRef = firebaseDatabase.getReference("friendRequests").child(requestId);
            
            // Update request status to ACCEPTED
            requestRef.child("status").setValue("ACCEPTED", (error, ref) -> {
                if (error != null) {
                    future.completeExceptionally(new RuntimeException("Failed to update friend request: " + error.getMessage()));
                } else {
                    future.complete("Friend request accepted");
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to accept friend request: " + e.getMessage()));
        }
        
        return future;
    }

    public CompletableFuture<String> rejectFriendRequest(String requestId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        try {
            DatabaseReference requestRef = firebaseDatabase.getReference("friendRequests").child(requestId);
            requestRef.child("status").setValue("REJECTED", (error, ref) -> {
                if (error != null) {
                    future.completeExceptionally(new RuntimeException("Failed to reject friend request: " + error.getMessage()));
                } else {
                    future.complete("Friend request rejected");
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to reject friend request: " + e.getMessage()));
        }
        
        return future;
    }

    public CompletableFuture<String> removeFriend(String userId, String friendId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            // Find and remove friend requests between these two users
            DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests");
            
            // Query for requests where either user is sender or receiver
            requestsRef.orderByChild("senderId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String receiverId = child.child("receiverId").getValue(String.class);
                                if (friendId.equals(receiverId)) {
                                    child.getRef().removeValue((error, ref) -> {
                                        if (error != null) {
                                            System.out.println("=== DEBUG: Error removing friend request: " + error.getMessage() + " ===");
                                        }
                                    });
                                }
                            }
                            
                            // Also check the reverse direction
                            requestsRef.orderByChild("senderId").equalTo(friendId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot2) {
                                            for (DataSnapshot child : snapshot2.getChildren()) {
                                                String receiverId = child.child("receiverId").getValue(String.class);
                                                if (userId.equals(receiverId)) {
                                                    child.getRef().removeValue((error, ref) -> {
                                                        if (error != null) {
                                                            System.out.println("=== DEBUG: Error removing friend request: " + error.getMessage() + " ===");
                                                        }
                                                    });
                                                }
                                            }
                                            future.complete("Friend removed successfully");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            future.completeExceptionally(new RuntimeException("Failed to remove friend: " + error.getMessage()));
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            future.completeExceptionally(new RuntimeException("Failed to remove friend: " + error.getMessage()));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to remove friend: " + e.getMessage()));
        }
        return future;
    }

    public CompletableFuture<List<User>> getFriendSuggestions(String userId) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        
        try {
            // Get friends of friends from accepted friend requests
            getAcceptedFriendsFromFirebase(userId).thenCompose(friends -> {
                Set<String> suggestedIds = new HashSet<>();
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                
                for (User friend : friends) {
                    CompletableFuture<Void> friendFuture = getAcceptedFriendsFromFirebase(friend.getUserId())
                            .thenAccept(friendsOfFriend -> {
                                for (User friendOfFriend : friendsOfFriend) {
                                    if (!friendOfFriend.getUserId().equals(userId)) {
                                        suggestedIds.add(friendOfFriend.getUserId());
                                    }
                                }
                            });
                    futures.add(friendFuture);
                }
                
                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> suggestedIds);
            }).thenCompose(suggestedIds -> {
                // Remove users who are already friends
                return getAcceptedFriendsFromFirebase(userId).thenApply(friends -> {
                    Set<String> friendIds = friends.stream()
                            .map(User::getUserId)
                            .collect(Collectors.toSet());
                    suggestedIds.removeAll(friendIds);
                    return suggestedIds;
                });
            }).thenCompose(suggestedIds -> {
                // Get user details for suggestions
                List<CompletableFuture<User>> userFutures = suggestedIds.stream()
                        .limit(10)
                        .map(this::getUserFromFirebase)
                        .collect(Collectors.toList());
                
                return CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> userFutures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()));
            }).thenAccept(future::complete)
            .exceptionally(ex -> {
                future.completeExceptionally(ex);
                return null;
            });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to get friend suggestions: " + e.getMessage()));
        }
        
        return future;
    }

    public CompletableFuture<List<FriendRequest>> getPendingRequests(String userId) {
        CompletableFuture<List<FriendRequest>> future = new CompletableFuture<>();
        
        try {
            DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests");
            
            requestsRef.orderByChild("receiverId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<CompletableFuture<FriendRequest>> requestFutures = new ArrayList<>();
                            
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String status = child.child("status").getValue(String.class);
                                if ("PENDING".equals(status)) {
                                    String senderId = child.child("senderId").getValue(String.class);
                                    
                                    // Get sender details to get fullName
                                    CompletableFuture<FriendRequest> requestFuture = getUserFromFirebase(senderId)
                                            .thenApply(sender -> {
                                                FriendRequest request = new FriendRequest();
                                                request.setFriendRequestId(child.getKey());
                                                request.setSenderId(senderId);
                                                request.setReceiverId(child.child("receiverId").getValue(String.class));
                                                request.setStatus(child.child("status").getValue(String.class));
                                                request.setTimestamp(child.child("timestamp").getValue(Long.class));
                                                
                                                // Set sender name and full sender object from user data
                                                if (sender != null) {
                                                    String senderName = sender.getFullName() != null ? sender.getFullName() : sender.getUserName();
                                                    request.setSenderName(senderName);
                                                    request.setSender(sender); // Include full sender object for avatar
                                                } else {
                                                    request.setSenderName(senderId); // Fallback to senderId
                                                }
                                                
                                                return request;
                                            });
                                    
                                    requestFutures.add(requestFuture);
                                }
                            }
                            
                            if (requestFutures.isEmpty()) {
                                future.complete(new ArrayList<>());
                            } else {
                                CompletableFuture.allOf(requestFutures.toArray(new CompletableFuture[0]))
                                        .thenApply(v -> requestFutures.stream()
                                                .map(CompletableFuture::join)
                                                .collect(Collectors.toList()))
                                        .thenAccept(future::complete)
                                        .exceptionally(ex -> {
                                            future.completeExceptionally(ex);
                                            return null;
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            future.completeExceptionally(new RuntimeException("Error getting pending requests: " + error.getMessage()));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to get pending requests: " + e.getMessage()));
        }
        
        return future;
    }

    public CompletableFuture<String> shareToSocialMedia(String platform, String userId, String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            // Implement social media sharing logic here
            // This is a placeholder implementation
            future.complete("Shared to " + platform + " successfully");
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to share: " + e.getMessage()));
        }
        return future;
    }

    public CompletableFuture<List<User>> importFriendsFromPlatform(String platform, String userId, List<String> contacts) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        try {
            // Implement friend import logic here
            // This is a placeholder implementation
            future.complete(new ArrayList<>());
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to import friends: " + e.getMessage()));
        }
        return future;
    }

    // Helper methods
    private CompletableFuture<List<User>> getAcceptedFriendsFromFirebase(String userId) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        
        System.out.println("=== DEBUG: Getting accepted friends from Firebase for userId: " + userId + " ===");
        
        DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests");
        List<User> friends = new ArrayList<>();
        
        // Get friends where user is sender and status is ACCEPTED
        requestsRef.orderByChild("senderId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<CompletableFuture<User>> userFutures = new ArrayList<>();
                        
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String status = child.child("status").getValue(String.class);
                            if ("ACCEPTED".equals(status)) {
                                String receiverId = child.child("receiverId").getValue(String.class);
                                System.out.println("=== DEBUG: Found accepted friend ID: " + receiverId + " ===");
                                userFutures.add(getUserFromFirebase(receiverId));
                            }
                        }
                        
                        // Also get friends where user is receiver and status is ACCEPTED
                        requestsRef.orderByChild("receiverId").equalTo(userId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot2) {
                                        for (DataSnapshot child : snapshot2.getChildren()) {
                                            String status = child.child("status").getValue(String.class);
                                            if ("ACCEPTED".equals(status)) {
                                                String senderId = child.child("senderId").getValue(String.class);
                                                System.out.println("=== DEBUG: Found accepted friend ID: " + senderId + " ===");
                                                userFutures.add(getUserFromFirebase(senderId));
                                            }
                                        }
                                        
                                        if (userFutures.isEmpty()) {
                                            System.out.println("=== DEBUG: No accepted friends found ===");
                                            future.complete(new ArrayList<>());
                                        } else {
                                            CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                                                    .thenApply(v -> userFutures.stream()
                                                            .map(CompletableFuture::join)
                                                            .filter(Objects::nonNull)
                                                            .collect(Collectors.toList()))
                                                    .thenAccept(friendsList -> {
                                                        System.out.println("=== DEBUG: Successfully loaded " + friendsList.size() + " accepted friends ===");
                                                        future.complete(friendsList);
                                                    })
                                                    .exceptionally(ex -> {
                                                        System.out.println("=== DEBUG: Error loading friends: " + ex.getMessage() + " ===");
                                                        future.completeExceptionally(ex);
                                                        return null;
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        System.out.println("=== DEBUG: Firebase friends query cancelled: " + error.getMessage() + " ===");
                                        future.completeExceptionally(new RuntimeException("Error getting friends: " + error.getMessage()));
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("=== DEBUG: Firebase friends query cancelled: " + error.getMessage() + " ===");
                        future.completeExceptionally(new RuntimeException("Error getting friends: " + error.getMessage()));
                    }
                });
        
        return future;
    }

    private CompletableFuture<User> getUserFromFirebase(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();
        
        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userId);
        
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                future.complete(user);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new RuntimeException("Error getting user: " + error.getMessage()));
            }
        });
        
        return future;
    }
}
