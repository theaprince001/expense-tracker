package com.expensetracker.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Data
@Builder
public class MonthlyReportData {
    private YearMonth month;
    private String userName;
    private String userEmail;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;
    private BigDecimal savingsRate;


    private List<CategorySummary> topCategories;
    private List<TransactionSummary> recentTransactions;
    private List<BudgetStatus> budgetStatuses;

    @Data
    @Builder
    public static class CategorySummary {
        private String categoryName;
        private BigDecimal amount;
        private BigDecimal percentage;

        public static CategorySummary fromArray(Object[] result) {
            return CategorySummary.builder()
                    .categoryName((String) result[0])
                    .amount((BigDecimal) result[1])
                    .percentage((BigDecimal) result[2])
                    .build();
        }
    }

    @Data
    @Builder
    public static class TransactionSummary {
        private LocalDate date;
        private String description;
        private BigDecimal amount;
        private String type;
        private String categoryName;
        public static TransactionSummary fromTransaction(com.expensetracker.entity.Transaction t) {
            return TransactionSummary.builder()
                    .date(t.getDate())
                    .description(t.getDescription())
                    .amount(t.getAmount())
                    .type(t.getType().name())
                    .categoryName(t.getCategory() != null ? t.getCategory().getName() : "Uncategorized")
                    .build();
        }
    }

    @Data
    @Builder
    public static class BudgetStatus {
        private String budgetName;
        private BigDecimal budgeted;
        private BigDecimal spent;
        private BigDecimal remaining;
        private BigDecimal progress;
        private String status;

        public static String calculateStatus(BigDecimal progress) {
            if (progress.compareTo(BigDecimal.valueOf(100)) >= 0) {
                return "Exceeded";
            } else if (progress.compareTo(BigDecimal.valueOf(80)) >= 0) {
                return "Warning";
            } else {
                return "On Track";
            }
        }

        // Helper method to create from Budget entity
        public static BudgetStatus fromBudget(com.expensetracker.entity.Budget budget, BigDecimal spent) {
            BigDecimal progress = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(budget.getAmount(), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;

            String budgetName = budget.getBudgetType().name();
            if (budget.getCategory() != null) {
                budgetName = budget.getCategory().getName();
            }

            return BudgetStatus.builder()
                    .budgetName(budgetName)
                    .budgeted(budget.getAmount())
                    .spent(spent)
                    .remaining(budget.getAmount().subtract(spent))
                    .progress(progress)
                    .status(calculateStatus(progress))
                    .build();
        }
    }

    // Helper method to create a sample for testing
    public static MonthlyReportData createSample(YearMonth month) {
        return MonthlyReportData.builder()
                .month(month)
                .userName("John Doe")
                .userEmail("john@example.com")
                .totalIncome(new BigDecimal("5000.00"))
                .totalExpense(new BigDecimal("3200.00"))
                .netBalance(new BigDecimal("1800.00"))
                .savingsRate(new BigDecimal("36.00"))
                .topCategories(List.of(
                        CategorySummary.builder()
                                .categoryName("Food & Dining")
                                .amount(new BigDecimal("850.00"))
                                .percentage(new BigDecimal("26.56"))
                                .build(),
                        CategorySummary.builder()
                                .categoryName("Transportation")
                                .amount(new BigDecimal("450.00"))
                                .percentage(new BigDecimal("14.06"))
                                .build()
                ))
                .recentTransactions(List.of(
                        TransactionSummary.builder()
                                .date(LocalDate.of(2024, 12, 15))
                                .description("Grocery shopping")
                                .amount(new BigDecimal("125.50"))
                                .type("EXPENSE")
                                .categoryName("Food & Dining")
                                .build(),
                        TransactionSummary.builder()
                                .date(LocalDate.of(2024, 12, 14))
                                .description("Salary deposit")
                                .amount(new BigDecimal("2500.00"))
                                .type("INCOME")
                                .categoryName("Salary")
                                .build()
                ))
                .budgetStatuses(List.of(
                        BudgetStatus.builder()
                                .budgetName("Overall")
                                .budgeted(new BigDecimal("3500.00"))
                                .spent(new BigDecimal("3200.00"))
                                .remaining(new BigDecimal("300.00"))
                                .progress(new BigDecimal("91.43"))
                                .status("Warning")
                                .build(),
                        BudgetStatus.builder()
                                .budgetName("Food & Dining")
                                .budgeted(new BigDecimal("800.00"))
                                .spent(new BigDecimal("850.00"))
                                .remaining(new BigDecimal("-50.00"))
                                .progress(new BigDecimal("106.25"))
                                .status("Exceeded")
                                .build()
                ))
                .build();
    }
}