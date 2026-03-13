package com.hobby.shop.service;

import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.dto.response.RegisterResponse; // Add this import

public interface AuthService {

    RegisterResponse registerUser(RegisterRequest request); // Changed return type

    CustomerResponse registerAdmin(RegisterRequest request);

    boolean verifyEmail(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);
}