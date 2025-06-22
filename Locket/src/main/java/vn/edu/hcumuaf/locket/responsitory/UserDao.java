package vn.edu.hcumuaf.locket.responsitory;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import vn.edu.hcumuaf.locket.dto.UserProfileRequest;
import vn.edu.hcumuaf.locket.model.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Repository
public class UserDao {

    private final DatabaseReference dbRef;

    //    Constructor - khởi tạo reference đến node "users" trên Firebase
    @Autowired
    public UserDao(FirebaseDatabase firebaseDatabase) {
        this.dbRef = firebaseDatabase.getReference("users");
    }

    //    Tìm user bằng userId
//     userId ID của user cần tìm
//      Trả về boolean
    public boolean existUId(String userId) {
        return findUserById(userId) != null;
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
     * CompletableFuture<User> - User object hoặc null nếu không tìm thấy
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
     * CompletableFuture<List<User>> - Danh sách users phù hợp
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

    /*
    update fullname và url avatar khi lần dầud tạo tài khoản
    nếu bị lỗi mặc định fullname là username và lấy avatar mặc định
    */
    public void updateInit(String uid, UserProfileRequest data) {
        final String[] fullName = {data.getFullName()};
        String urlAvatar = data.getUrlAvatar();

        findUserById(uid).thenAccept(user -> {
            if (user == null) {
                throw new RuntimeException("Người dùng không tồn tại!");
            }
            //gán fullname bằng userName nếu fullname rỗng
            if (fullName[0] == null || fullName[0].trim().isEmpty()) {
                fullName[0] = user.getUserName();
            }

            user.setFullName(fullName[0]);
            user.setUrlAvatar(urlAvatar);

            // Lưu lại vào Firebase Realtime DB
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid);

            ref.setValueAsync(user)
                    .addListener(() -> System.out.println("Cập nhật thành công"),
                            Runnable::run);
        }).exceptionally(ex -> {
            System.err.println("Lỗi khi lấy thông tin user: " + ex.getMessage());
            return null;
        });
    }
}
