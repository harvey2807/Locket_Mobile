package vn.edu.hcumuaf.locket.config;

import com.google.firebase.database.FirebaseDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import vn.edu.hcumuaf.locket.model.Message;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messagingTemplate;
    private FirebaseDatabase database;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            log.warn("Session attributes are null during disconnect for sessionId: {}", accessor.getSessionId());
            return;
        }

        String username = (String) sessionAttributes.get("username");
        if (username != null && !username.trim().isEmpty()) {
            log.info("User disconnected: {}", username);
            Message chatMessage = new Message();
            chatMessage.setType(Message.MessageType.LEAVE);
            chatMessage.setSenderId(username);
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        } else {
            log.warn("Username not found during disconnect for sessionId: {}", accessor.getSessionId());
        }
    }
}