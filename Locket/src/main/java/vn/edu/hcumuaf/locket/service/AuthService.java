package vn.edu.hcumuaf.locket.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DatabaseReference;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import vn.edu.hcumuaf.locket.config.FirebaseConfig;
import vn.edu.hcumuaf.locket.dto.SignupRequest;
import vn.edu.hcumuaf.locket.responsitory.UserDao;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.Logger.Level.DEBUG;

@Service
public class AuthService {

    @Autowired
    private FirebaseConfig firebaseConfig;

    @Autowired
    private UserDao userDao;

    public ResponseEntity<?> register(@RequestBody SignupRequest request) {
        try {
            // 1. Tạo user trên Firebase Auth
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setUid(request.getUserName())
                    .setPhoneNumber(request.getPhoneNumber())
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword())
                    .setDisplayName(request.getUserName());

            UserRecord userRecord = firebaseConfig.firebaseAuth().createUser(createRequest);
            String userId = userRecord.getUid();

            // 2. Lưu dữ liệu vào Firebase Realtime Database
            DatabaseReference ref = firebaseConfig.firebaseDatabase().getReference("users");

            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("userName", request.getUserName());
            userData.put("phoneNumber", request.getPhoneNumber());
            userData.put("email", request.getEmail());
            userData.put("password", hashPassword(request.getPassword()));

            ref.child(userId).setValueAsync(userData);

            return ResponseEntity.ok("Đăng ký thành công. UID: " + userId);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi tạo tài khoản: " + e.getMessage());

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + ex.getMessage());
        }
    }

    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        try {
            // Verify idToken
            FirebaseToken decodedToken = firebaseConfig.firebaseAuth().verifyIdToken(idToken);
            String uid = decodedToken.getUid();

            // lấy thông tin hiển thị
            String email = decodedToken.getEmail();

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", uid);
            userInfo.put("message", "Đăng nhập thành công");

            return ResponseEntity.ok(userInfo);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String hashPassword(String rawPassword) {
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }

    //xác thực token để return về bên fe
    public ResponseEntity<?> verifyToken(String userId, String authHeader) throws IOException, FirebaseAuthException {
       try {

           if (authHeader == null || !authHeader.startsWith("Bearer ")) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body("Token không hợp lệ: Thiếu tiền tố Bearer");
           }

           //bỏ tiền tố "Bearer"
           String token = authHeader.substring(7);
           FirebaseToken decodedToken = firebaseConfig.firebaseAuth().verifyIdToken(token);
           String uidFromToken = decodedToken.getUid();

           if (!uidFromToken.equals(userId)) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                       .body("UID trong token không khớp với userId trong đường dẫn.");
           }

           Map<String, Object> response = new HashMap<>();
           response.put("userId", uidFromToken);
           response.put("message", "Xác thực thành công");

           return ResponseEntity.ok(response);
       } catch (FirebaseAuthException e) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                   .body("Token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
       }
    }

    //xác thực token bên be
    public FirebaseToken verifyAndExtractToken(String uid, String authHeader) throws FirebaseAuthException, IOException {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Không tồn tại token hoặc sai định dạng.");

    }
        String token = authHeader.substring(7);
        FirebaseToken decodedToken = firebaseConfig.firebaseAuth().verifyIdToken(token);

        if (!decodedToken.getUid().equals(uid)) {
            throw new SecurityException("UID trong token không khớp với userId yêu cầu");
        }
        return decodedToken;
    }
}
