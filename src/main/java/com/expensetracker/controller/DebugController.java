package com.expensetracker.controller;

import com.expensetracker.service.BudgetAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {
    private final BudgetAlertService budgetAlertService;

    @PostMapping("/run-alerts")
    public String runAlerts() {
        budgetAlertService.checkAndSendAlerts();
        return "Alerts executed manually";
    }
}
