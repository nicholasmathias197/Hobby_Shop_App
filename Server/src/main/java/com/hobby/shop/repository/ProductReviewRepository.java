package com.hobby.shop.repository;

import com.hobby.shop.model.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    Page<ProductReview> findByProductId(Long productId, Pageable pageable);

    boolean existsByProductIdAndCustomerId(Long productId, Long customerId);


    @Query("SELECT AVG(pr.rating) FROM ProductReview pr WHERE pr.product.id = :productId")
    Optional<Double> findAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId")
    Long countByProductId(@Param("productId") Long productId);

    @Query("SELECT pr.rating, COUNT(pr) FROM ProductReview pr WHERE pr.product.id = :productId GROUP BY pr.rating ORDER BY pr.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);


}