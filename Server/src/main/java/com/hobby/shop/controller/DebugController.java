package com.hobby.shop.controller;

import com.hobby.shop.model.Role;
import com.hobby.shop.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final RoleRepository roleRepository;

    /**
     * Debug endpoint to check role configuration (Admin only)
     */
    @GetMapping("/check-roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkRoles() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Debug check-roles endpoint called");

            // Check if roleRepository is injected properly
            if (roleRepository == null) {
                log.error("roleRepository is null");
                response.put("error", "roleRepository is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Try to count roles first (simpler query)
            long totalRoles;
            try {
                totalRoles = roleRepository.count();
                log.info("Successfully counted roles: {}", totalRoles);
            } catch (Exception e) {
                log.error("Error counting roles: {}", e.getMessage(), e);
                response.put("error", "Failed to count roles: " + e.getMessage());
                response.put("exception", e.getClass().getName());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Try to fetch all roles
            List<Role> allRoles;
            try {
                allRoles = roleRepository.findAll();
                log.info("Successfully fetched {} roles", allRoles.size());
            } catch (Exception e) {
                log.error("Error fetching roles: {}", e.getMessage(), e);
                response.put("error", "Failed to fetch roles: " + e.getMessage());
                response.put("exception", e.getClass().getName());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // Build successful response
            response.put("totalRoles", totalRoles);
            response.put("roles", allRoles);
            response.put("userRoleExists", roleRepository.findByName("USER").isPresent());
            response.put("adminRoleExists", roleRepository.findByName("ADMIN").isPresent());
            response.put("status", "SUCCESS");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error in check-roles: {}", e.getMessage(), e);
            response.put("error", "Unexpected error: " + e.getMessage());
            response.put("exception", e.getClass().getName());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Simple test endpoint that doesn't use database
     */
    @GetMapping("/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "pong");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    /**
     * Check database connection specifically
     */
    @GetMapping("/db-check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Test database connectivity
            boolean databaseConnected = false;
            String databaseError = null;

            try {
                // Just try to execute a simple query
                roleRepository.count();
                databaseConnected = true;
            } catch (Exception e) {
                databaseError = e.getMessage();
                log.error("Database connection test failed: {}", e.getMessage());
            }

            response.put("databaseConnected", databaseConnected);
            response.put("databaseError", databaseError);
            response.put("repositoryInitialized", roleRepository != null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error in db-check: {}", e.getMessage(), e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}