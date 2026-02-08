package com.expensetracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategorySpending {
    private String categoryName;
    private String categoryColor;
    private BigDecimal amount;
    private BigDecimal percentage;
}
