package com.hobby.shop.repository;

import com.hobby.shop.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    boolean existsByOrderNumber(String orderNumber);
    Page<Order> findByStatus(String status, Pageable pageable);

    Page<Order> findByGuestEmail(String guestEmail, Pageable pageable);
    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o " +
            "JOIN o.items i WHERE o.customer.id = :customerId AND i.product.id = :productId")
    boolean existsByCustomerIdAndOrderItemsProductId(@Param("customerId") Long customerId,
                                                     @Param("productId") Long productId);
}
