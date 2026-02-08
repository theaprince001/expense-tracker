package com.expensetracker.realtime.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {


    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void register(String email, WebSocketSession session) {
        sessions.put(email, session);
    }

    public WebSocketSession getActiveSession(String email) {
        return sessions.get(email);
    }

    public void remove(String email) {
        sessions.remove(email);
    }
}
