package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CartItemRequest;
import com.hobby.shop.dto.response.ApiResponse;
import com.hobby.shop.dto.response.CartResponse;
import com.hobby.shop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String sessionId = getSessionId(request);
        if (userDetails != null) {
            return ResponseEntity.ok(cartService.getCartByUser(userDetails.getUsername()));
        } else {
            return ResponseEntity.ok(cartService.getCartBySession(sessionId));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody CartItemRequest itemRequest,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String sessionId = getSessionId(request);
        CartResponse cartResponse;

        if (userDetails != null) {
            cartResponse = cartService.addItemToUserCart(userDetails.getUsername(), itemRequest);
        } else {
            cartResponse = cartService.addItemToSessionCart(sessionId, itemRequest);
        }

        return ResponseEntity.ok(cartResponse);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String sessionId = getSessionId(request);
        CartResponse cartResponse;

        if (userDetails != null) {
            cartResponse = cartService.updateCartItemQuantity(userDetails.getUsername(), itemId, quantity);
        } else {
            cartResponse = cartService.updateSessionCartItemQuantity(sessionId, itemId, quantity);
        }

        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String sessionId = getSessionId(request);
        CartResponse cartResponse;

        if (userDetails != null) {
            cartResponse = cartService.removeItemFromUserCart(userDetails.getUsername(), itemId);
        } else {
            cartResponse = cartService.removeItemFromSessionCart(sessionId, itemId);
        }

        return ResponseEntity.ok(cartResponse);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse> clearCart(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        String sessionId = getSessionId(request);

        if (userDetails != null) {
            cartService.clearUserCart(userDetails.getUsername());
        } else {
            cartService.clearSessionCart(sessionId);
        }

        return ResponseEntity.ok(new ApiResponse(true, "Cart cleared successfully"));
    }

    private String getSessionId(HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-ID");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = request.getSession().getId();
        }
        return sessionId;
    }
}