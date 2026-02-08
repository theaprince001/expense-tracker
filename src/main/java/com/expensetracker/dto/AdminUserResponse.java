package com.expensetracker.dto;

import com.expensetracker.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean isActive;
    private String role;
    private LocalDateTime createdAt;

    public static AdminUserResponse fromEntity(User u) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .isActive(u.getIsActive())
                .role(u.getRole().name())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
