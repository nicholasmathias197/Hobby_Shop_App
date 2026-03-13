package com.hobby.shop.service;

import com.hobby.shop.dto.request.CustomerUpdateRequest;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.dto.response.CustomerStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse getCustomerById(Long id);

    CustomerResponse getCustomerByEmail(String email);

    CustomerResponse updateCustomer(Long id, CustomerUpdateRequest request);

    void deleteCustomer(Long id);

    void changePassword(Long id, String oldPassword, String newPassword);

    // Admin operations
    Page<CustomerResponse> getAllCustomers(Pageable pageable);

    CustomerResponse toggleCustomerStatus(Long id, boolean enabled);

    void addRoleToCustomer(Long customerId, Long roleId);

    void removeRoleFromCustomer(Long customerId, Long roleId);

    long getInactiveCustomerCount();

    // Statistics
    CustomerStatisticsResponse getCustomerStatistics();

    long getTotalCustomerCount();  // keep for backward compatibility

    long getActiveCustomerCount(); // keep for backward compatibility
}