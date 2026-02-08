package com.expensetracker.dto;

import com.expensetracker.entity.BudgetType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.YearMonth;
import static org.junit.jupiter.api.Assertions.*;

class BudgetRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void validBudgetRequest_shouldHaveNoViolations() {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setMonth(YearMonth.of(2024, 12));
        request.setBudgetType(BudgetType.OVERALL);

        var violations = validator.validate(request);
        assertEquals(0, violations.size(), "Valid request should have no violations");
    }

    @Test
    void nullAmount_shouldHaveViolation() {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(null); // Invalid
        request.setMonth(YearMonth.of(2024, 12));
        request.setBudgetType(BudgetType.OVERALL);

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null amount should be invalid");
    }

    @Test
    void amountTooSmall_shouldHaveViolation() {
        BudgetRequest request = new BudgetRequest();
        request.setAmount(new BigDecimal("0.00")); // Too small
        request.setMonth(YearMonth.of(2024, 12));
        request.setBudgetType(BudgetType.OVERALL);

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Amount must be > 0.01");
    }
}