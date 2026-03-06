package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CartItemRequest;
import com.hobby.shop.dto.response.CartResponse;
import com.hobby.shop.service.CartService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    // User cart endpoints
    @GetMapping
    public ResponseEntity<CartResponse> getUserCart() {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return ResponseEntity.ok(cartService.getCartByUser(email));
        } else {
            // Handle guest cart via session ID
            String sessionId = getSessionIdFromRequest();
            return ResponseEntity.ok(cartService.getCartBySession(sessionId));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(@Valid @RequestBody CartItemRequest request) {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return new ResponseEntity<>(cartService.addItemToUserCart(email, request), HttpStatus.CREATED);
        } else {
            String sessionId = getSessionIdFromRequest();
            return new ResponseEntity<>(cartService.addItemToSessionCart(sessionId, request), HttpStatus.CREATED);
        }
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return ResponseEntity.ok(cartService.updateCartItemQuantity(email, cartItemId, quantity));
        } else {
            String sessionId = getSessionIdFromRequest();
            return ResponseEntity.ok(cartService.updateSessionCartItemQuantity(sessionId, cartItemId, quantity));
        }
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long cartItemId) {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return ResponseEntity.ok(cartService.removeItemFromUserCart(email, cartItemId));
        } else {
            String sessionId = getSessionIdFromRequest();
            return ResponseEntity.ok(cartService.removeItemFromSessionCart(sessionId, cartItemId));
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            cartService.clearUserCart(email);
        } else {
            String sessionId = getSessionIdFromRequest();
            cartService.clearSessionCart(sessionId);
        }
        return ResponseEntity.noContent().build();
    }

    // Explicit session cart endpoints (for when you want to force session-based cart)
    @GetMapping("/session")
    public ResponseEntity<CartResponse> getSessionCart(@RequestParam String sessionId) {
        return ResponseEntity.ok(cartService.getCartBySession(sessionId));
    }

    @PostMapping("/session/items")
    public ResponseEntity<CartResponse> addItemToSessionCart(
            @RequestParam String sessionId,
            @Valid @RequestBody CartItemRequest request) {
        return new ResponseEntity<>(cartService.addItemToSessionCart(sessionId, request), HttpStatus.CREATED);
    }

    @PutMapping("/session/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateSessionCartItemQuantity(
            @RequestParam String sessionId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateSessionCartItemQuantity(sessionId, cartItemId, quantity));
    }

    @DeleteMapping("/session/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItemFromSessionCart(
            @RequestParam String sessionId,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItemFromSessionCart(sessionId, cartItemId));
    }

    @DeleteMapping("/session")
    public ResponseEntity<Void> clearSessionCart(@RequestParam String sessionId) {
        cartService.clearSessionCart(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeCarts(@RequestParam String sessionId) {
        String email = securityUtils.getCurrentUserEmail();
        return ResponseEntity.ok(cartService.mergeCarts(email, sessionId));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return ResponseEntity.ok(cartService.getCartItemCount(email, true));
        } else {
            String sessionId = getSessionIdFromRequest();
            return ResponseEntity.ok(cartService.getCartItemCount(sessionId, false));
        }
    }

    @GetMapping("/session/count")
    public ResponseEntity<Integer> getSessionCartItemCount(@RequestParam String sessionId) {
        return ResponseEntity.ok(cartService.getCartItemCount(sessionId, false));
    }

    private String getSessionIdFromRequest() {
        // You can implement this to get session ID from:
        // 1. Cookie - @CookieValue("sessionId") String sessionId
        // 2. Header - @RequestHeader("X-Session-ID") String sessionId
        // 3. Parameter - @RequestParam("sessionId") String sessionId

        // For now, returning null - you'll need to inject HttpServletRequest
        // or use Spring's RequestContextHolder to get the current request
        return null;
    }
}