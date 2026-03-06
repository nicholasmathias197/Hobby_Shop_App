package com.hobby.shop.controller;

import com.hobby.shop.dto.request.OrderRequest;
import com.hobby.shop.dto.response.OrderResponse;
import com.hobby.shop.service.OrderService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    // Customer endpoints
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        String email = securityUtils.getCurrentUserEmail();
        return new ResponseEntity<>(orderService.createOrder(email, request), HttpStatus.CREATED);
    }

    @PostMapping("/guest")
    public ResponseEntity<OrderResponse> createGuestOrder(
            @RequestParam String sessionId,
            @RequestParam(required = false) String guestEmail,
            @Valid @RequestBody OrderRequest request) {
        return new ResponseEntity<>(orderService.createGuestOrder(sessionId, request, guestEmail),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        // If authenticated, get user email, otherwise check for guest email in session
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return ResponseEntity.ok(orderService.getUserOrders(email, pageable));
        } else {
            // Handle guest orders via session ID - you might want to get this from a cookie or header
            String sessionId = getSessionIdFromRequest(); // Implement this method
            return ResponseEntity.ok(orderService.getGuestOrders(sessionId, pageable));
        }
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, reason));
    }

    // Admin endpoints
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> getOrdersByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status, comment));
    }

    @PutMapping("/{orderId}/payment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam String paymentStatus) {
        return ResponseEntity.ok(orderService.updatePaymentStatus(orderId, paymentStatus));
    }

    @PutMapping("/{orderId}/tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateTrackingNumber(
            @PathVariable Long orderId,
            @RequestParam String trackingNumber) {
        return ResponseEntity.ok(orderService.updateTrackingNumber(orderId, trackingNumber));
    }

    private String getSessionIdFromRequest() {
        // Implement logic to get session ID from cookie, header, or parameter
        // This is just a placeholder
        return null;
    }
}