package com.expensetracker.notification;

public interface NotificationSender {
    void sendToUser(String email, String message);
}
