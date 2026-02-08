package com.expensetracker.controller;

import com.expensetracker.dto.AdminTransactionResponse;
import com.expensetracker.dto.AdminUserResponse;
import com.expensetracker.dto.SystemStatsResponse;
import com.expensetracker.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminUserResponse> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Admin: fetching users page={} size={}", page, size);
        return adminService.getAllUsers(PageRequest.of(page, size));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse getUser(@PathVariable Long id) {
        log.info("Admin: fetching user {}", id);
        return adminService.getUserById(id);
    }

    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse activateUser(@PathVariable Long id) {
        log.info("Admin: activating user {}", id);
        return adminService.activateUser(id);
    }

    @PutMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponse deactivateUser(@PathVariable Long id) {
        log.info("Admin: deactivating user {}", id);
        return adminService.deactivateUser(id);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AdminTransactionResponse> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Admin: fetching transactions page={} size={}", page, size);
        return adminService.getAllTransactions(PageRequest.of(page, size));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public SystemStatsResponse getStats() {
        log.info("Admin: fetching stats");
        return adminService.getSystemStats();
    }
}
