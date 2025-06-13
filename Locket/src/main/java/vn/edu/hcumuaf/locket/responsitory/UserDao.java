package vn.edu.hcumuaf.locket.responsitory;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import vn.edu.hcumuaf.locket.model.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Repository
public class UserDao {
    
    // Reference đến node "users" trên Firebase
    private final DatabaseReference dbRef;

    // Executor để xử lý async operations
    @Qualifier("taskExecutor")
    @Autowired
    private Executor taskExecutor;

//    Constructor - khởi tạo reference đến node "users" trên Firebase
    @Autowired
    public UserDao(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference("users");
    }


//    Tìm user bằng userId
//     userId ID của user cần tìm
//     CompletableFuture<User> - User object hoặc null nếu không tìm thấy
    public CompletableFuture<User> findUserById(String userId) {
        CompletableFuture<User> future = new CompletableFuture<>();

        // Query Firebase theo userId
        dbRef.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // Duyệt qua kết quả query
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User user = snap.getValue(User.class);
                            future.complete(user);
                            return;
                        }
                        future.complete(null); // Không tìm thấy user
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }

    /**
     * Tìm user bằng email
     * THÊM MỚI: Để lấy data user với email Email của user cần tìm
     *  CompletableFuture<User> - User object hoặc null nếu không tìm thấy
     */
    public CompletableFuture<User> findUserByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();

        // Query Firebase theo email
        dbRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // Duyệt qua kết quả query
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User user = snap.getValue(User.class);
                            future.complete(user);
                            return;
                        }
                        future.complete(null); // Không tìm thấy user
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }

    /**
     * Tìm kiếm users theo tên hoặc username
     * query Từ khóa tìm kiếm (có thể là tên hoặc username)
     *  CompletableFuture<List<User>> - Danh sách users phù hợp
     */
    public CompletableFuture<List<User>> searchUsers(String query) {
        CompletableFuture<List<User>> future = new CompletableFuture<>();
        List<User> users = new ArrayList<>();

        // Kiểm tra query có hợp lệ không
        if (query == null || query.trim().isEmpty()) {
            future.complete(users);
            return future;
        }

        String searchQuery = query.toLowerCase().trim();

        // ===== TÌM KIẾM THEO FULLNAME =====
        // Query Firebase theo fullName
        dbRef.orderByChild("fullName")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        // Duyệt qua tất cả users và kiểm tra fullName
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            User user = snap.getValue(User.class);
                            if (user != null && user.getFullName() != null && 
                                user.getFullName().toLowerCase().contains(searchQuery)) {
                                users.add(user);
                            }
                        }

                        // ===== TÌM KIẾM THEO USERNAME =====
                        // Query Firebase theo userName
                        dbRef.orderByChild("userName")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot2) {
                                        // Duyệt qua tất cả users và kiểm tra userName
                                        for (DataSnapshot snap : snapshot2.getChildren()) {
                                            User user = snap.getValue(User.class);
                                            if (user != null && user.getUserName() != null && 
                                                user.getUserName().toLowerCase().contains(searchQuery)) {
                                                // Kiểm tra xem user đã có trong danh sách chưa (tránh trùng lặp)
                                                boolean exists = users.stream()
                                                        .anyMatch(u -> u.getUserId().equals(user.getUserId()));
                                                if (!exists) {
                                                    users.add(user);
                                                }
                                            }
                                        }
                                        future.complete(users);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new RuntimeException(error.getMessage()));
                    }
                });

        return future;
    }

}
