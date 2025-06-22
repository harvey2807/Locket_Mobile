package vn.edu.hcumuaf.locket.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.hcumuaf.locket.model.entity.Rooms;
import vn.edu.hcumuaf.locket.responsitory.RoomDao;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class RoomService {
    @Autowired
    private RoomDao roomDao;
    private FirebaseDatabase database;
    private Random random;

    public RoomService(FirebaseDatabase database) {
        this.database = database;
        random = new Random();
    }

    public CompletableFuture<List<Rooms>> getRoomsByUserId(@PathVariable String userId, String receiverId) {
        return roomDao.getRomsByUserIdWithReceiverId(userId, receiverId)
                .thenApply(rooms -> {
                    rooms.sort(Comparator.comparing(Rooms::getTimestamp));
                    return rooms;
                });

    }

    public ResponseEntity<List<Object>> updateLastMessage(@PathVariable String chatId) {
        return null;
    }

    public void saveRoom(Rooms room) {
        DatabaseReference ref = database.getReference("rooms").push();
        Map<String, Object> map = new HashMap<>();
        room.setId(room.getOwnerId() + "_" + room.getReceiverId());
        room.setTimestamp(System.currentTimeMillis());
        map.put("Id", room.getId());
        map.put("OwnerId", room.getOwnerId());
        map.put("ReceiverId", room.getReceiverId());
        map.put("Timestamp", room.getTimestamp());
        ref.setValueAsync(room);

    }
}
