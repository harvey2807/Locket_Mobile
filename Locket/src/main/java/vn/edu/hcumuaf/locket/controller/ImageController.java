package vn.edu.hcumuaf.locket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcumuaf.locket.model.entity.Image;
import vn.edu.hcumuaf.locket.service.ImageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<List<Image>>> getImagesByUserId(@PathVariable String userId) {
        return imageService.getImagesByUserId(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.internalServerError().build());
    }

    @GetMapping("/pair")
    public CompletableFuture<ResponseEntity<List<Image>>> getImagesBySenderIdAndReceiverId
            (@RequestParam String senderId, @RequestParam String receiverId) {
        return imageService.getImagesBySenderIdAndReceiverId(senderId, receiverId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.internalServerError().build());
    }

    @GetMapping("/{imageId}")
    public CompletableFuture<ResponseEntity<?>> getImageById(@PathVariable String imageId) {
        return imageService.getImageById(imageId)
                .thenApply(image -> {
                    if (image != null) return ResponseEntity.ok(image);
                    else return ResponseEntity.notFound().build();
                })
                .exceptionally(ex -> ResponseEntity.internalServerError().build());
    }


    @GetMapping("/all/{userId}")
    public CompletableFuture<ResponseEntity<List<Image>>> getAllImagesByUserId(@PathVariable String userId) {
        System.out.println("Chay controller getALlImage roi nay");
        return imageService.getAllImagesByUserId(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.internalServerError().build());
    }

    @PostMapping("/save")
    public CompletableFuture<ResponseEntity<Map<String, String>>> saveImage(@RequestBody Image image) {
        return imageService.saveImage(image)
                .thenApply(v -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Lưu thành công");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("message", "Lỗi khi lưu: " + ex.getMessage());
                    return ResponseEntity.status(500).body(error);
                });
    }
}