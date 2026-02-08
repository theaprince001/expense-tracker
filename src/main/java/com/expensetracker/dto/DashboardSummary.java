package com.expensetracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
public class DashboardSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private BigDecimal savingsRate;
    private YearMonth month;
}
