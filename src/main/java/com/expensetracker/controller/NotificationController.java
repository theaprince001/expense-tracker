package com.expensetracker.controller;

import com.expensetracker.service.OfflineNotificationService;
import com.expensetracker.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final OfflineNotificationService offlineNotificationService;
    private final JwtService jwtService;

    @GetMapping("/offline")
    public List<String> getOfflineNotifications(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);

        return offlineNotificationService.fetchAndClear(email);
    }
}
