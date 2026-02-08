package com.expensetracker.service;

import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_shouldSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setFirstName("Jane");
        request.setLastName("Doe");

        Session session = Session.getDefaultInstance(new java.util.Properties());
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage(session));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });
        var response = authService.register(request);

        assertNotNull(response);
        assertTrue(response.containsKey("message"));
        assertTrue(response.get("message").toString().contains("successful"));
        verify(userRepository, times(1)).save(any(User.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void registerExistingEmail_shouldThrowException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));

        assertTrue(exception.getMessage().contains("already registered"));
        verify(userRepository, never()).save(any());
    }
}
