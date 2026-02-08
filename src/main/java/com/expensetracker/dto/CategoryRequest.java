package com.expensetracker.dto;

import com.expensetracker.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required")
    private TransactionType type;

    private String description;

    private String color;

    private String icon;
}