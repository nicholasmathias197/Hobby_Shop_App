package com.hobby.shop.service;

import com.hobby.shop.dto.request.ReviewRequest;
import com.hobby.shop.dto.response.RatingResponse;
import com.hobby.shop.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {

    Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable);

    RatingResponse getProductRating(Long productId);

    ReviewResponse createReview(Long productId, Long customerId, ReviewRequest request);

    ReviewResponse updateReview(Long reviewId, Long customerId, ReviewRequest request);

    void deleteReview(Long reviewId, Long customerId);

    boolean hasUserReviewedProduct(Long productId, Long customerId);
}