package com.expensetracker.dto;

import com.expensetracker.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private String description;
    private TransactionType type;
    private LocalDate date;
    private String categoryName;
    private String categoryColor;
    private LocalDateTime createdAt;

}
