package com.expensetracker.controller;

import com.expensetracker.dto.BudgetOverviewResponse;
import com.expensetracker.dto.BudgetRequest;
import com.expensetracker.dto.BudgetResponse;
import com.expensetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getUserBudgets() {
        List<BudgetResponse> budgets = budgetService.findAllForUser();
        return ResponseEntity.ok(budgets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudget(@PathVariable Long id) {
        BudgetResponse budget = budgetService.findById(id);
        return ResponseEntity.ok(budget);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse budget = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(budget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/overview")
    public ResponseEntity<BudgetOverviewResponse> getBudgetOverview(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        BudgetOverviewResponse overview = budgetService.getBudgetOverview(month);
        return ResponseEntity.ok(overview);
    }
}
