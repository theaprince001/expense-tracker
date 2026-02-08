package com.expensetracker.realtime.event;

import com.expensetracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handleBudgetAlert(BudgetAlertEvent event) {
        notificationService.sendBudgetAlert(
                event.getEmail(),
                event.getMessage()
        );
    }
}
