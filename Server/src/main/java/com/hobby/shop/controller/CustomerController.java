package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CustomerUpdateRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.service.CustomerService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final SecurityUtils securityUtils;

    // Customer profile endpoints
    @GetMapping("/profile")
    public ResponseEntity<CustomerResponse> getCurrentUserProfile() {
        String email = securityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<CustomerResponse> updateCurrentUserProfile(
            @Valid @RequestBody CustomerUpdateRequest request) {
        String email = securityUtils.getCurrentUserEmail();
        CustomerResponse customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customerService.updateCustomer(customer.getId(), request));
    }

    @PostMapping("/profile/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        String email = securityUtils.getCurrentUserEmail();
        CustomerResponse customer = customerService.getCustomerByEmail(email);
        customerService.changePassword(customer.getId(), oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> toggleCustomerStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(customerService.toggleCustomerStatus(id, enabled));
    }

    @PostMapping("/{customerId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addRoleToCustomer(
            @PathVariable Long customerId,
            @PathVariable Long roleId) {
        customerService.addRoleToCustomer(customerId, roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{customerId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRoleFromCustomer(
            @PathVariable Long customerId,
            @PathVariable Long roleId) {
        customerService.removeRoleFromCustomer(customerId, roleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics/total")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getTotalCustomerCount() {
        return ResponseEntity.ok(customerService.getTotalCustomerCount());
    }

    @GetMapping("/statistics/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveCustomerCount() {
        return ResponseEntity.ok(customerService.getActiveCustomerCount());
    }
}