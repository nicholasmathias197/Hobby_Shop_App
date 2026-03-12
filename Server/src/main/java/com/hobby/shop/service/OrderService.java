package com.hobby.shop.service;

import com.hobby.shop.dto.request.OrderRequest;
import com.hobby.shop.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    // Guest orders
    OrderResponse createGuestOrder(String sessionId, OrderRequest request);
    OrderResponse getGuestOrder(String orderNumber, String email);
    // Customer order operations (AUTHENTICATED USERS ONLY)
    OrderResponse createOrder(String email, OrderRequest request);
    OrderResponse getOrderByNumber(String email, String orderNumber); // Add email param for security
    Page<OrderResponse> getUserOrders(String email, Pageable pageable);
    OrderResponse cancelOrder(String email, Long orderId, String reason); // Add email param

    // Admin operations
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Page<OrderResponse> getOrdersByStatus(String status, Pageable pageable);
    OrderResponse updateOrderStatus(Long orderId, String status, String comment);
    OrderResponse updatePaymentStatus(Long orderId, String paymentStatus);
    OrderResponse updateTrackingNumber(Long orderId, String trackingNumber);


}