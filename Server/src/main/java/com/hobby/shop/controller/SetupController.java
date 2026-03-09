package com.hobby.shop.controller;

import com.hobby.shop.model.Role;
import com.hobby.shop.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
public class SetupController {

    private final RoleRepository roleRepository;

    // ============= INITIALIZATION ENDPOINTS =============

    /**
     * Initialize default roles (USER and ADMIN)
     * POST /api/setup/init-roles
     * Should be called once during application setup or deployment
     * Creates roles if they don't already exist
     * @return Success message
     */
    @PostMapping("/init-roles")
    public ResponseEntity<String> initializeRoles() {

        // Create USER role if it doesn't exist
        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = new Role();
            userRole.setName("USER");
            roleRepository.save(userRole);
            System.out.println("USER role created");
        }

        // Create ADMIN role if it doesn't exist
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            roleRepository.save(adminRole);
            System.out.println("ADMIN role created");
        }

        return ResponseEntity.ok("Roles initialized successfully");
    }
}