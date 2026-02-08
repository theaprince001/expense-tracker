package com.expensetracker.dto;

import com.expensetracker.config.YearMonthFlexibleDeserializer;
import com.expensetracker.entity.BudgetType;
import com.expensetracker.entity.YearMonthAttributeConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Convert;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull
    @JsonDeserialize(using = YearMonthFlexibleDeserializer.class)
    private YearMonth month;

    @NotNull
    private BudgetType budgetType;

    private Long categoryId; // null for OVERALL budgets
}
