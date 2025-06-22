package vn.edu.hcumuaf.locket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcumuaf.locket.dto.UserProfileRequest;
import vn.edu.hcumuaf.locket.service.FriendService;
import vn.edu.hcumuaf.locket.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    //update lan dau gom fullname và avatar
    @PutMapping("/{id}/updateProfile")
    public ResponseEntity<?> updateUserProfile(@PathVariable String id, @RequestBody UserProfileRequest profile) {
        try {
            userService.updateInit(id, profile);
            return ResponseEntity.ok("Cập nhật thông tin thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }
}
