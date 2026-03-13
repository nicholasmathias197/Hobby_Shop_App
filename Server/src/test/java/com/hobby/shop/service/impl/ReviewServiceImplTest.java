package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.ReviewRequest;
import com.hobby.shop.dto.response.RatingResponse;
import com.hobby.shop.dto.response.ReviewResponse;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.exception.UnauthorizedException;
import com.hobby.shop.model.Customer;
import com.hobby.shop.model.Product;
import com.hobby.shop.model.ProductReview;
import com.hobby.shop.repository.CustomerRepository;
import com.hobby.shop.repository.OrderRepository;
import com.hobby.shop.repository.ProductRepository;
import com.hobby.shop.repository.ProductReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest extends BaseServiceTest {

    @Mock
    private ProductReviewRepository reviewRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Customer customer;
    private Product product;
    private ProductReview review;
    private ProductReview verifiedReview;
    private ReviewRequest reviewRequest;

    private final Long productId = 1L;
    private final Long customerId = 1L;
    private final Long reviewId = 1L;
    private final int rating = 4;

    @BeforeEach
    void setUp() {
        customer = createTestCustomer(customerId, "test@example.com");
        product = createTestProduct(productId, "Test Product", "SKU123", 10);

        review = createTestReview(reviewId, product, customer, rating);
        review.setCreatedAt(LocalDateTime.now());

        verifiedReview = createTestVerifiedReview(2L, product, customer, 5);
        verifiedReview.setCreatedAt(LocalDateTime.now());

        reviewRequest = createTestReviewRequest(rating);
    }

    // ==================== GET PRODUCT REVIEWS TESTS ====================

    @Test
    void getProductReviews_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<ProductReview> reviews = Arrays.asList(review, verifiedReview);
        Page<ProductReview> reviewPage = new PageImpl<>(reviews, pageable, reviews.size());

        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findByProductId(productId, pageable)).thenReturn(reviewPage);

        // Act
        Page<ReviewResponse> result = reviewService.getProductReviews(productId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        verify(productRepository).existsById(productId);
        verify(reviewRepository).findByProductId(productId, pageable);
    }

    @Test
    void getProductReviews_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> reviewService.getProductReviews(999L, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");

        verify(productRepository).existsById(999L);
        verify(reviewRepository, never()).findByProductId(anyLong(), any());
    }

    @Test
    void getProductReviews_NoReviews_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductReview> emptyPage = Page.empty(pageable);

        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findByProductId(productId, pageable)).thenReturn(emptyPage);

        // Act
        Page<ReviewResponse> result = reviewService.getProductReviews(productId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    // ==================== GET PRODUCT RATING TESTS ====================

    @Test
    void getProductRating_Success() {
        // Arrange
        Double averageRating = 4.5;
        Long totalReviews = 10L;

        List<Object[]> distributionData = Arrays.asList(
                new Object[]{5, 5L},
                new Object[]{4, 3L},
                new Object[]{3, 1L},
                new Object[]{2, 1L},
                new Object[]{1, 0L}
        );

        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findAverageRatingByProductId(productId)).thenReturn(Optional.of(averageRating));
        when(reviewRepository.countByProductId(productId)).thenReturn(totalReviews);
        when(reviewRepository.getRatingDistribution(productId)).thenReturn(distributionData);

        // Act
        RatingResponse result = reviewService.getProductRating(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAverage()).isEqualTo(averageRating);
        assertThat(result.getTotal()).isEqualTo(totalReviews);
        assertThat(result.getDistribution()).hasSize(5);
        assertThat(result.getDistribution().get(5)).isEqualTo(5L);
        assertThat(result.getDistribution().get(4)).isEqualTo(3L);
        assertThat(result.getDistribution().get(3)).isEqualTo(1L);
        assertThat(result.getDistribution().get(2)).isEqualTo(1L);
        assertThat(result.getDistribution().get(1)).isEqualTo(0L);

        verify(productRepository).existsById(productId);
        verify(reviewRepository).findAverageRatingByProductId(productId);
        verify(reviewRepository).countByProductId(productId);
        verify(reviewRepository).getRatingDistribution(productId);
    }

    @Test
    void getProductRating_NoReviews_ReturnsZeroAverageAndEmptyDistribution() {
        // Arrange
        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findAverageRatingByProductId(productId)).thenReturn(Optional.empty());
        when(reviewRepository.countByProductId(productId)).thenReturn(0L);
        when(reviewRepository.getRatingDistribution(productId)).thenReturn(new ArrayList<>());

        // Act
        RatingResponse result = reviewService.getProductRating(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAverage()).isEqualTo(0.0);
        assertThat(result.getTotal()).isEqualTo(0L);
        assertThat(result.getDistribution()).hasSize(5);
        assertThat(result.getDistribution().get(5)).isEqualTo(0L);
        assertThat(result.getDistribution().get(4)).isEqualTo(0L);
        assertThat(result.getDistribution().get(3)).isEqualTo(0L);
        assertThat(result.getDistribution().get(2)).isEqualTo(0L);
        assertThat(result.getDistribution().get(1)).isEqualTo(0L);
    }

    @Test
    void getProductRating_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> reviewService.getProductRating(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with ID: 999");
    }

    // ==================== CREATE REVIEW TESTS ====================

    @Test
    void createReview_Success() {
        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.existsByCustomerIdAndOrderItemsProductId(customerId, productId)).thenReturn(false);
        when(reviewRepository.save(any(ProductReview.class))).thenReturn(review);

        // Act
        ReviewResponse result = reviewService.createReview(productId, customerId, reviewRequest);

        // Assert
        assertThat(result).isNotNull();

        verify(reviewRepository).existsByProductIdAndCustomerId(productId, customerId);
        verify(productRepository).findById(productId);
        verify(customerRepository).findById(customerId);
        verify(orderRepository).existsByCustomerIdAndOrderItemsProductId(customerId, productId);
        verify(reviewRepository).save(any(ProductReview.class));
    }

    @Test
    void createReview_VerifiedPurchase_Success() {
        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.existsByCustomerIdAndOrderItemsProductId(customerId, productId)).thenReturn(true);
        when(reviewRepository.save(any(ProductReview.class))).thenReturn(verifiedReview);

        // Act
        ReviewResponse result = reviewService.createReview(productId, customerId, reviewRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).existsByCustomerIdAndOrderItemsProductId(customerId, productId);
    }

    @Test
    void createReview_UserAlreadyReviewed_ThrowsIllegalStateException() {
        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(productId, customerId, reviewRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User has already reviewed this product");

        verify(reviewRepository).existsByProductIdAndCustomerId(productId, customerId);
        verify(productRepository, never()).findById(any());
        verify(customerRepository, never()).findById(any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(productId, customerId, reviewRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with ID: " + productId);

        verify(reviewRepository).existsByProductIdAndCustomerId(productId, customerId);
        verify(productRepository).findById(productId);
        verify(customerRepository, never()).findById(any());
    }

    @Test
    void createReview_CustomerNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(productId, customerId, reviewRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found with ID: " + customerId);
    }

    // ==================== UPDATE REVIEW TESTS ====================

    @Test
    void updateReview_Success() {
        // Arrange
        ReviewRequest updateRequest = createTestReviewRequest(5);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(ProductReview.class))).thenReturn(review);

        // Act
        ReviewResponse result = reviewService.updateReview(reviewId, customerId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Test review comment");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).save(review);
    }

    @Test
    void updateReview_ReviewNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(reviewRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.updateReview(invalidId, customerId, reviewRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review not found with ID: " + invalidId);
    }

    @Test
    void updateReview_NotOwner_ThrowsUnauthorizedException() {
        // Arrange
        Long differentCustomerId = 2L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, differentCustomerId, reviewRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You are not authorized to update this review");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).save(any());
    }

    // ==================== DELETE REVIEW TESTS ====================

    @Test
    void deleteReview_Success() {
        // Arrange
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        // Act
        reviewService.deleteReview(reviewId, customerId);

        // Assert
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).delete(review);
    }

    @Test
    void deleteReview_ReviewNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(reviewRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.deleteReview(invalidId, customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Review not found with ID: " + invalidId);
    }

    @Test
    void deleteReview_NotOwner_ThrowsUnauthorizedException() {
        // Arrange
        Long differentCustomerId = 2L;
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, differentCustomerId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You are not authorized to delete this review");

        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository, never()).delete(any());
    }

    // ==================== HAS USER REVIEWED PRODUCT TESTS ====================

    @Test
    void hasUserReviewedProduct_ReturnsTrue() {
        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(true);

        // Act
        boolean result = reviewService.hasUserReviewedProduct(productId, customerId);

        // Assert
        assertThat(result).isTrue();
        verify(reviewRepository).existsByProductIdAndCustomerId(productId, customerId);
    }

    @Test
    void hasUserReviewedProduct_ReturnsFalse() {
        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);

        // Act
        boolean result = reviewService.hasUserReviewedProduct(productId, customerId);

        // Assert
        assertThat(result).isFalse();
        verify(reviewRepository).existsByProductIdAndCustomerId(productId, customerId);
    }

    // ==================== MAPPER TESTS (indirectly tested through other methods) ====================

    @Test
    void mapToReviewResponse_ContainsCorrectData() {
        // This is indirectly tested through create/update methods
        // But we can verify the mapping logic by capturing and examining the saved entity

        // Arrange
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(orderRepository.existsByCustomerIdAndOrderItemsProductId(customerId, productId)).thenReturn(false);

        ArgumentCaptor<ProductReview> reviewCaptor = ArgumentCaptor.forClass(ProductReview.class);
        when(reviewRepository.save(reviewCaptor.capture())).thenReturn(review);

        // Act
        reviewService.createReview(productId, customerId, reviewRequest);

        // Assert
        ProductReview capturedReview = reviewCaptor.getValue();
        assertThat(capturedReview.getProduct()).isEqualTo(product);
        assertThat(capturedReview.getCustomer()).isEqualTo(customer);
        assertThat(capturedReview.getRating()).isEqualTo(rating);
        assertThat(capturedReview.getComment()).isEqualTo("Test review comment");
        assertThat(capturedReview.getIsVerifiedPurchase()).isFalse();
    }
}