package com.expensetracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyTrend {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal balance;
    private BigDecimal changeVsPrevious;
}
