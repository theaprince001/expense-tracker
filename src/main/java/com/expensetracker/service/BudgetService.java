package com.expensetracker.service;

import com.expensetracker.dto.BudgetOverviewResponse;
import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;

import java.time.YearMonth;
import java.util.List;

public interface BudgetService {
    BudgetResponse createBudget(BudgetRequest request);
    BudgetResponse updateBudget(Long id, BudgetRequest request);
    void deleteBudget(Long id);
    List<BudgetResponse> findAllForUser();
    BudgetResponse findById(Long id);
    BudgetOverviewResponse getBudgetOverview(YearMonth month);
}
