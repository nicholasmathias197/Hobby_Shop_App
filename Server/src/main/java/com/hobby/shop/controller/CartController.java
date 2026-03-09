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

    // ============= CART MANAGEMENT ENDPOINTS =============

    /**
     * Get current user's cart (authenticated or guest)
     * GET /api/cart
     * For authenticated users: fetches cart by email
     * For guests: fetches cart by session ID (creates new session if needed)
     * @return Cart response with items and totals
     */
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

    /**
     * Add item to cart (authenticated or guest)
     * POST /api/cart/items
     * @param request Cart item details (product ID, quantity)
     * @return Updated cart response
     */
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

    /**
     * Update quantity of a specific cart item
     * PUT /api/cart/items/{cartItemId}?quantity={quantity}
     * @param cartItemId Cart item ID
     * @param quantity New quantity
     * @return Updated cart response
     */
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

    /**
     * Remove a specific item from cart
     * DELETE /api/cart/items/{cartItemId}
     * @param cartItemId Cart item ID to remove
     * @return Updated cart response
     */
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

    /**
     * Clear all items from cart
     * DELETE /api/cart
     * @return No content response
     */
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

    /**
     * Get total number of items in cart
     * GET /api/cart/count
     * @return Item count
     */
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

    // ============= CART MERGING =============

    /**
     * Merge guest cart with authenticated user's cart (happens after login)
     * POST /api/cart/merge?sessionId={sessionId}
     * @param sessionId Guest session ID to merge from
     * @return Merged cart response
     */
    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeCarts(@RequestParam String sessionId) {
        String email = securityUtils.getCurrentUserEmail();
        log.info("Merging session cart {} with user cart for {}", sessionId, email);
        return ResponseEntity.ok(cartService.mergeCarts(email, sessionId));
    }

    // ============= HELPER METHODS =============

    /**
     * Get existing session ID from cookie/header or create a new one
     * Priority: Cookie > Header > Generate new
     * Sets cookie in response for new sessions
     * @return Session ID string
     */
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