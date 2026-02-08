package com.expensetracker.service;

import com.expensetracker.dto.*;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    @Transactional(readOnly = true)
    public DashboardSummary getFinancialSummary(YearMonth month) {
        Long userId = currentUserService.getCurrentUserId();
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        BigDecimal totalIncome = getTotalByType(userId, monthStart, monthEnd, TransactionType.INCOME);
        BigDecimal totalExpense = getTotalByType(userId, monthStart, monthEnd, TransactionType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        BigDecimal savingsRate = calculateSavingsRate(totalIncome, totalExpense);

        return DashboardSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .savingsRate(savingsRate)
                .month(month)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySpending> getCategorySpending(LocalDate startDate, LocalDate endDate) {
        Long userId = currentUserService.getCurrentUserId();

        String sql = """
            SELECT 
                c.name as category_name,
                c.color as category_color,
                SUM(t.amount) as total_spent,
                CASE 
                    WHEN total_expenses > 0 THEN 
                        ROUND((SUM(t.amount) / total_expenses * 100), 2)
                    ELSE 0 
                END as percentage
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            CROSS JOIN (SELECT SUM(amount) as total_expenses 
                       FROM transactions 
                       WHERE user_id = :userId AND type = 'EXPENSE' 
                       AND date BETWEEN :startDate AND :endDate) te
            WHERE t.user_id = :userId AND t.type = 'EXPENSE' 
            AND t.date BETWEEN :startDate AND :endDate
            GROUP BY c.id, c.name, c.color, te.total_expenses
            ORDER BY total_spent DESC
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Object[]> results = query.getResultList();
        return results.stream()
                .map(row -> CategorySpending.builder()
                        .categoryName((String) row[0])
                        .categoryColor((String) row[1])
                        .amount((BigDecimal) row[2])
                        .percentage((BigDecimal) row[3])
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyTrend> getMonthlyTrends(int months) {
        Long userId = currentUserService.getCurrentUserId();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        String sql = """
            SELECT 
                EXTRACT(YEAR FROM date) as year,
                EXTRACT(MONTH FROM date) as month_num,
                SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) as income,
                SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) as expense
            FROM transactions
            WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate
            GROUP BY EXTRACT(YEAR FROM date), EXTRACT(MONTH FROM date)
            ORDER BY year, month_num
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<Object[]> results = query.getResultList();

        List<MonthlyTrend> trends = new ArrayList<>();
        BigDecimal previousBalance = BigDecimal.ZERO;

        for (Object[] row : results) {
            int year = ((Number) row[0]).intValue();
            int monthNum = ((Number) row[1]).intValue();
            BigDecimal income = (BigDecimal) row[2];
            BigDecimal expense = (BigDecimal) row[3];
            BigDecimal balance = income.subtract(expense);

            YearMonth yearMonth = YearMonth.of(year, monthNum);
            String monthName = yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"));

            BigDecimal changeVsPrevious = previousBalance.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : balance.subtract(previousBalance)
                    .divide(previousBalance, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            trends.add(MonthlyTrend.builder()
                    .month(monthName)
                    .income(income)
                    .expense(expense)
                    .balance(balance)
                    .changeVsPrevious(changeVsPrevious)
                    .build());

            previousBalance = balance;
        }

        return trends;
    }

    private BigDecimal getTotalByType(Long userId, LocalDate startDate, LocalDate endDate, TransactionType type) {
        List<Transaction> transactions = transactionRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate)
                .stream()
                .filter(t -> t.getType() == type)
                .toList();

        return transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSavingsRate(BigDecimal income, BigDecimal expense) {
        if (income.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return income.subtract(expense)
                .divide(income, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}
