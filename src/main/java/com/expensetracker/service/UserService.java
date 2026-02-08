package com.expensetracker.service;

import com.expensetracker.dto.PasswordChangeRequest;
import com.expensetracker.dto.UserProfileResponse;
import com.expensetracker.dto.UserUpdateRequest;

public interface UserService {
    UserProfileResponse getCurrentUserProfile();
    UserProfileResponse updateUserProfile(UserUpdateRequest request);
    void changePassword(PasswordChangeRequest request);
}
