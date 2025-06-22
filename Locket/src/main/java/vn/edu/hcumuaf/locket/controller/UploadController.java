package vn.edu.hcumuaf.locket.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.netty.util.internal.ObjectUtil;
import lombok.extern.java.Log;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.hcumuaf.locket.model.entity.UploadImageResponse;
import vn.edu.hcumuaf.locket.service.AuthService;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private AuthService authService;

    @PostMapping
    public ResponseEntity<?> uploadImage(
            @RequestParam("userId") String uid,
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("image") MultipartFile file) throws IOException, FirebaseAuthException {
        try {
            // Gọi lại hàm dùng chung
            FirebaseToken token = authService.verifyAndExtractToken(uid, authHeader);

            // Gửi tùy chọn folder khi upload
            Map options = ObjectUtils.asMap("folder", "Modis");

            // Kết quả trả về từ Cloudinary sau khi upload ảnh
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            // Trả về secure_url (link ảnh công khai)
            return ResponseEntity.ok(Map.of("url", uploadResult.get("secure_url")));
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi upload ảnh: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi không xác định: " + e.getMessage());
        }
    }
}
