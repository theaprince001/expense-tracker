package com.expensetracker.dto;

import com.expensetracker.entity.BudgetType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@Builder
public class BudgetResponse {
    private Long id;
    private BigDecimal amount;
    private YearMonth month;
    private BudgetType budgetType;
    private Long categoryId;
    private String categoryName;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal progress; // 0-100%
}
