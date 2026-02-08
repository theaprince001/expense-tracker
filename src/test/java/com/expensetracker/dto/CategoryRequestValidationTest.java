package com.expensetracker.dto;

import com.expensetracker.entity.TransactionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validCategoryRequest_shouldPass() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Groceries");
        request.setType(TransactionType.EXPENSE);
        request.setColor("#FF6B6B");

        var violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void emptyName_shouldFail() {
        CategoryRequest request = new CategoryRequest();
        request.setName("");
        request.setType(TransactionType.EXPENSE);

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullColor_shouldPass() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Groceries");
        request.setType(TransactionType.EXPENSE);

        var violations = validator.validate(request);
        assertEquals(0, violations.size());
    }
}
