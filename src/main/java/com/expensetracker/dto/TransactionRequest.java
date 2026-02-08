package com.expensetracker.dto;

import com.expensetracker.entity.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    @NotNull
    private TransactionType type;

    @NotNull
    private LocalDate date;

    @NotNull
    private Long categoryId;
}
