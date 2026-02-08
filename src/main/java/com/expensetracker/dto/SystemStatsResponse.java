package com.expensetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemStatsResponse {
    private long totalUsers;
    private long activeUsers;
    private long totalTransactions;
}
