package com.hobby.shop.service;

import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.response.CustomerResponse;

public interface AuthService {

    CustomerResponse registerUser(RegisterRequest request);
    CustomerResponse registerAdmin(RegisterRequest request);
    boolean verifyEmail(String token);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void changePassword(Long userId, String oldPassword, String newPassword);
}