package com.expensetracker.controller;

import com.expensetracker.dto.PasswordChangeRequest;
import com.expensetracker.dto.UserProfileResponse;
import com.expensetracker.dto.UserUpdateRequest;
import com.expensetracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@Valid @RequestBody UserUpdateRequest request) {
        UserProfileResponse profile = userService.updateUserProfile(request);
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
