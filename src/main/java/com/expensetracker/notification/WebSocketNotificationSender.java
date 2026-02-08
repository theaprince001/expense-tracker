package com.expensetracker.notification;

import com.expensetracker.realtime.websocket.WebSocketSessionRegistry;
import com.expensetracker.service.OfflineNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationSender implements NotificationSender {

    private final WebSocketSessionRegistry sessionRegistry;
    private final OfflineNotificationService offlineNotificationService;

    @Override
    public void sendToUser(String email, String message) {

        var session = sessionRegistry.getActiveSession(email);

        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                offlineNotificationService.save(email, message);
                sessionRegistry.remove(email);
            }
        } else {
            offlineNotificationService.save(email, message);
        }
    }
}
