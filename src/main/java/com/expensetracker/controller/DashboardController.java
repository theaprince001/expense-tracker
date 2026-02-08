package com.expensetracker.controller;

import com.expensetracker.dto.CategorySpending;
import com.expensetracker.dto.DashboardSummary;
import com.expensetracker.dto.MonthlyTrend;
import com.expensetracker.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummary> getFinancialSummary(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        if (month == null) {
            month = YearMonth.now();
        }

        DashboardSummary summary = dashboardService.getFinancialSummary(month);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategorySpending>> getCategorySpending(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate finalStart = (startDate != null) ? startDate : today.withDayOfMonth(1);
        LocalDate finalEnd = (endDate != null) ? endDate : today.withDayOfMonth(today.lengthOfMonth());

        List<CategorySpending> spending = dashboardService.getCategorySpending(finalStart, finalEnd);
        return ResponseEntity.ok(spending);
    }

    @GetMapping("/trends")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrends(
            @RequestParam(defaultValue = "6") int months) {

        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends(months);
        return ResponseEntity.ok(trends);
    }
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Dashboard API is working");
    }
}