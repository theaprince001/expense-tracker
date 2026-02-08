package com.expensetracker.config;

import com.expensetracker.realtime.websocket.NotificationWebSocketHandler;
import com.expensetracker.realtime.websocket.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler handler;
    private final WebSocketAuthInterceptor interceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/notifications")
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }
}
