package com.expensetracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResendActivationRequest {
    @NotNull
    @Email
    private String email;
}

