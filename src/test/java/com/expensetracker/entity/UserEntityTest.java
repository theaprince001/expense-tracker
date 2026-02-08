package com.expensetracker.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserEntityTest {

    @Test
    void userCreation_shouldWork() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreatedAt(LocalDateTime.now());

        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void userToString_shouldNotThrow() {
        User user = new User();
        user.setEmail("test@example.com");

        // Just ensure toString doesn't crash
        assertNotNull(user.toString());
    }
}