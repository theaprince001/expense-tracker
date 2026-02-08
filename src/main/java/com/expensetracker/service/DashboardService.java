package com.expensetracker.service;

import com.expensetracker.dto.DashboardSummary;
import com.expensetracker.dto.CategorySpending;
import com.expensetracker.dto.MonthlyTrend;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface DashboardService {
    DashboardSummary getFinancialSummary(YearMonth month);
    List<CategorySpending> getCategorySpending(LocalDate startDate, LocalDate endDate);
    List<MonthlyTrend> getMonthlyTrends(int months);
}
