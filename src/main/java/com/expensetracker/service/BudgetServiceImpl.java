package com.expensetracker.service;

import com.expensetracker.dto.*;
import com.expensetracker.entity.*;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.exception.UnauthorizedAccessException;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.security.CurrentUserService;
import com.expensetracker.service.BudgetService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        Long userId = currentUserService.getCurrentUserId();

        if (request.getCategoryId() != null &&
                budgetRepository.findByUserIdAndMonthAndCategoryId(
                        userId, request.getMonth(), request.getCategoryId()).isPresent()) {
            throw new ConflictException("Budget already exists for this category and month");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Budget budget = new Budget();
        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setBudgetType(request.getBudgetType());
        budget.setCategory(category);
        budget.setUser(currentUserService.getCurrentUser());

        Budget saved = budgetRepository.save(budget);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        Long userId = currentUserService.getCurrentUserId();
        Budget budget = findOwnedBudget(id, userId);

        budget.setAmount(request.getAmount());
        budget.setMonth(request.getMonth());
        budget.setBudgetType(request.getBudgetType());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            budget.setCategory(category);
        }

        Budget updated = budgetRepository.save(budget);
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteBudget(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        Budget budget = findOwnedBudget(id, userId);
        budgetRepository.delete(budget);
    }

    @Override
    public List<BudgetResponse> findAllForUser() {
        Long userId = currentUserService.getCurrentUserId();
        return budgetRepository.findByUserIdOrderByMonthDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BudgetResponse findById(Long id) {
        Long userId = currentUserService.getCurrentUserId();
        Budget budget = findOwnedBudget(id, userId);
        return toResponse(budget);
    }

    @Override
    public BudgetOverviewResponse getBudgetOverview(YearMonth month) {
        Long userId = currentUserService.getCurrentUserId();
        List<Budget> budgets = budgetRepository.findByUserIdAndMonth(userId, month);

        BigDecimal totalBudgeted = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Budget budget : budgets) {
            BigDecimal spent = calculateSpent(budget);
            totalBudgeted = totalBudgeted.add(budget.getAmount());
            totalSpent = totalSpent.add(spent);
        }

        BigDecimal remaining = totalBudgeted.subtract(totalSpent);
        BigDecimal progress = totalBudgeted.compareTo(BigDecimal.ZERO) > 0
                ? totalSpent.divide(totalBudgeted, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return BudgetOverviewResponse.builder()
                .month(month)
                .totalBudgeted(totalBudgeted)
                .totalSpent(totalSpent)
                .remaining(remaining)
                .progress(progress)
                .budgets(budgets.stream().map(this::toResponse).toList())
                .build();
    }

    private Budget findOwnedBudget(Long id, Long userId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Not authorized to access this budget");
        }
        return budget;
    }

    private BigDecimal calculateSpent(Budget budget) {
        LocalDate startDate = budget.getMonth().atDay(1);
        LocalDate endDate = budget.getMonth().atEndOfMonth();

        Long userId = budget.getUser().getId();

        if (budget.getBudgetType() == BudgetType.CATEGORY && budget.getCategory() != null) {
            return transactionRepository.findByUserIdAndDateBetweenAndTypeAndCategoryId(
                            userId, startDate, endDate, TransactionType.EXPENSE, budget.getCategory().getId())
                    .stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            return transactionRepository.findByUserIdAndDateBetweenAndType(
                            userId, startDate, endDate, TransactionType.EXPENSE)
                    .stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    private BudgetResponse toResponse(Budget budget) {
        BigDecimal spent = calculateSpent(budget);
        BigDecimal remaining = budget.getAmount().subtract(spent);
        BigDecimal progress = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getAmount(), 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return BudgetResponse.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .month(budget.getMonth())
                .budgetType(budget.getBudgetType())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
                .spent(spent)
                .remaining(remaining)
                .progress(progress)
                .build();
    }
}
