package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CartItemRequest;
import com.hobby.shop.dto.response.CartResponse;
import com.hobby.shop.service.CartService;
import com.hobby.shop.util.SecurityUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @GetMapping
    public ResponseEntity<CartResponse> getUserCart() {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            log.info("Fetching cart for authenticated user: {}", email);
            return ResponseEntity.ok(cartService.getCartByUser(email));
        } else {
            String sessionId = getOrCreateSessionId();
            log.info("Fetching cart for guest with session: {}", sessionId);
            return ResponseEntity.ok(cartService.getCartBySession(sessionId));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(@Valid @RequestBody CartItemRequest request) {
        log.info("Adding item to cart. Request: {}", request);

        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            log.info("Authenticated user: {}", email);
            return new ResponseEntity<>(cartService.addItemToUserCart(email, request), HttpStatus.CREATED);
        } else {
            String sessionId = getOrCreateSessionId();
            log.info("Guest user with session: {}", sessionId);
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
            String sessionId = getOrCreateSessionId();
            return ResponseEntity.ok(cartService.updateSessionCartItemQuantity(sessionId, cartItemId, quantity));
        }
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@PathVariable Long cartItemId) {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return ResponseEntity.ok(cartService.removeItemFromUserCart(email, cartItemId));
        } else {
            String sessionId = getOrCreateSessionId();
            return ResponseEntity.ok(cartService.removeItemFromSessionCart(sessionId, cartItemId));
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            cartService.clearUserCart(email);
        } else {
            String sessionId = getOrCreateSessionId();
            cartService.clearSessionCart(sessionId);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeCarts(@RequestParam String sessionId) {
        String email = securityUtils.getCurrentUserEmail();
        log.info("Merging session cart {} with user cart for {}", sessionId, email);
        return ResponseEntity.ok(cartService.mergeCarts(email, sessionId));
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            return ResponseEntity.ok(cartService.getCartItemCount(email, true));
        } else {
            String sessionId = getOrCreateSessionId();
            return ResponseEntity.ok(cartService.getCartItemCount(sessionId, false));
        }
    }

    private String getOrCreateSessionId() {
        // Try to get from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("CART_SESSION_ID".equals(cookie.getName())) {
                    log.debug("Found existing session cookie: {}", cookie.getValue());
                    return cookie.getValue();
                }
            }
        }

        // Try to get from header
        String headerSessionId = request.getHeader("X-Session-ID");
        if (headerSessionId != null && !headerSessionId.isEmpty()) {
            log.debug("Using session from header: {}", headerSessionId);
            return headerSessionId;
        }

        // Create new session ID
        String newSessionId = UUID.randomUUID().toString();
        log.info("Creating new session ID: {}", newSessionId);

        // Set cookie in response
        Cookie sessionCookie = new Cookie("CART_SESSION_ID", newSessionId);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(false); // Set to true in production with HTTPS
        response.addCookie(sessionCookie);

        return newSessionId;
    }
}