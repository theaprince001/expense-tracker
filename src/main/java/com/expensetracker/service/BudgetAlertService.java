package com.expensetracker.service;

import com.expensetracker.entity.Budget;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.TransactionType;
import com.expensetracker.entity.User;
import com.expensetracker.realtime.event.BudgetAlertEvent;
import com.expensetracker.realtime.event.EventPublisher;
import com.expensetracker.redis.AlertStateRepository;
import com.expensetracker.repository.BudgetRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final JavaMailSender mailSender;
    private final AlertStateRepository alertStateRepository;
    private final EventPublisher eventPublisher;

    private static final List<Integer> ALERT_LEVELS = List.of(80, 90, 100);

    @Scheduled(cron = "0 0 9 * * *")
    public void checkAndSendAlerts() {
        YearMonth currentMonth = YearMonth.now();

        List<Long> userIdsWithBudgets = budgetRepository.findDistinctUserIdsByMonth(currentMonth);
        if (userIdsWithBudgets.isEmpty()) return;

        for (Long userId : userIdsWithBudgets) {

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            List<Budget> userBudgets = budgetRepository.findByUserIdAndMonth(userId, currentMonth);

            BigDecimal maxProgress = BigDecimal.ZERO;

            for (Budget budget : userBudgets) {
                AlertCheckResult result = calculateBudgetProgress(budget);
                if (result.progress.compareTo(maxProgress) > 0) {
                    maxProgress = result.progress;
                }
            }

            int progressInt = maxProgress.intValue();
            String periodKey = currentMonth.toString();

            Integer lastAlerted = alertStateRepository.getLastAlertLevel(userId, periodKey);

            Integer nextLevel = null;
            for (Integer level : ALERT_LEVELS) {
                if ((lastAlerted == null || level > lastAlerted) && progressInt >= level) {
                    nextLevel = level;
                    break;
                }
            }

            if (nextLevel != null) {

                String msg = "⚠️ Budget Alert: You crossed " + nextLevel + "% usage for " + currentMonth;
                sendAlertEmail(user, userBudgets, nextLevel, currentMonth);
                eventPublisher.publish(
                        new BudgetAlertEvent(user.getEmail(), msg)
                );
                alertStateRepository.saveAlertLevel(userId, periodKey, nextLevel);
            }
        }
    }


    private AlertCheckResult calculateBudgetProgress(Budget budget) {
        LocalDate startDate = budget.getMonth().atDay(1);
        LocalDate endDate = budget.getMonth().atEndOfMonth();

        BigDecimal spent;
        Long userId = budget.getUser().getId();

        if (budget.getBudgetType() == com.expensetracker.entity.BudgetType.CATEGORY
                && budget.getCategory() != null) {
            spent = transactionRepository
                    .findByUserIdAndDateBetweenAndTypeAndCategoryId(
                            userId,
                            startDate,
                            endDate,
                            TransactionType.EXPENSE,
                            budget.getCategory().getId())
                    .stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            spent = transactionRepository
                    .findByUserIdAndDateBetweenAndType(
                            userId,
                            startDate,
                            endDate,
                            TransactionType.EXPENSE)
                    .stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal progress = BigDecimal.ZERO;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = spent
                    .divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        return new AlertCheckResult(spent, progress);
    }

    private void sendAlertEmail(User user, List<Budget> exceededBudgets,
                                Integer threshold, YearMonth month) {

        String to = user.getEmail();
        String subject = "Budget Alert - " + month;
        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(user.getFirstName()).append(",\n\n")
                .append("You have budgets that reached or exceeded your alert threshold of ")
                .append(threshold).append("% for ").append(month).append(".\n\n");

        for (Budget budget : exceededBudgets) {
            AlertCheckResult result = calculateBudgetProgress(budget);
            String scope = budget.getBudgetType().name();
            if (budget.getCategory() != null) {
                scope += " - " + budget.getCategory().getName();
            }

            body.append("- ")
                    .append(scope)
                    .append(": Spent ")
                    .append(result.spent)
                    .append(" of ")
                    .append(budget.getAmount())
                    .append(" (")
                    .append(result.progress).append("%)\n");
        }

        body.append("\nConsider reviewing your recent expenses in the app.\n\n")
                .append("Best regards,\n")
                .append("Expense Tracker");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("Expense Tracker <noreply@expensetracker.com>");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body.toString());
            mailSender.send(message);
            log.info("Budget alert email sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send budget alert email to {}: {}", to, e.getMessage());
        }
    }

    private record AlertCheckResult(BigDecimal spent, BigDecimal progress) {}
}
