package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CustomerUpdateRequest;
import com.hobby.shop.dto.request.ReviewRequest;
import com.hobby.shop.dto.request.RoleAssignmentRequest;
import com.hobby.shop.dto.response.ApiResponse;
import com.hobby.shop.dto.response.CustomerResponse;
import com.hobby.shop.dto.response.CustomerStatisticsResponse;
import com.hobby.shop.dto.response.RatingResponse;
import com.hobby.shop.dto.response.ReviewResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.model.Role;
import com.hobby.shop.repository.RoleRepository;
import com.hobby.shop.service.CustomerService;
import com.hobby.shop.service.ReviewService;
import com.hobby.shop.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerReviewSupportControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private ReviewService reviewService;

    @Mock
    private RoleRepository roleRepository;

    private CustomerController customerController;
    private ReviewController reviewController;
    private DebugController debugController;
    private HealthController healthController;

    @BeforeEach
    void setUp() {
        customerController = new CustomerController(customerService, securityUtils);
        reviewController = new ReviewController(reviewService, securityUtils);
        debugController = new DebugController(roleRepository);
        healthController = new HealthController();
    }

    @Test
    void customerControllerDelegatesProfileAdminAndStatisticsEndpoints() {
        Pageable pageable = PageRequest.of(0, 20);
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        CustomerResponse currentCustomer = mock(CustomerResponse.class);
        CustomerResponse updatedCustomer = mock(CustomerResponse.class);
        Page<CustomerResponse> page = new PageImpl<>(List.of(currentCustomer));
        CustomerStatisticsResponse statistics = mock(CustomerStatisticsResponse.class);
        RoleAssignmentRequest roleAssignmentRequest = new RoleAssignmentRequest();
        roleAssignmentRequest.setRoleId(9L);

        when(securityUtils.getCurrentUserEmail()).thenReturn("customer@example.com");
        when(customerService.getCustomerByEmail("customer@example.com")).thenReturn(currentCustomer);
        when(currentCustomer.getId()).thenReturn(44L);
        when(customerService.updateCustomer(44L, updateRequest)).thenReturn(updatedCustomer);
        when(customerService.getAllCustomers(pageable)).thenReturn(page);
        when(customerService.getCustomerById(2L)).thenReturn(currentCustomer);
        when(customerService.getCustomerByEmail("admin@example.com")).thenReturn(updatedCustomer);
        when(customerService.updateCustomer(2L, updateRequest)).thenReturn(updatedCustomer);
        when(customerService.toggleCustomerStatus(2L, true)).thenReturn(updatedCustomer);
        when(customerService.getActiveCustomerCount()).thenReturn(8L);
        when(customerService.getInactiveCustomerCount()).thenReturn(3L);
        when(customerService.getCustomerStatistics()).thenReturn(statistics);

        assertThat(customerController.getCurrentUserProfile().getBody()).isSameAs(currentCustomer);
        assertThat(customerController.updateCurrentUserProfile(updateRequest).getBody()).isSameAs(updatedCustomer);
        assertThat(customerController.changePassword("old", "new").getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(customerController.getAllCustomers(pageable).getBody()).isSameAs(page);
        assertThat(customerController.getCustomerById(2L).getBody()).isSameAs(currentCustomer);
        assertThat(customerController.getCustomerByEmail("admin@example.com").getBody()).isSameAs(updatedCustomer);
        assertThat(customerController.updateCustomer(2L, updateRequest).getBody()).isSameAs(updatedCustomer);
        assertThat(customerController.deleteCustomer(2L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(customerController.toggleCustomerStatus(2L, Map.of("enabled", true)).getBody()).isSameAs(updatedCustomer);
        assertThat(customerController.addRoleToCustomer(2L, Map.of("roleId", 9L)).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(customerController.removeRoleFromCustomer(2L, roleAssignmentRequest).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(customerController.getActiveCustomerCount().getBody()).isEqualTo(8L);
        assertThat(customerController.getInactiveCustomerCount().getBody()).isEqualTo(3L);
        assertThat(customerController.getCustomerStatistics().getBody()).isSameAs(statistics);

        verify(customerService).changePassword(44L, "old", "new");
        verify(customerService).deleteCustomer(2L);
        verify(customerService).addRoleToCustomer(2L, 9L);
        verify(customerService).removeRoleFromCustomer(2L, 9L);
    }

    @Test
    void customerControllerRejectsMissingFieldsForStatusAndRoleAssignment() {
        assertThatThrownBy(() -> customerController.toggleCustomerStatus(2L, Map.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("enabled field is required");

        assertThatThrownBy(() -> customerController.addRoleToCustomer(2L, Map.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("roleId is required");
    }

    @Test
    void reviewControllerDelegatesAllEndpointsAndHandlesAnonymousUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        ReviewRequest reviewRequest = new ReviewRequest(5, "Great product");
        Page<ReviewResponse> page = new PageImpl<>(List.of(mock(ReviewResponse.class)));
        ReviewResponse review = mock(ReviewResponse.class);
        RatingResponse rating = mock(RatingResponse.class);

        when(reviewService.getProductReviews(1L, pageable)).thenReturn(page);
        when(reviewService.getProductRating(1L)).thenReturn(rating);
        when(securityUtils.getCurrentUserId()).thenReturn(22L, 22L, 22L, null, 22L);
        when(reviewService.createReview(1L, 22L, reviewRequest)).thenReturn(review);
        when(reviewService.updateReview(2L, 22L, reviewRequest)).thenReturn(review);
        when(reviewService.hasUserReviewedProduct(1L, 22L)).thenReturn(true);

        assertThat(reviewController.getProductReviews(1L, pageable).getBody()).isSameAs(page);
        assertThat(reviewController.getProductRating(1L).getBody()).isSameAs(rating);
        assertThat(reviewController.createReview(1L, reviewRequest).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reviewController.updateReview(2L, reviewRequest).getBody()).isSameAs(review);
        assertThat(reviewController.deleteReview(2L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(reviewController.hasUserReviewedProduct(1L).getBody()).isFalse();
        assertThat(reviewController.hasUserReviewedProduct(1L).getBody()).isTrue();

        verify(reviewService).deleteReview(2L, 22L);
    }

    @Test
    void debugControllerReturnsSuccessPayloadOnHealthyRepository() {
        Role userRole = new Role();
        userRole.setName("USER");
        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        when(roleRepository.count()).thenReturn(2L);
        when(roleRepository.findAll()).thenReturn(List.of(userRole, adminRole));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));

        var response = debugController.checkRoles();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("totalRoles", 2L);
        assertThat(response.getBody()).containsEntry("userRoleExists", true);
        assertThat(response.getBody()).containsEntry("adminRoleExists", true);
    }

    @Test
    void debugControllerReturnsInternalServerErrorWhenCountFails() {
        when(roleRepository.count()).thenThrow(new IllegalStateException("db down"));

        var response = debugController.checkRoles();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("error")).isEqualTo("Failed to count roles: db down");
    }

    @Test
    void debugControllerUsesOuterCatchForUnexpectedErrors() {
        when(roleRepository.count()).thenReturn(1L);
        when(roleRepository.findAll()).thenReturn(List.of());
        when(roleRepository.findByName("USER")).thenThrow(new IllegalStateException("unexpected"));

        var response = debugController.checkRoles();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("error")).isEqualTo("Unexpected error: unexpected");
    }

    @Test
    void debugControllerPingAndDatabaseCheckReturnExpectedPayloads() {
        when(roleRepository.count()).thenReturn(1L);

        var pingResponse = debugController.ping();
        var dbResponse = debugController.checkDatabase();

        assertThat(pingResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pingResponse.getBody()).containsEntry("message", "pong");
        assertThat(dbResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(dbResponse.getBody()).containsEntry("databaseConnected", true);
        assertThat(dbResponse.getBody()).containsEntry("repositoryInitialized", true);
    }

    @Test
    void healthControllerReturnsHealthyApiResponse() {
        var response = healthController.healthCheck();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(ApiResponse.class);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Service is healthy");
    }
}