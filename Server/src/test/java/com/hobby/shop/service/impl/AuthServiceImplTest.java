package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.RegisterRequest;
import com.hobby.shop.dto.response.RegisterResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.CustomerMapper;
import com.hobby.shop.model.Customer;
import com.hobby.shop.model.Role;
import com.hobby.shop.repository.CustomerRepository;
import com.hobby.shop.repository.RoleRepository;
import com.hobby.shop.security.JwtUtils;
import com.hobby.shop.security.UserDetailsImpl;
import com.hobby.shop.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest extends BaseServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private RegisterRequest registerRequest;
    private Customer customer;
    private Role userRole;
    private Authentication authentication;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");

        customer = createTestCustomer(1L, "test@example.com");
        customer.setPassword("encodedPassword");
        customer.setCreatedAt(LocalDateTime.now());

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");

        // Create UserDetailsImpl using constructor
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER"));

        userDetails = new UserDetailsImpl(
                1L,
                "test@example.com",
                "encodedPassword",
                "John",
                "Doe",
                authorities
        );

        authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(registerRequest)).thenReturn(customer);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(AppConstants.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token-123");

        // Act
        RegisterResponse response = authService.registerUser(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getRoles()).contains("ROLE_USER");

        verify(customerRepository).existsByEmail(registerRequest.getEmail());
        verify(customerMapper).toEntity(registerRequest);
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(roleRepository).findByName(AppConstants.ROLE_USER);
        verify(customerRepository).save(any(Customer.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(authentication);

        // Verify SecurityContext was set
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email test@example.com is already registered");

        verify(customerRepository).existsByEmail(registerRequest.getEmail());
        verify(customerMapper, never()).toEntity(any());
        verify(passwordEncoder, never()).encode(any());
        verify(roleRepository, never()).findByName(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerUser_RoleNotFound_ThrowsRuntimeException() {
        // Arrange
        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(registerRequest)).thenReturn(customer);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(AppConstants.ROLE_USER)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Default USER role not found");

        verify(customerRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName(AppConstants.ROLE_USER);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerAdmin_Success() {
        // Arrange
        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setName("ADMIN");

        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(registerRequest)).thenReturn(customer);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(AppConstants.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName(AppConstants.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        com.hobby.shop.dto.response.CustomerResponse customerResponse =
                new com.hobby.shop.dto.response.CustomerResponse();
        customerResponse.setId(1L);
        customerResponse.setEmail("test@example.com");
        customerResponse.setFirstName("John");
        customerResponse.setLastName("Doe");

        when(customerMapper.toResponse(any(Customer.class))).thenReturn(customerResponse);

        // Act
        var response = authService.registerAdmin(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");

        verify(customerRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository).findByName(AppConstants.ROLE_USER);
        verify(roleRepository).findByName(AppConstants.ROLE_ADMIN);
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void registerAdmin_EmailAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.registerAdmin(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email test@example.com is already registered");

        verify(customerRepository).existsByEmail(registerRequest.getEmail());
        verify(roleRepository, never()).findByName(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerAdmin_UserRoleNotFound_ThrowsRuntimeException() {
        // Arrange
        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(registerRequest)).thenReturn(customer);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(AppConstants.ROLE_USER)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.registerAdmin(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("USER role not found");
    }

    @Test
    void registerAdmin_AdminRoleNotFound_ThrowsRuntimeException() {
        // Arrange
        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(registerRequest)).thenReturn(customer);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(AppConstants.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName(AppConstants.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.registerAdmin(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ADMIN role not found");
    }

    @Test
    void verifyEmail_AlwaysReturnsTrue() {
        // Act
        boolean result = authService.verifyEmail("some-token");

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void requestPasswordReset_Success() {
        // Arrange
        String email = "test@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        // Act
        authService.requestPasswordReset(email);

        // Assert
        verify(customerRepository).findByEmail(email);
    }

    @Test
    void requestPasswordReset_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.requestPasswordReset(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);
    }

    @Test
    void resetPassword_DoesNothing() {
        // Act (should not throw any exception)
        authService.resetPassword("token", "newPassword");

        // Assert - no verification needed, just ensure no exception
    }

    @Test
    void changePassword_Success() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "oldPass";
        String newPassword = "newPass";
        String encodedNewPassword = "encodedNewPass";

        // Setup the customer with the original encoded password
        when(customerRepository.findById(userId)).thenReturn(Optional.of(customer));

        // Mock the password encoder to verify old password matches
        when(passwordEncoder.matches(oldPassword, customer.getPassword())).thenReturn(true);

        // Mock the encoding of the new password
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // Capture the customer when saved to verify the password was updated
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer savedCustomer = invocation.getArgument(0);
            return savedCustomer;
        });

        // Act
        authService.changePassword(userId, oldPassword, newPassword);

        // Assert
        verify(customerRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedPassword"); // Verify old password check
        verify(passwordEncoder).encode(newPassword); // Verify new password encoding

        // Verify the customer was saved with the new encoded password
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();
        assertThat(savedCustomer.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    void changePassword_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long userId = 999L;
        when(customerRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(userId, "old", "new"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + userId);
    }

    @Test
    void changePassword_IncorrectOldPassword_ThrowsBadRequestException() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "wrongPass";

        when(customerRepository.findById(userId)).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches(oldPassword, customer.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.changePassword(userId, oldPassword, "newPass"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(customerRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, customer.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void registerUser_AuthenticationFails_PropagatesException() {
        // Arrange
        when(customerRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(registerRequest)).thenReturn(customer);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(AppConstants.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        // Act & Assert
        assertThatThrownBy(() -> authService.registerUser(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Authentication failed");

        verify(customerRepository).save(any(Customer.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateJwtToken(any());
    }
}