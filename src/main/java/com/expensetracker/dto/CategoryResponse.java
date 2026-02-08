package com.expensetracker.dto;

import com.expensetracker.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private TransactionType type;
    private String description;
    private String color;
    private String icon;
    private Boolean isCustom;
    private LocalDateTime createdAt;
}