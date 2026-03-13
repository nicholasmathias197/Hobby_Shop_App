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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest extends BaseServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private Customer customer;
    private CustomerResponse customerResponse;
    private Role userRole;
    private Role adminRole;
    private final Long customerId = 1L;
    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        customer = createTestCustomer(customerId, email);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerResponse = new CustomerResponse();
        customerResponse.setId(customerId);
        customerResponse.setEmail(email);
        customerResponse.setFirstName("John");
        customerResponse.setLastName("Doe");
        customerResponse.setPhone("123-456-7890");
        customerResponse.setAddress("123 Test St");
        customerResponse.setCity("Test City");
        customerResponse.setPostalCode("12345");
        customerResponse.setCountry("Test Country");
        customerResponse.setEnabled(true);

        userRole = createTestRole(1L, "ROLE_USER");
        adminRole = createTestRole(2L, "ROLE_ADMIN");
    }

    // ==================== GET CUSTOMER TESTS ====================

    @Test
    void getCustomerById_Success() {
        // Arrange
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        // Act
        CustomerResponse result = customerService.getCustomerById(customerId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(customerId);
        assertThat(result.getEmail()).isEqualTo(email);

        verify(customerRepository).findById(customerId);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void getCustomerById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerById(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + invalidId);
    }

    @Test
    void getCustomerByEmail_Success() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        // Act
        CustomerResponse result = customerService.getCustomerByEmail(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);

        verify(customerRepository).findByEmail(email);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    void getCustomerByEmail_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String invalidEmail = "nonexistent@example.com";
        when(customerRepository.findByEmail(invalidEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerByEmail(invalidEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with email: " + invalidEmail);
    }

    // ==================== UPDATE CUSTOMER TESTS ====================

    @Test
    void updateCustomer_AllFields_Success() {
        // Arrange
        CustomerUpdateRequest updateRequest = createTestCustomerUpdateRequest();

        Customer updatedCustomer = createTestCustomer(customerId, updateRequest.getEmail());
        updatedCustomer.setFirstName(updateRequest.getFirstName());
        updatedCustomer.setLastName(updateRequest.getLastName());
        updatedCustomer.setPhone(updateRequest.getPhone());
        updatedCustomer.setAddress(updateRequest.getAddress());
        updatedCustomer.setCity(updateRequest.getCity());
        updatedCustomer.setPostalCode(updateRequest.getPostalCode());
        updatedCustomer.setCountry(updateRequest.getCountry());

        CustomerResponse updatedResponse = new CustomerResponse();
        updatedResponse.setId(customerId);
        updatedResponse.setEmail(updateRequest.getEmail());
        updatedResponse.setFirstName(updateRequest.getFirstName());
        updatedResponse.setLastName(updateRequest.getLastName());
        updatedResponse.setPhone(updateRequest.getPhone());
        updatedResponse.setAddress(updateRequest.getAddress());
        updatedResponse.setCity(updateRequest.getCity());
        updatedResponse.setPostalCode(updateRequest.getPostalCode());
        updatedResponse.setCountry(updateRequest.getCountry());
        updatedResponse.setEnabled(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);
        when(customerMapper.toResponse(updatedCustomer)).thenReturn(updatedResponse);

        // Act
        CustomerResponse result = customerService.updateCustomer(customerId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(updateRequest.getEmail());
        assertThat(result.getFirstName()).isEqualTo(updateRequest.getFirstName());
        assertThat(result.getLastName()).isEqualTo(updateRequest.getLastName());

        verify(customerRepository).findById(customerId);
        verify(customerRepository).existsByEmail(updateRequest.getEmail());
        verify(customerRepository).save(any(Customer.class));
        verify(customerMapper).toResponse(updatedCustomer);
    }

    @Test
    void updateCustomer_PartialFields_Success() {
        // Arrange
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setFirstName("Jane");
        updateRequest.setLastName("Smith");

        Customer partiallyUpdatedCustomer = createTestCustomer(customerId, email);
        partiallyUpdatedCustomer.setFirstName("Jane");
        partiallyUpdatedCustomer.setLastName("Smith");

        CustomerResponse partiallyUpdatedResponse = new CustomerResponse();
        partiallyUpdatedResponse.setId(customerId);
        partiallyUpdatedResponse.setEmail(email);
        partiallyUpdatedResponse.setFirstName("Jane");
        partiallyUpdatedResponse.setLastName("Smith");
        partiallyUpdatedResponse.setEnabled(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(partiallyUpdatedCustomer);
        when(customerMapper.toResponse(partiallyUpdatedCustomer)).thenReturn(partiallyUpdatedResponse);

        // Act
        CustomerResponse result = customerService.updateCustomer(customerId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");

        verify(customerRepository).findById(customerId);
        verify(customerRepository, never()).existsByEmail(anyString());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void updateCustomer_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        CustomerUpdateRequest updateRequest = createTestCustomerUpdateRequest();

        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(invalidId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + invalidId);
    }

    @Test
    void updateCustomer_EmailAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        CustomerUpdateRequest updateRequest = createTestCustomerUpdateRequest();
        updateRequest.setEmail("existing@example.com");

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.updateCustomer(customerId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email existing@example.com is already in use");

        verify(customerRepository).findById(customerId);
        verify(customerRepository).existsByEmail("existing@example.com");
        verify(customerRepository, never()).save(any());
    }

    // ==================== DELETE CUSTOMER TESTS ====================

    @Test
    void deleteCustomer_Success() {
        // Arrange
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        customerService.deleteCustomer(customerId);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(customer);
        assertThat(customer.getEnabled()).isFalse();
    }

    @Test
    void deleteCustomer_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.deleteCustomer(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + invalidId);
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    void changePassword_Success() {
        // Arrange
        String oldPassword = "oldPass123";
        String newPassword = "newPass456";
        String encodedNewPassword = "encodedNewPass456";

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(oldPassword, customer.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        customerService.changePassword(customerId, oldPassword, newPassword);

        // Assert
        verify(customerRepository).findById(customerId);

        // Verify matches was called with the old password and the CURRENT encoded password
        verify(passwordEncoder).matches(oldPassword, "encodedPassword");

        // Verify encode was called with the new password
        verify(passwordEncoder).encode(newPassword);

        // Verify the customer was saved with the new encoded password
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    void changePassword_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.changePassword(invalidId, "old", "new"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + invalidId);
    }

    @Test
    void changePassword_IncorrectOldPassword_ThrowsBadRequestException() {
        // Arrange
        String oldPassword = "wrongPass";

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(oldPassword, customer.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> customerService.changePassword(customerId, oldPassword, "newPass"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(customerRepository).findById(customerId);
        verify(passwordEncoder).matches(oldPassword, customer.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(customerRepository, never()).save(any());
    }

    // ==================== GET ALL CUSTOMERS TESTS ====================

    @Test
    void getAllCustomers_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Customer> customers = Arrays.asList(
                customer,
                createTestCustomer(2L, "test2@example.com"),
                createTestCustomer(3L, "test3@example.com")
        );
        Page<Customer> customerPage = new PageImpl<>(customers, pageable, customers.size());

        when(customerRepository.findAll(pageable)).thenReturn(customerPage);
        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerResponse);

        // Act
        Page<CustomerResponse> result = customerService.getAllCustomers(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        verify(customerRepository).findAll(pageable);
        verify(customerMapper, times(3)).toResponse(any(Customer.class));
    }

    // ==================== TOGGLE CUSTOMER STATUS TESTS ====================

    @Test
    void toggleCustomerStatus_Enable_Success() {
        // Arrange
        customer.setEnabled(false);

        Customer enabledCustomer = createTestCustomer(customerId, email);
        enabledCustomer.setEnabled(true);

        CustomerResponse enabledResponse = new CustomerResponse();
        enabledResponse.setId(customerId);
        enabledResponse.setEnabled(true);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(enabledCustomer);
        when(customerMapper.toResponse(enabledCustomer)).thenReturn(enabledResponse);

        // Act
        CustomerResponse result = customerService.toggleCustomerStatus(customerId, true);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEnabled()).isTrue();
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(customer);
    }

    @Test
    void toggleCustomerStatus_Disable_Success() {
        // Arrange
        customer.setEnabled(true);

        Customer disabledCustomer = createTestCustomer(customerId, email);
        disabledCustomer.setEnabled(false);

        CustomerResponse disabledResponse = new CustomerResponse();
        disabledResponse.setId(customerId);
        disabledResponse.setEnabled(false);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(disabledCustomer);
        when(customerMapper.toResponse(disabledCustomer)).thenReturn(disabledResponse);

        // Act
        CustomerResponse result = customerService.toggleCustomerStatus(customerId, false);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEnabled()).isFalse();
        verify(customerRepository).findById(customerId);
        verify(customerRepository).save(customer);
    }

    // ==================== ROLE MANAGEMENT TESTS ====================

    @Test
    void addRoleToCustomer_Success() {
        // Arrange
        Long roleId = 2L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        // Act
        customerService.addRoleToCustomer(customerId, roleId);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(roleRepository).findById(roleId);
        verify(customerRepository).save(customer);
        assertThat(customer.getRoles()).contains(adminRole);
    }

    @Test
    void addRoleToCustomer_RoleAlreadyExists_DoesNotAddDuplicate() {
        // Arrange
        Long roleId = 1L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(userRole));

        int originalSize = customer.getRoles().size();

        // Act
        customerService.addRoleToCustomer(customerId, roleId);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(roleRepository).findById(roleId);
        // Verify that save is NOT called when role already exists
        verify(customerRepository, never()).save(any(Customer.class));
        assertThat(customer.getRoles()).hasSize(originalSize);
    }

    @Test
    void addRoleToCustomer_CustomerNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.addRoleToCustomer(invalidId, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + invalidId);
    }

    @Test
    void addRoleToCustomer_RoleNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidRoleId = 999L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(roleRepository.findById(invalidRoleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.addRoleToCustomer(customerId, invalidRoleId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role not found with id: " + invalidRoleId);
    }

    @Test
    void removeRoleFromCustomer_Success() {
        // Arrange
        customer.getRoles().add(adminRole);

        Long roleId = 2L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        int originalSize = customer.getRoles().size();

        // Act
        customerService.removeRoleFromCustomer(customerId, roleId);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(roleRepository).findById(roleId);
        verify(customerRepository).save(customer);
        assertThat(customer.getRoles()).hasSize(originalSize - 1);
        assertThat(customer.getRoles()).doesNotContain(adminRole);
    }

    @Test
    void removeRoleFromCustomer_RoleNotPresent_DoesNothing() {
        // Arrange
        Long roleId = 2L;
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(adminRole));

        int originalSize = customer.getRoles().size();

        // Act
        customerService.removeRoleFromCustomer(customerId, roleId);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(roleRepository).findById(roleId);
        // Verify that save is NOT called when role doesn't exist
        verify(customerRepository, never()).save(any(Customer.class));
        assertThat(customer.getRoles()).hasSize(originalSize);
    }

    @Test
    void removeRoleFromCustomer_CustomerNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(customerRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.removeRoleFromCustomer(invalidId, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with id: " + invalidId);
    }

    // ==================== STATISTICS TESTS ====================

    @Test
    void getActiveCustomerCount_Success() {
        // Arrange
        when(customerRepository.countByEnabledTrue()).thenReturn(5L);

        // Act
        long result = customerService.getActiveCustomerCount();

        // Assert
        assertThat(result).isEqualTo(5L);
        verify(customerRepository).countByEnabledTrue();
    }

    @Test
    void getInactiveCustomerCount_Success() {
        // Arrange
        when(customerRepository.countByEnabledFalse()).thenReturn(3L);

        // Act
        long result = customerService.getInactiveCustomerCount();

        // Assert
        assertThat(result).isEqualTo(3L);
        verify(customerRepository).countByEnabledFalse();
    }

    @Test
    void getTotalCustomerCount_Success() {
        // Arrange
        when(customerRepository.count()).thenReturn(8L);

        // Act
        long result = customerService.getTotalCustomerCount();

        // Assert
        assertThat(result).isEqualTo(8L);
        verify(customerRepository).count();
    }

    @Test
    void getCustomerStatistics_Success() {
        // Arrange
        when(customerRepository.count()).thenReturn(10L);
        when(customerRepository.countByEnabledTrue()).thenReturn(7L);
        when(customerRepository.countByEnabledFalse()).thenReturn(3L);

        // Act
        CustomerStatisticsResponse result = customerService.getCustomerStatistics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalCustomers()).isEqualTo(10L);
        assertThat(result.getActiveCustomers()).isEqualTo(7L);
        assertThat(result.getInactiveCustomers()).isEqualTo(3L);

        verify(customerRepository).count();
        verify(customerRepository).countByEnabledTrue();
        verify(customerRepository).countByEnabledFalse();
    }
}