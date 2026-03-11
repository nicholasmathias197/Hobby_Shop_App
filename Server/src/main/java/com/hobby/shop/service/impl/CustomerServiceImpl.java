package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.CustomerUpdateRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.dto.response.CustomerStatisticsResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.CustomerMapper;
import com.hobby.shop.model.Customer;
import com.hobby.shop.model.Role;
import com.hobby.shop.repository.CustomerRepository;
import com.hobby.shop.repository.RoleRepository;
import com.hobby.shop.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the CustomerService interface.
 * Provides comprehensive customer management functionality including CRUD operations,
 * role management, password changes, and customer statistics.
 *
 * This service handles both customer data management and administrative functions
 * for user administration.
 *
 * @author Hobby Shop Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves a customer by their ID.
     *
     * @param id the customer ID to search for
     * @return CustomerResponse containing customer details
     * @throws ResourceNotFoundException if customer not found
     */
    @Override
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        return customerMapper.toResponse(customer);
    }

    /**
     * Retrieves a customer by their email address.
     *
     * @param email the email address to search for
     * @return CustomerResponse containing customer details
     * @throws ResourceNotFoundException if customer not found
     */
    @Override
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with email: " + email));

        return customerMapper.toResponse(customer);
    }

    /**
     * Updates customer information.
     *
     * Process flow:
     * 1. Finds the customer by ID
     * 2. Validates email uniqueness if being changed
     * 3. Updates only the provided fields
     * 4. Saves the updated customer
     *
     * @param id the ID of the customer to update
     * @param request the update request containing fields to update
     * @return CustomerResponse with updated information
     * @throws ResourceNotFoundException if customer not found
     * @throws BadRequestException if new email is already in use
     */
    @Override
    public CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request) {
        log.info("Updating customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Check if email is being changed and if it's already taken
        if (!customer.getEmail().equals(request.getEmail()) &&
                customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email " + request.getEmail() + " is already in use");
        }

        // Update fields conditionally (only if provided)
        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getPhone() != null) customer.setPhone(request.getPhone());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getCity() != null) customer.setCity(request.getCity());
        if (request.getPostalCode() != null) customer.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null) customer.setCountry(request.getCountry());

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated successfully with ID: {}", updatedCustomer.getId());

        return customerMapper.toResponse(updatedCustomer);
    }

    /**
     * Soft-deletes a customer by disabling their account.
     * The customer record remains in the database but is marked as inactive.
     *
     * @param id the ID of the customer to delete
     * @throws ResourceNotFoundException if customer not found
     */
    @Override
    public void deleteCustomer(Long id) {
        log.info("Deleting (disabling) customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setEnabled(false);
        customerRepository.save(customer);

        log.info("Customer disabled successfully with ID: {}", id);
    }

    /**
     * Changes a customer's password after verifying the old password.
     *
     * @param id the ID of the customer
     * @param oldPassword the current password for verification
     * @param newPassword the new password to set
     * @throws ResourceNotFoundException if customer not found
     * @throws BadRequestException if old password is incorrect
     */
    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Changing password for customer with ID: {}", id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        log.info("Password changed successfully for customer ID: {}", id);
    }

    /**
     * Retrieves a paginated list of all customers.
     *
     * @param pageable pagination information
     * @return Page of CustomerResponse objects
     */
    @Override
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponse);
    }

    /**
     * Enables or disables a customer account.
     *
     * @param id the ID of the customer
     * @param enabled true to enable, false to disable
     * @return CustomerResponse with updated status
     * @throws ResourceNotFoundException if customer not found
     */
    @Override
    public CustomerResponse toggleCustomerStatus(Long id, boolean enabled) {
        log.info("Toggling customer status: {} for ID: {}", enabled, id);

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));

        customer.setEnabled(enabled);
        Customer updatedCustomer = customerRepository.save(customer);

        log.info("Customer status updated to: {} for ID: {}", enabled, id);

        return customerMapper.toResponse(updatedCustomer);
    }

    /**
     * Adds a role to a customer.
     *
     * @param customerId the ID of the customer
     * @param roleId the ID of the role to add
     * @throws ResourceNotFoundException if customer or role not found
     */
    @Override
    public void addRoleToCustomer(Long customerId, Long roleId) {
        log.info("Adding role ID: {} to customer ID: {}", roleId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (customer.getRoles().add(role)) {
            customerRepository.save(customer);
            log.info("Role added successfully");
        } else {
            log.info("Customer already has this role");
        }
    }

    /**
     * Removes a role from a customer.
     *
     * @param customerId the ID of the customer
     * @param roleId the ID of the role to remove
     * @throws ResourceNotFoundException if customer or role not found
     */
    @Override
    public void removeRoleFromCustomer(Long customerId, Long roleId) {
        log.info("Removing role ID: {} from customer ID: {}", roleId, customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        if (customer.getRoles().remove(role)) {
            customerRepository.save(customer);
            log.info("Role removed successfully");
        } else {
            log.info("Customer does not have this role");
        }
    }

    /**
     * Gets the count of active (enabled) customers.
     *
     * @return the number of active customers
     */
    @Override
    public long getActiveCustomerCount() {
        return customerRepository.countByEnabledTrue();
    }

    /**
     * Gets the count of inactive (disabled) customers.
     *
     * @return the number of inactive customers
     */
    @Override
    public long getInactiveCustomerCount() {
        return customerRepository.countByEnabledFalse();
    }

    /**
     * Gets comprehensive customer statistics including total, active, and inactive counts.
     *
     * @return CustomerStatisticsResponse containing all statistics
     */
    @Override
    public CustomerStatisticsResponse getCustomerStatistics() {
        long total = customerRepository.count();
        long enabled = customerRepository.countByEnabledTrue();
        long disabled = customerRepository.countByEnabledFalse();

        log.info("Customer statistics - Total: {}, Active (enabled): {}, Inactive (disabled): {}",
                total, enabled, disabled);

        return new CustomerStatisticsResponse(total, enabled, disabled);
    }

    /**
     * Gets the total number of customers (including both active and inactive).
     *
     * @return the total customer count
     */
    @Override
    public long getTotalCustomerCount() {
        return customerRepository.count();
    }
}