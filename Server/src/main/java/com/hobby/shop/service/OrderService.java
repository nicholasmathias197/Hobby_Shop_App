package com.hobby.shop.service;

import com.hobby.shop.dto.request.OrderRequest;
import com.hobby.shop.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    // Customer order operations
    OrderResponse createOrder(String email, OrderRequest request);
    OrderResponse createGuestOrder(String sessionId, OrderRequest request, String guestEmail);
    OrderResponse getOrderByNumber(String orderNumber);
    Page<OrderResponse> getUserOrders(String email, Pageable pageable);
    Page<OrderResponse> getGuestOrders(String guestEmail, Pageable pageable);

    // Order status operations
    OrderResponse updateOrderStatus(Long orderId, String status, String comment);
    OrderResponse updatePaymentStatus(Long orderId, String paymentStatus);
    OrderResponse cancelOrder(Long orderId, String reason);

    // Admin operations
    Page<OrderResponse> getAllOrders(Pageable pageable);
    Page<OrderResponse> getOrdersByStatus(String status, Pageable pageable);

    // Tracking
    OrderResponse updateTrackingNumber(Long orderId, String trackingNumber);
}