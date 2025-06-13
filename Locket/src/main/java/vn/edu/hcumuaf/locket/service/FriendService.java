
package vn.edu.hcumuaf.locket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.hcumuaf.locket.model.entity.User;
import vn.edu.hcumuaf.locket.responsitory.FriendRequestDao;
import vn.edu.hcumuaf.locket.responsitory.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private FriendRequestDao frDao;

    @Autowired
    private UserDao userDao;

    public CompletableFuture<List<User>> getListFriendByUserId(String userId) {
        return frDao.findListFriendByUserID(userId)
                .thenCompose(friendIds -> {
                    List<CompletableFuture<User>> userFutures = friendIds.stream()
                            .map(userDao::findUserById)
                            .toList();

                    return CompletableFuture.allOf(userFutures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> userFutures.stream()
                                    .map(CompletableFuture::join)
                                    .toList());
                });
    }

    public CompletableFuture<Set<String>> getFriendIdsByUserId(String userId) {
        return frDao.findListFriendByUserID(userId);
    }
}
