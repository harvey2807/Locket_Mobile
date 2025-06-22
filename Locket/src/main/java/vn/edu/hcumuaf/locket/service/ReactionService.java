package vn.edu.hcumuaf.locket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hcumuaf.locket.model.entity.Reaction;
import vn.edu.hcumuaf.locket.responsitory.ReactionDao;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ReactionService {
    @Autowired
    private ReactionDao reactionDao;

    public CompletableFuture<Void> addReaction(Reaction reaction) {
        return reactionDao.addReaction(reaction);
    }

    public CompletableFuture<List<String>> getFriendSendReact(String imageId) {
        return reactionDao.getFriendSendReact(imageId);
    }
}
