package com.expensetracker.service;

import com.expensetracker.dto.TransactionRequest;
import com.expensetracker.dto.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BusinessRuleException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.exception.UnauthorizedAccessException;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.security.CurrentUserService;
import com.expensetracker.service.CategoryService;
import com.expensetracker.service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;
    private final CurrentUserService currentUserService;
    private final ReceiptService receiptService;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        validateRequest(request);

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Amount must be greater than zero");
        }

        Long userId = currentUserService.getCurrentUserId();
        User user = currentUserService.getCurrentUser();

        Category category = categoryService.getUserCategoryById(request.getCategoryId(), userId);
        categoryService.validateCategoryForTransaction(category, userId, request.getType());

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setType(request.getType());
        transaction.setDate(request.getDate());
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUser(user);
        transaction.setCategory(category);

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions() {
        Long userId = currentUserService.getCurrentUserId();
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        Transaction transaction = findOwnedTransaction(id, userId);
        return toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        validateRequest(request);

        Long userId = currentUserService.getCurrentUserId();
        Transaction transaction = findOwnedTransaction(id, userId);

        Category category = categoryService.getUserCategoryById(request.getCategoryId(), userId);
        categoryService.validateCategoryForTransaction(category, userId, request.getType());

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setType(request.getType());
        transaction.setDate(request.getDate());
        transaction.setCategory(category);

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        Transaction transaction = findOwnedTransaction(id, userId);
        transactionRepository.delete(transaction);
    }

    private void validateRequest(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Amount must be greater than zero");
        }
        LocalDate today = LocalDate.now();
        if (request.getDate() != null && request.getDate().isAfter(today.plusYears(1))) {
            throw new BusinessRuleException("Date cannot be more than 1 year in future");
        }
    }

    private Transaction findOwnedTransaction(Long id, Long userId) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to access this transaction");
        }
        return transaction;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .type(transaction.getType())
                .date(transaction.getDate())
                .categoryName(transaction.getCategory().getName())
                .categoryColor(transaction.getCategory().getColor())
                .createdAt(transaction.getCreatedAt())
                .build();
    }


}
