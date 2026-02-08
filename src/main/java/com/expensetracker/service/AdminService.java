package com.expensetracker.service;

import com.expensetracker.dto.AdminTransactionResponse;
import com.expensetracker.dto.AdminUserResponse;
import com.expensetracker.dto.SystemStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    Page<AdminUserResponse> getAllUsers(Pageable pageable);

    AdminUserResponse getUserById(Long id);

    AdminUserResponse activateUser(Long id);

    AdminUserResponse deactivateUser(Long id);

    Page<AdminTransactionResponse> getAllTransactions(Pageable pageable);

    SystemStatsResponse getSystemStats();
}
