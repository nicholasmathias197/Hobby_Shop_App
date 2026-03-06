package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.CustomerMapper;
import com.hobby.shop.model.Customer;
import com.hobby.shop.model.Role;
import com.hobby.shop.repository.CustomerRepository;
import com.hobby.shop.repository.RoleRepository;
import com.hobby.shop.service.AuthService;
import com.hobby.shop.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponse registerUser(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email " + request.getEmail() + " is already registered");
        }

        // Create new customer
        Customer customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default USER role
        Role userRole = roleRepository.findByName(AppConstants.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        customer.setRoles(roles);

        Customer savedCustomer = customerRepository.save(customer);
        log.info("User registered successfully with ID: {}", savedCustomer.getId());

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse registerAdmin(RegisterRequest request) {
        log.info("Registering new admin with email: {}", request.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email " + request.getEmail() + " is already registered");
        }

        // Create new customer
        Customer customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign both USER and ADMIN roles
        Role userRole = roleRepository.findByName(AppConstants.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        Role adminRole = roleRepository.findByName(AppConstants.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        customer.setRoles(roles);

        Customer savedCustomer = customerRepository.save(customer);
        log.info("Admin registered successfully with ID: {}", savedCustomer.getId());

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public boolean verifyEmail(String token) {
        // Implementation for email verification
        // This would typically involve validating a token sent via email
        log.info("Verifying email with token: {}", token);

        // For now, just return true
        // In a real implementation, you would:
        // 1. Validate the token
        // 2. Find the user by token
        // 3. Set emailVerified to true
        // 4. Remove or expire the token

        return true;
    }

    @Override
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Generate reset token and send email
        // This would typically:
        // 1. Generate a secure random token
        // 2. Save token with expiration to user record or separate table
        // 3. Send email with reset link containing token

        log.info("Password reset email would be sent to: {}", email);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token: {}", token);

        // Validate token and find user
        // For now, just log
        // In a real implementation, you would:
        // 1. Validate the token and check expiration
        // 2. Find the user associated with the token
        // 3. Update their password
        // 4. Remove or expire the token

        log.info("Password would be reset for token: {}", token);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user ID: {}", userId);

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update to new password
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        log.info("Password changed successfully for user ID: {}", userId);
    }
}