package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.ReviewRequest;
import com.hobby.shop.dto.response.RatingResponse;
import com.hobby.shop.dto.response.ReviewResponse;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.exception.UnauthorizedException;
import com.hobby.shop.model.Customer;
import com.hobby.shop.model.Order;
import com.hobby.shop.model.Product;
import com.hobby.shop.model.ProductReview;
import com.hobby.shop.repository.CustomerRepository;
import com.hobby.shop.repository.OrderRepository;
import com.hobby.shop.repository.ProductRepository;
import com.hobby.shop.repository.ProductReviewRepository;
import com.hobby.shop.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        log.info("Fetching reviews for product ID: {}", productId);

        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }

        return reviewRepository.findByProductId(productId, pageable)
                .map(this::mapToReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getProductRating(Long productId) {
        log.info("Calculating rating for product ID: {}", productId);

        // Verify product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId);
        }

        Double average = reviewRepository.findAverageRatingByProductId(productId).orElse(0.0);
        Long total = reviewRepository.countByProductId(productId);

        // Get rating distribution
        List<Object[]> distributionData = reviewRepository.getRatingDistribution(productId);
        Map<Integer, Long> distribution = new HashMap<>();

        // Initialize with zeros
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        // Fill in actual counts
        for (Object[] row : distributionData) {
            Integer rating = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            distribution.put(rating, count);
        }

        return RatingResponse.builder()
                .average(average)
                .total(total)
                .distribution(distribution)
                .build();
    }

    @Override
    @Transactional
    public ReviewResponse createReview(Long productId, Long customerId, ReviewRequest request) {
        log.info("Creating review for product ID: {} by customer ID: {}", productId, customerId);

        // Check if user already reviewed this product
        if (reviewRepository.existsByProductIdAndCustomerId(productId, customerId)) {
            throw new IllegalStateException("User has already reviewed this product");
        }

        // Fetch product and customer
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        // Check if purchase is verified (customer has ordered this product)
        boolean isVerifiedPurchase = orderRepository.existsByCustomerIdAndOrderItemsProductId(
                customerId, productId);

        // Create and save review
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setIsVerifiedPurchase(isVerifiedPurchase);

        ProductReview savedReview = reviewRepository.save(review);

        log.info("Review created successfully with ID: {}", savedReview.getId());
        return mapToReviewResponse(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Long customerId, ReviewRequest request) {
        log.info("Updating review ID: {} for customer ID: {}", reviewId, customerId);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Verify ownership
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You are not authorized to update this review");
        }

        // Update fields
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        ProductReview updatedReview = reviewRepository.save(review);

        log.info("Review updated successfully with ID: {}", updatedReview.getId());
        return mapToReviewResponse(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long customerId) {
        log.info("Deleting review ID: {} for customer ID: {}", reviewId, customerId);

        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Verify ownership
        if (!review.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedException("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
        log.info("Review deleted successfully with ID: {}", reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedProduct(Long productId, Long customerId) {
        return reviewRepository.existsByProductIdAndCustomerId(productId, customerId);
    }

    private ReviewResponse mapToReviewResponse(ProductReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .customerName(review.getCustomer().getFirstName() + " " + review.getCustomer().getLastName())
                .customerId(review.getCustomer().getId())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .createdAt(review.getCreatedAt())
                .build();
    }
}