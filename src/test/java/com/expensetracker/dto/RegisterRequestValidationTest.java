package com.expensetracker.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void validRegisterRequest_shouldPass() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setFirstName("John");
        request.setLastName("Doe");

        var violations = validator.validate(request);
        assertEquals(0, violations.size());
    }

    @Test
    void invalidEmail_shouldFail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email"); // Invalid
        request.setPassword("Password123!");
        request.setFirstName("John");
        request.setLastName("Doe");

        var violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}