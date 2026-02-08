package com.expensetracker.realtime.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BudgetAlertEvent {

    private final String email;
    private final String message;
}
