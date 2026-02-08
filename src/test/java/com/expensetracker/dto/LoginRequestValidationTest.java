package com.expensetracker.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validLoginRequest_shouldPass() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");

        var violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void emptyEmail_shouldFail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("Password123!");

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullPassword_shouldFail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void emailTooLong_shouldFail() {
        LoginRequest request = new LoginRequest();
        request.setEmail("a".repeat(100) + "@example.com"); // Very long
        request.setPassword("Password123!");

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
