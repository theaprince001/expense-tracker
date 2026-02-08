package com.expensetracker.service;

import com.expensetracker.dto.AuthResponse;
import com.expensetracker.dto.LoginRequest;
import com.expensetracker.dto.RegisterRequest;
import com.expensetracker.entity.Role;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BusinessRuleException;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    @Transactional
    public Map<String, String> register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ROLE_USER)
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .alertThreshold(80)
                .build();

        user.setActivationToken(UUID.randomUUID().toString());
        user.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        userRepository.save(user);
        sendActivationEmail(user);

        return Map.of(
                "message", "Registration successful. Check email to activate account",
                "email", user.getEmail()
        );
    }

    private void sendActivationEmail(User user) {
        String activationUrl = "http://localhost:8080/api/auth/activate?token=" + user.getActivationToken();

        String subject = "Activate Your Expense Tracker Account";
        String body = """
            Hi %s,
            
            Thank you for registering! Click below to activate:
            
            %s
            
            Link expires in 24 hours.
            
            Best,
            Expense Tracker
            """.formatted(user.getFirstName(), activationUrl);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Expense Tracker <noreply@expensetracker.com>");
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }


    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BusinessRuleException("Account not activated");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Transactional
    public Map<String, String> activateAccount(String token) {

        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid activation token"));

        if (user.getActivationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Activation token expired");
        }

        user.setIsActive(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiry(null);

        return Map.of("message", "Account activated successfully");
    }



    @Transactional
    public Map<String, String> resendActivation(String email) {

        User user = userRepository.findByEmailAndIsActiveFalse(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No pending account for this email"));

        user.setActivationToken(UUID.randomUUID().toString());
        user.setActivationTokenExpiry(LocalDateTime.now().plusHours(24));

        sendActivationEmail(user);
        return Map.of("message", "Activation email resent");
    }

}

