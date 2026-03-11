package com.hobby.shop.controller;

import com.hobby.shop.dto.request.ReviewRequest;
import com.hobby.shop.dto.response.RatingResponse;
import com.hobby.shop.dto.response.ReviewResponse;
import com.hobby.shop.service.ReviewService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtils securityUtils;

    /**
     * Get all reviews for a specific product
     * GET /api/products/{productId}/reviews
     */
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Fetching reviews for product ID: {}", productId);
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }

    /**
     * Get average rating and distribution for a product
     * GET /api/products/{productId}/rating
     */
    @GetMapping("/products/{productId}/rating")
    public ResponseEntity<RatingResponse> getProductRating(@PathVariable Long productId) {
        log.info("Fetching rating for product ID: {}", productId);
        return ResponseEntity.ok(reviewService.getProductRating(productId));
    }

    /**
     * Create a new review for a product (authenticated users only)
     * POST /api/products/{productId}/reviews
     */
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {

        Long customerId = securityUtils.getCurrentUserId();
        log.info("Creating review for product ID: {} by customer ID: {}", productId, customerId);

        ReviewResponse response = reviewService.createReview(productId, customerId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing review (only the review owner can update)
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request) {

        Long customerId = securityUtils.getCurrentUserId();
        log.info("Updating review ID: {} by customer ID: {}", reviewId, customerId);

        ReviewResponse response = reviewService.updateReview(reviewId, customerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a review (only the review owner can delete)
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        Long customerId = securityUtils.getCurrentUserId();
        log.info("Deleting review ID: {} by customer ID: {}", reviewId, customerId);

        reviewService.deleteReview(reviewId, customerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Check if current user has reviewed a product
     * GET /api/products/{productId}/has-reviewed
     */
    @GetMapping("/products/{productId}/has-reviewed")
    public ResponseEntity<Boolean> hasUserReviewedProduct(@PathVariable Long productId) {
        Long customerId = securityUtils.getCurrentUserId();

        // If user is not authenticated, return false
        if (customerId == null) {
            return ResponseEntity.ok(false);
        }

        boolean hasReviewed = reviewService.hasUserReviewedProduct(productId, customerId);
        return ResponseEntity.ok(hasReviewed);
    }
}