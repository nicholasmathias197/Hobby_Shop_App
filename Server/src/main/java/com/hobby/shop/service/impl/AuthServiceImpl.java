package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.dto.response.RegisterResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.CustomerMapper;
import com.hobby.shop.model.Customer;
import com.hobby.shop.model.Role;
import com.hobby.shop.repository.CustomerRepository;
import com.hobby.shop.repository.RoleRepository;
import com.hobby.shop.security.JwtUtils;
import com.hobby.shop.security.UserDetailsImpl;
import com.hobby.shop.service.AuthService;
import com.hobby.shop.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the AuthService interface.
 * Handles all authentication-related operations including user registration,
 * admin registration, password management, and email verification.
 *
 * This service manages user creation, role assignment, and provides JWT
 * token generation upon successful registration.
 *
 * @author Hobby Shop Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Registers a new user in the system.
     *
     * Process flow:
     * 1. Validates that the email is not already registered
     * 2. Creates a new customer entity from the request
     * 3. Encodes the password for security
     * 4. Assigns the default USER role
     * 5. Saves the customer to the database
     * 6. Automatically authenticates the user and generates a JWT token
     *
     * @param request the registration request containing user details
     * @return RegisterResponse containing user details and JWT token
     * @throws BadRequestException if email is already registered
     */
    @Override
    public RegisterResponse registerUser(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Check if email already exists in the system
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email " + request.getEmail() + " is already registered");
        }

        // Create new customer from request
        Customer customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        // Assign default USER role
        Role userRole = roleRepository.findByName(AppConstants.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        customer.setRoles(roles);

        // Save customer to database
        Customer savedCustomer = customerRepository.save(customer);
        log.info("User registered successfully with ID: {}", savedCustomer.getId());

        // Automatically authenticate the user after registration
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roleNames = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Return response with token
        return new RegisterResponse(
                savedCustomer.getId(),
                savedCustomer.getEmail(),
                savedCustomer.getFirstName(),
                savedCustomer.getLastName(),
                jwt,
                roleNames,
                savedCustomer.getCreatedAt()
        );
    }

    /**
     * Registers a new admin user in the system.
     *
     * Similar to user registration but assigns both USER and ADMIN roles.
     * This method is typically restricted to existing administrators.
     *
     * @param request the registration request containing admin details
     * @return CustomerResponse containing admin details
     * @throws BadRequestException if email is already registered
     */
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

    /**
     * Verifies a user's email address using a token.
     *
     * @param token the verification token sent to the user's email
     * @return true if verification successful, false otherwise
     */
    @Override
    public boolean verifyEmail(String token) {
        // Implementation for email verification
        log.info("Verifying email with token: {}", token);
        // TODO: Implement actual email verification logic
        return true;
    }

    /**
     * Initiates the password reset process for a user.
     *
     * @param email the email address of the user requesting password reset
     * @throws ResourceNotFoundException if no user found with the given email
     */
    @Override
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        log.info("Password reset email would be sent to: {}", email);
        // TODO: Implement actual password reset email sending
    }

    /**
     * Resets a user's password using a reset token.
     *
     * @param token the password reset token
     * @param newPassword the new password to set
     */
    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token: {}", token);
        log.info("Password would be reset for token: {}", token);
        // TODO: Implement actual password reset logic
    }

    /**
     * Changes a user's password when they know their current password.
     *
     * @param userId the ID of the user changing password
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @throws ResourceNotFoundException if user not found
     * @throws BadRequestException if old password is incorrect
     */
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