package com.expensetracker.realtime.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketSessionRegistry sessionRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String email = (String) session.getAttributes().get("email");

        if (email != null) {
            sessionRegistry.register(email, session);
            log.info("WebSocket connected for email: {}", email);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String email = (String) session.getAttributes().get("email");

        if (email != null) {
            sessionRegistry.remove(email);
            log.info("WebSocket disconnected for email: {}", email);
        }
    }
}
