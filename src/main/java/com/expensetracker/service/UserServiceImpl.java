package com.expensetracker.service;

import com.expensetracker.dto.PasswordChangeRequest;
import com.expensetracker.dto.UserProfileResponse;
import com.expensetracker.dto.UserUpdateRequest;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BusinessRuleException;
import com.expensetracker.exception.ConflictException;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.CurrentUserService;
import com.expensetracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = currentUserService.getCurrentUser();
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(UserUpdateRequest request) {
        User user = currentUserService.getCurrentUser();

//        // Update fields if provided
//        if (request.getFirstName() != null) {
//            user.setFirstName(request.getFirstName());
//        }
//        if (request.getLastName() != null) {
//            user.setLastName(request.getLastName());
//        }
//        if (request.getEmail() != null) {
//            // TODO: Check if email already exists for another user
//            user.setEmail(request.getEmail());
//        }
        if (request.getEmail() != null &&
                userRepository.existsByEmail(request.getEmail()) &&
                !request.getEmail().equals(user.getEmail())) {
            throw new ConflictException("Email already in use");
        }

        User updated = userRepository.save(user);
        return UserProfileResponse.builder()
                .id(updated.getId())
                .email(updated.getEmail())
                .firstName(updated.getFirstName())
                .lastName(updated.getLastName())
                .createdAt(updated.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeRequest request) {
        User user = currentUserService.getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessRuleException("Current password is incorrect");
        }


        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
