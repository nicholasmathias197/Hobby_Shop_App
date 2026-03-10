package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CustomerUpdateRequest;
import com.hobby.shop.dto.request.RoleAssignmentRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.dto.response.CustomerStatisticsResponse;
import com.hobby.shop.exception.BadRequestException;
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

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final SecurityUtils securityUtils;

    // ============= CUSTOMER PROFILE ENDPOINTS (Self-Service) =============

    /**
     * Get current authenticated user's profile
     * GET /api/customers/profile
     *
     * @return Customer profile response
     */
    @GetMapping("/profile")
    public ResponseEntity<CustomerResponse> getCurrentUserProfile() {
        String email = securityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    /**
     * Update current authenticated user's profile
     * PUT /api/customers/profile
     *
     * @param request Profile update request
     * @return Updated customer response
     */
    @PutMapping("/profile")
    public ResponseEntity<CustomerResponse> updateCurrentUserProfile(
            @Valid @RequestBody CustomerUpdateRequest request) {
        String email = securityUtils.getCurrentUserEmail();
        CustomerResponse customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customerService.updateCustomer(customer.getId(), request));
    }

    /**
     * Change password for current authenticated user
     * POST /api/customers/profile/change-password?oldPassword={old}&newPassword={new}
     *
     * @param oldPassword Current password for verification
     * @param newPassword New password to set
     * @return OK response on success
     */
    @PostMapping("/profile/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        String email = securityUtils.getCurrentUserEmail();
        CustomerResponse customer = customerService.getCustomerByEmail(email);
        customerService.changePassword(customer.getId(), oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }

    // ============= ADMIN ENDPOINTS =============

    /**
     * Get paginated list of all customers (admin only)
     * GET /api/customers
     *
     * @param pageable Pagination information (default: size=20, sort by createdAt DESC)
     * @return Paginated list of customer responses
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    /**
     * Get customer by ID (admin only)
     * GET /api/customers/{id}
     *
     * @param id Customer ID
     * @return Customer response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    /**
     * Get customer by email (admin only)
     * GET /api/customers/email/{email}
     *
     * @param email Customer email
     * @return Customer response
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    /**
     * Update any customer by ID (admin only)
     * PUT /api/customers/{id}
     *
     * @param id      Customer ID
     * @param request Customer update request
     * @return Updated customer response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    /**
     * Delete a customer (soft delete) (admin only)
     * DELETE /api/customers/{id}
     *
     * @param id Customer ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Enable or disable a customer account (admin only)
     * PUT /api/customers/{id}/toggle-status?enabled={true/false}
     *
     * @param id        Customer ID
     * @param statusMap Desired account status
     * @return Updated customer response
     */
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> toggleCustomerStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> statusMap) {

        Boolean enabled = statusMap.get("enabled");
        if (enabled == null) {
            throw new BadRequestException("enabled field is required");
        }

        return ResponseEntity.ok(customerService.toggleCustomerStatus(id, enabled));
    }

    /**
     * Add a role to a customer (admin only)
     * POST /api/customers/{customerId}/roles/{roleId}
     *
     * @param customerId Customer ID
     * @param roleMap    Role ID to add
     * @return OK response
     */
    @PostMapping("/{customerId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addRoleToCustomer(
            @PathVariable Long customerId,
            @RequestBody Map<String, Long> roleMap) {

        Long roleId = roleMap.get("roleId");
        if (roleId == null) {
            throw new BadRequestException("roleId is required");
        }

        customerService.addRoleToCustomer(customerId, roleId);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove a role from a customer (admin only)
     * DELETE /api/customers/{customerId}/roles/{roleId}
     *
     * @param customerId Customer ID
     * @param request    Role ID to remove
     * @return OK response
     */
    @DeleteMapping("/{customerId}/roles")  // No {roleId} in path
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRoleFromCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody RoleAssignmentRequest request) {  // Role ID from body
        customerService.removeRoleFromCustomer(customerId, request.getRoleId());
        return ResponseEntity.ok().build();
    }

    // ============= STATISTICS ENDPOINTS (Admin Only) =============

    /**
     * Get number of active customers (enabled = true) (admin only)
     * GET /api/customers/statistics/active
     */
    @GetMapping("/statistics/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveCustomerCount() {
        return ResponseEntity.ok(customerService.getActiveCustomerCount());
    }

    /**
     * Get number of inactive customers (enabled = false) (admin only)
     * GET /api/customers/statistics/inactive
     */
    @GetMapping("/statistics/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getInactiveCustomerCount() {
        return ResponseEntity.ok(customerService.getInactiveCustomerCount());
    }

    /**
     * Get comprehensive customer statistics (admin only)
     * GET /api/customers/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerStatisticsResponse> getCustomerStatistics() {
        return ResponseEntity.ok(customerService.getCustomerStatistics());
    }
}