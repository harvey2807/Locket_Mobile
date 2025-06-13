package vn.edu.hcumuaf.locket.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import vn.edu.hcumuaf.locket.model.ChatMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) accessor.getSessionAttributes().get("username");
        log.info("User connected: {}", username);
        if (username != null) {
            var chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.JOIN);
            chatMessage.setSenderId(username);
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) accessor.getSessionAttributes().get("username");
        if (username != null) {
            log.info("User disconnected: {}", username);
            var chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSenderId(username);
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}