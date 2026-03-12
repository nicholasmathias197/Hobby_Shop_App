package com.hobby.shop.controller;

import com.hobby.shop.dto.request.*;
import com.hobby.shop.dto.response.OrderResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.service.OrderService;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    // ============= AUTHENTICATED USER ENDPOINTS =============

    /**
     * Create order for guest user
     * POST /api/orders/guest
     */
    @PostMapping("/guest")
    public ResponseEntity<OrderResponse> createGuestOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestParam(required = false) String sessionId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionIdHeader) {

        // Determine session ID from either query param or header
        String finalSessionId = sessionId;
        if (finalSessionId == null || finalSessionId.isEmpty()) {
            finalSessionId = sessionIdHeader;
        }

        if (finalSessionId == null || finalSessionId.isEmpty()) {
            throw new BadRequestException("Session ID is required for guest checkout");
        }

        log.info("Creating guest order for session: {}", finalSessionId);
        OrderResponse response = orderService.createGuestOrder(finalSessionId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all orders for authenticated user
     * GET /api/orders
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        String email = securityUtils.getCurrentUserEmail();
        log.info("Fetching orders for authenticated user: {}", email);
        return ResponseEntity.ok(orderService.getUserOrders(email, pageable));
    }

    /**
     * Get specific order by order number (must belong to authenticated user)
     * GET /api/orders/{orderNumber}
     */
    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        String email = securityUtils.getCurrentUserEmail();
        log.info("Fetching order {} for user: {}", orderNumber, email);
        return ResponseEntity.ok(orderService.getOrderByNumber(email, orderNumber));
    }

    /**
     * Cancel order (must belong to authenticated user)
     * PUT /api/orders/{orderId}/cancel
     */
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody(required = false) CancelOrderRequest request) {

        String email = securityUtils.getCurrentUserEmail();
        String reason = request != null ? request.getReason() : null;
        log.info("User {} cancelling order ID: {}", email, orderId);
        return ResponseEntity.ok(orderService.cancelOrder(email, orderId, reason));
    }

    // ============= GUEST ORDER LOOKUP (Public) =============

    /**
     * Guest order lookup (requires email verification)
     * GET /api/orders/guest/lookup
     */
    @GetMapping("/guest/lookup")
    public ResponseEntity<OrderResponse> getGuestOrder(
            @RequestParam String email,
            @RequestParam String orderNumber) {

        log.info("Guest looking up order: {} for email: {}", orderNumber, email);
        return ResponseEntity.ok(orderService.getGuestOrder(orderNumber, email));
    }

    // ============= ADMIN ENDPOINTS =============

    /**
     * Get all orders (admin only)
     * GET /api/orders/all
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Admin fetching all orders");
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    /**
     * Get orders by status (admin only)
     * GET /api/orders/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("Admin fetching orders with status: {}", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    /**
     * Update order status (admin only)
     * PUT /api/orders/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {

        log.info("Admin updating order {} status to: {}", orderId, request.getStatus());
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus(), request.getComment()));
    }

    /**
     * Update payment status (admin only)
     * PUT /api/orders/{orderId}/payment
     */
    @PutMapping("/{orderId}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {

        log.info("Admin updating payment for order {} to: {}", orderId, request.getPaymentStatus());
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, request.getPaymentStatus()));
    }

    /**
     * Update tracking number (admin only)
     * PUT /api/orders/{orderId}/tracking
     */
    @PutMapping("/{orderId}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateTrackingNumber(
            @PathVariable Long orderId,
            @Valid @RequestBody TrackingNumberUpdateRequest request) {

        log.info("Admin updating tracking for order {} to: {}", orderId, request.getTrackingNumber());
        return ResponseEntity.ok(orderService.updateTrackingNumber(orderId, request.getTrackingNumber()));
    }
}