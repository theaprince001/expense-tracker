package com.expensetracker.service;

import com.expensetracker.dto.AdminTransactionResponse;
import com.expensetracker.dto.AdminUserResponse;
import com.expensetracker.dto.SystemStatsResponse;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(AdminUserResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ConflictException("User not found"));
        return AdminUserResponse.fromEntity(user);
    }

    @Override
    public AdminUserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ConflictException("User not found"));
        user.setIsActive(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);
        return AdminUserResponse.fromEntity(user);
    }

    @Override
    public AdminUserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ConflictException("User not found"));
        user.setIsActive(false);
        return AdminUserResponse.fromEntity(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminTransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAllWithUser(pageable)
                .map(AdminTransactionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatsResponse getSystemStats() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long totalTx = transactionRepository.count();
        return new SystemStatsResponse(totalUsers, activeUsers, totalTx);
    }
}
