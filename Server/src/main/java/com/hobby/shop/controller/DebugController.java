package com.hobby.shop.controller;

import com.hobby.shop.model.Role;
import com.hobby.shop.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final RoleRepository roleRepository;

    // ============= DEBUG ENDPOINTS =============

    /**
     * Debug endpoint to check role configuration
     * GET /api/debug/check-roles
     * Useful for troubleshooting authentication/authorization issues
     * @return Map containing role information and existence checks
     */
    @GetMapping("/check-roles")
    public ResponseEntity<Map<String, Object>> checkRoles() {
        Map<String, Object> response = new HashMap<>();

        List<Role> allRoles = roleRepository.findAll();
        response.put("totalRoles", allRoles.size());
        response.put("roles", allRoles);

        // Check specifically for USER role
        boolean userRoleExists = roleRepository.findByName("USER").isPresent();
        response.put("userRoleExists", userRoleExists);

        // Check specifically for ADMIN role
        boolean adminRoleExists = roleRepository.findByName("ADMIN").isPresent();
        response.put("adminRoleExists", adminRoleExists);

        return ResponseEntity.ok(response);
    }
}