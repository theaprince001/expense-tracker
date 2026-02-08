package com.expensetracker.dto;

import com.expensetracker.entity.TransactionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validTransactionRequest_shouldPass() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("50.00"));
        request.setDescription("Groceries");
        request.setType(TransactionType.EXPENSE);
        request.setDate(LocalDate.now());
        request.setCategoryId(1L);

        var violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void nullAmount_shouldFail() {
        TransactionRequest request = new TransactionRequest();
        request.setDescription("Test");
        request.setType(TransactionType.EXPENSE);
        request.setDate(LocalDate.now());

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void negativeAmount_shouldFail() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(new BigDecimal("-10.00"));
        request.setDescription("Test");
        request.setType(TransactionType.EXPENSE);
        request.setDate(LocalDate.now());

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
