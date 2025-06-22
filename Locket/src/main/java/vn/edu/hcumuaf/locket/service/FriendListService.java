package vn.edu.hcumuaf.locket.service;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hcumuaf.locket.model.FriendRequestStatus;
import vn.edu.hcumuaf.locket.model.entity.FriendRequest;
import vn.edu.hcumuaf.locket.model.entity.User;
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
        try {
            getAcceptedFriendsFromFirebase(userId).thenCompose(friends -> {
                        System.out.println("Found " + friends.size() + " accepted friends for user " + userId);
                        return getFriendSuggestions(userId).thenApply(suggestions -> {
                            System.out.println("Found " + suggestions.size() + " suggestions for user " + userId);
                            return new FriendListResponse(friends, friends.size(), MAX_FRIENDS, suggestions);
                        });
                    }).thenAccept(future::complete)
                    .exceptionally(ex -> {
                        System.out.println("Error getting friend list: " + ex.getMessage());
                        future.completeExceptionally(ex);
                        return null;
                    });
        } catch (Exception e) {
            System.out.println("Exception in getFriendList: " + e.getMessage());
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

    public CompletableFuture<String> cancelFriendRequest(String requestId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            DatabaseReference requestRef = firebaseDatabase.getReference("friendRequests").child(requestId);
            requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        future.completeExceptionally(new RuntimeException("Friend request not found"));
                        return;
                    }
                    requestRef.child("status").setValue("CANCELLED", (error, ref) -> {
                        if (error != null) {
                            future.completeExceptionally(new RuntimeException("Failed to cancel friend request: " + error.getMessage()));
                        } else {
                            future.complete("Friend request cancelled successfully");
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    future.completeExceptionally(new RuntimeException("Failed to cancel friend request: " + error.getMessage()));
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to cancel friend request: " + e.getMessage()));
        }
        return future;
    }

    public CompletableFuture<String> removeFriend(String userId, String friendId) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests");
            requestsRef.orderByChild("senderId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String receiverId = child.child("receiverId").getValue(String.class);
                                if (friendId.equals(receiverId)) {
                                    child.getRef().child("status").setValue("CANCELLED", (error, ref) -> {
                                        if (error != null) {
                                            System.out.println("Error removing friend request: " + error.getMessage());
                                        }
                                    });
                                }
                            }

                            requestsRef.orderByChild("senderId").equalTo(friendId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot2) {
                                            for (DataSnapshot child : snapshot2.getChildren()) {
                                                String receiverId = child.child("receiverId").getValue(String.class);
                                                if (userId.equals(receiverId)) {
                                                    child.getRef().child("status").setValue("CANCELLED", (error, ref) -> {
                                                        if (error != null) {
                                                            System.out.println("Error removing friend request: " + error.getMessage());
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
                        return getAcceptedFriendsFromFirebase(userId).thenApply(friends -> {
                            Set<String> friendIds = friends.stream()
                                    .map(User::getUserId)
                                    .collect(Collectors.toSet());
                            suggestedIds.removeAll(friendIds);
                            return suggestedIds;
                        });
                    }).thenCompose(suggestedIds -> {
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

                                    CompletableFuture<FriendRequest> requestFuture = getUserFromFirebase(senderId)
                                            .thenApply(sender -> {
                                                FriendRequest request = new FriendRequest();
                                                request.setFriendRequestId(child.getKey());
                                                request.setSenderId(senderId);
                                                request.setReceiverId(child.child("receiverId").getValue(String.class));
                                                request.setStatus(FriendRequestStatus.valueOf(child.child("status").getValue(String.class)));
                                                request.setTimestamp(child.child("timestamp").getValue(Long.class));

                                                if (sender != null) {
                                                    String senderName = sender.getFullName() != null ? sender.getFullName() : sender.getUserName();
                                                    request.setSenderName(senderName);
                                                    request.setSender(sender);
                                                } else {
                                                    request.setSenderName(senderId);
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

    public CompletableFuture<List<FriendRequest>> getSentRequests(String userId) {
        CompletableFuture<List<FriendRequest>> future = new CompletableFuture<>();
        try {
            DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests");
            requestsRef.orderByChild("senderId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            List<CompletableFuture<FriendRequest>> requestFutures = new ArrayList<>();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String status = child.child("status").getValue(String.class);
                                if ("PENDING".equals(status)) {
                                    String receiverId = child.child("receiverId").getValue(String.class);
                                    CompletableFuture<FriendRequest> requestFuture = getUserFromFirebase(receiverId)
                                            .thenApply(receiver -> {
                                                FriendRequest request = new FriendRequest();
                                                request.setFriendRequestId(child.getKey());
                                                request.setSenderId(child.child("senderId").getValue(String.class));
                                                request.setReceiverId(receiverId);
                                                request.setStatus(FriendRequestStatus.valueOf(child.child("status").getValue(String.class)));
                                                request.setTimestamp(child.child("timestamp").getValue(Long.class));
                                                if (receiver != null) {
                                                    String receiverName = receiver.getFullName() != null ? receiver.getFullName() : receiver.getUserName();
                                                    request.setSenderName(receiverName);
                                                    request.setSender(receiver);
                                                } else {
                                                    request.setSenderName(receiverId);
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
                            future.completeExceptionally(new RuntimeException("Error getting sent requests: " + error.getMessage()));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to get sent requests: " + e.getMessage()));
        }
        return future;
    }

    public CompletableFuture<String> shareToSocialMedia(String platform, String userId, String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            future.complete("Shared to " + platform + " successfully");
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to share: " + e.getMessage()));
        }
        return future;
    }

    public CompletableFuture<List<User>> importFriendsFromPlatform(String platform, String userId, List<String> contacts) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        try {
            future.complete(new ArrayList<>());
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to import friends: " + e.getMessage()));
        }
        return future;
    }

    private CompletableFuture<List<User>> getAcceptedFriendsFromFirebase(String userId) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests");
        requestsRef.orderByChild("senderId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<CompletableFuture<User>> userFutures = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String status = child.child("status").getValue(String.class);
                            if ("ACCEPTED".equals(status)) {
                                String receiverId = child.child("receiverId").getValue(String.class);
                                userFutures.add(getUserFromFirebase(receiverId));
                            }
                        }
                        requestsRef.orderByChild("receiverId").equalTo(userId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot2) {
                                        for (DataSnapshot child : snapshot2.getChildren()) {
                                            String status = child.child("status").getValue(String.class);
                                            if ("ACCEPTED".equals(status)) {
                                                String senderId = child.child("senderId").getValue(String.class);
                                                userFutures.add(getUserFromFirebase(senderId));
                                            }
                                        }
                                        if (userFutures.isEmpty()) {
                                            future.complete(new ArrayList<>());
                                        } else {
                                            CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                                                    .thenApply(v -> userFutures.stream()
                                                            .map(CompletableFuture::join)
                                                            .filter(Objects::nonNull)
                                                            .collect(Collectors.toList()))
                                                    .thenAccept(friendsList -> {
                                                        future.complete(friendsList);
                                                    })
                                                    .exceptionally(ex -> {
                                                        future.completeExceptionally(ex);
                                                        return null;
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        future.completeExceptionally(new RuntimeException("Error getting friends: " + error.getMessage()));
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
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

    public CompletableFuture<FriendRequest> getFriendRequestStatus(String userId1, String userId2) {
        CompletableFuture<FriendRequest> future = new CompletableFuture<>();
        try {
            DatabaseReference requestsRef = firebaseDatabase.getReference("friendRequests");

            requestsRef.orderByChild("senderId").equalTo(userId1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String receiverId = child.child("receiverId").getValue(String.class);
                                if (userId2.equals(receiverId)) {
                                    FriendRequest request = new FriendRequest();
                                    request.setFriendRequestId(child.getKey());
                                    request.setSenderId(child.child("senderId").getValue(String.class));
                                    request.setReceiverId(receiverId);
                                    request.setStatus(FriendRequestStatus.valueOf(child.child("status").getValue(String.class)));
                                    request.setTimestamp(child.child("timestamp").getValue(Long.class));
                                    future.complete(request);
                                    return;
                                }
                            }

                            requestsRef.orderByChild("senderId").equalTo(userId2)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot snapshot2) {
                                            for (DataSnapshot child : snapshot2.getChildren()) {
                                                String receiverId = child.child("receiverId").getValue(String.class);
                                                if (userId1.equals(receiverId)) {
                                                    FriendRequest request = new FriendRequest();
                                                    request.setFriendRequestId(child.getKey());
                                                    request.setSenderId(child.child("senderId").getValue(String.class));
                                                    request.setReceiverId(receiverId);
                                                    request.setStatus(FriendRequestStatus.valueOf(child.child("status").getValue(String.class)));
                                                    request.setTimestamp(child.child("timestamp").getValue(Long.class));
                                                    future.complete(request);
                                                    return;
                                                }
                                            }
                                            future.complete(null);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            future.completeExceptionally(new RuntimeException("Error getting friend request status: " + error.getMessage()));
                                        }
                                    });
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            future.completeExceptionally(new RuntimeException("Error getting friend request status: " + error.getMessage()));
                        }
                    });
        } catch (Exception e) {
            future.completeExceptionally(new RuntimeException("Failed to get friend request status: " + e.getMessage()));
        }
        return future;
    }
}
