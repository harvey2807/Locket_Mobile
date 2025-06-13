package vn.edu.hcumuaf.locket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcumuaf.locket.model.entity.Image;
import vn.edu.hcumuaf.locket.service.ImageService;

import java.util.List;
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
        return imageService.getAllImagesByUserId(userId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.internalServerError().build());
    }
}