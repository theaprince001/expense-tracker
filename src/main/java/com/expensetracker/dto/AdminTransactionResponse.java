package com.expensetracker.dto;

import com.expensetracker.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminTransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String type;
    private LocalDateTime date;
    private String description;
    private Long userId;
    private String userEmail;

    public static AdminTransactionResponse fromEntity(Transaction t) {
        return AdminTransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType().name())
                .date(t.getDate().atStartOfDay())
                .description(t.getDescription())
                .userId(t.getUser().getId())
                .userEmail(t.getUser().getEmail())
                .build();
    }
}
