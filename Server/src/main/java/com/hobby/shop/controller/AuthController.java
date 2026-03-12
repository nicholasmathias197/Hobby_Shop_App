package com.hobby.shop.controller;

import com.hobby.shop.dto.request.LoginRequest;
import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.response.JwtResponse;
import com.hobby.shop.dto.response.RegisterResponse;
import com.hobby.shop.security.JwtUtils;
import com.hobby.shop.security.UserDetailsImpl;
import com.hobby.shop.service.AuthService;
import com.hobby.shop.service.CartService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;
    private final SecurityUtils securityUtils;
    private final CartService cartService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionIdHeader) {

        log.info("Login attempt for email: {}", loginRequest.getEmail());

        // Determine session ID from either query param or header
        String finalSessionId = sessionId;
        if (finalSessionId == null || finalSessionId.isEmpty()) {
            finalSessionId = sessionIdHeader;
        }

        log.info("Session ID from request: {}", finalSessionId);

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

        String firstName = userDetails.getFirstName();
        String lastName = userDetails.getLastName();

        // CRITICAL: Merge guest cart with user cart if sessionId is provided
        if (finalSessionId != null && !finalSessionId.isEmpty()) {
            try {
                log.info("🔀 Merging cart for user: {} with session: {}", loginRequest.getEmail(), finalSessionId);
                cartService.mergeCarts(loginRequest.getEmail(), finalSessionId);
                log.info("✅ Cart merged successfully");
            } catch (Exception e) {
                log.error("❌ Failed to merge cart: {}", e.getMessage(), e);
                // Continue with login even if cart merge fails
            }
        } else {
            log.info("No session ID provided, skipping cart merge");
        }

        JwtResponse response = new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getEmail(),
                firstName,
                lastName,
                roles
        );

        log.info("Login successful for user: {}", loginRequest.getEmail());
        return ResponseEntity.ok(response);
    }

    // In your AuthController.java
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionIdHeader) {

        log.info("Registration attempt for email: {}", request.getEmail());

        // Determine session ID from either query param or header
        String finalSessionId = sessionId;
        if (finalSessionId == null || finalSessionId.isEmpty()) {
            finalSessionId = sessionIdHeader;
        }

        log.info("Session ID from request: {}", finalSessionId);

        // Register the user and get token
        RegisterResponse response = authService.registerUser(request);
        log.info("User registered successfully with ID: {}", response.getId());

        // CRITICAL: Merge guest cart with new user cart if sessionId is provided
        if (finalSessionId != null && !finalSessionId.isEmpty()) {
            try {
                log.info("🔀 Merging cart for new user: {} with session: {}", request.getEmail(), finalSessionId);
                cartService.mergeCarts(request.getEmail(), finalSessionId);
                log.info("✅ Cart merged successfully for new user");
            } catch (Exception e) {
                log.error("❌ Failed to merge cart: {}", e.getMessage(), e);
                // Continue with registration even if cart merge fails
            }
        } else {
            log.info("No session ID provided, skipping cart merge");
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}