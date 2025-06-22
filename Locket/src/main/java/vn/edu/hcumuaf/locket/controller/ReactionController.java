package vn.edu.hcumuaf.locket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcumuaf.locket.model.entity.Reaction;
import vn.edu.hcumuaf.locket.service.ReactionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/reaction")
public class ReactionController {
    @Autowired
    private ReactionService reactionService;

    @PostMapping("/add")
    public CompletableFuture<ResponseEntity<Map<String, String>>> addReaction(@RequestBody Reaction reaction) {
        return reactionService.addReaction(reaction)
                .thenApply(v -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    return ResponseEntity.ok(response);
                })
                .exceptionally(ex -> {
                    Map<String, String> error = new HashMap<>();
                    error.put("status", "Failed: " + ex.getMessage());
                    return ResponseEntity.status(500).body(error);
                });
    }

    @GetMapping("/for/{imageId}")
    public CompletableFuture<ResponseEntity<List<String>>> getFriendSendReact(@PathVariable("imageId") String imageId) {
        return reactionService.getFriendSendReact(imageId)
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.internalServerError().build());
    }
}
