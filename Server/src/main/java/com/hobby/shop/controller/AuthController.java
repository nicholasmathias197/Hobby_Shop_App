package com.hobby.shop.controller;

import com.hobby.shop.dto.request.LoginRequest;
import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.dto.response.JwtResponse;
import com.hobby.shop.security.JwtUtils;
import com.hobby.shop.security.UserDetailsImpl;
import com.hobby.shop.service.AuthService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final SecurityUtils securityUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // You'll need to get firstName and lastName from userDetails
        // Assuming UserDetailsImpl has these methods, or you might need to fetch from database
        String firstName = userDetails.getFirstName(); // Make sure this method exists
        String lastName = userDetails.getLastName();   // Make sure this method exists

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                firstName,
                lastName,
                roles
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.registerUser(request), HttpStatus.CREATED);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<CustomerResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.registerAdmin(request), HttpStatus.CREATED);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean verified = authService.verifyEmail(token);
        if (verified) {
            return ResponseEntity.ok("Email verified successfully");
        }
        return ResponseEntity.badRequest().body("Email verification failed");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        Long userId = securityUtils.getCurrentUserId();
        authService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }
}