package com.expensetracker.service;

import com.expensetracker.notification.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationSender notificationSender;

    public void sendBudgetAlert(String email, String message) {
        notificationSender.sendToUser(email, message);
    }
}
