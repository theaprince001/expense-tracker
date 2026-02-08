package com.expensetracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
public class BudgetOverviewResponse {
    private YearMonth month;
    private BigDecimal totalBudgeted;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private BigDecimal progress;
    private List<BudgetResponse> budgets;
}
