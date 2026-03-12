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
        log.info("=== GET USER CART ===");

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
        log.info("=== ADD ITEM TO CART ===");
        log.info("Request: productId={}, quantity={}", request.getProductId(), request.getQuantity());

        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            log.info("Authenticated user: {}", email);
            CartResponse cartResponse = cartService.addItemToUserCart(email, request);
            log.info("Item added successfully to user cart");
            return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
        } else {
            String sessionId = getOrCreateSessionId();
            log.info("Guest user with session: {}", sessionId);

            CartResponse cartResponse = cartService.addItemToSessionCart(sessionId, request);
            log.info("Item added successfully to guest cart. New item count: {}",
                    cartResponse.getItems().size());

            return new ResponseEntity<>(cartResponse, HttpStatus.CREATED);
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

        log.info("=== UPDATE CART ITEM ===");
        log.info("CartItemId: {}, Quantity: {}", cartItemId, quantity);

        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            log.info("Authenticated user: {}", email);
            CartResponse response = cartService.updateCartItemQuantity(email, cartItemId, quantity);
            log.info("Cart item updated successfully");
            return ResponseEntity.ok(response);
        } else {
            String sessionId = getOrCreateSessionId();
            log.info("Guest user with session: {}", sessionId);
            CartResponse response = cartService.updateSessionCartItemQuantity(sessionId, cartItemId, quantity);
            log.info("Cart item updated successfully");
            return ResponseEntity.ok(response);
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
        log.info("=== REMOVE ITEM FROM CART ===");
        log.info("CartItemId: {}", cartItemId);

        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            log.info("Authenticated user: {}", email);
            CartResponse response = cartService.removeItemFromUserCart(email, cartItemId);
            log.info("Item removed successfully from user cart");
            return ResponseEntity.ok(response);
        } else {
            String sessionId = getOrCreateSessionId();
            log.info("Guest user with session: {}", sessionId);
            CartResponse response = cartService.removeItemFromSessionCart(sessionId, cartItemId);
            log.info("Item removed successfully from guest cart");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Clear all items from cart
     * DELETE /api/cart
     * @return No content response
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        log.info("=== CLEAR CART ===");

        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            log.info("Clearing cart for user: {}", email);
            cartService.clearUserCart(email);
        } else {
            String sessionId = getOrCreateSessionId();
            log.info("Clearing cart for session: {}", sessionId);
            cartService.clearSessionCart(sessionId);
        }

        log.info("Cart cleared successfully");
        return ResponseEntity.noContent().build();
    }

    /**
     * Get total number of items in cart
     * GET /api/cart/count
     * @return Item count
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount() {
        log.info("=== GET CART ITEM COUNT ===");

        if (securityUtils.isAuthenticated()) {
            String email = securityUtils.getCurrentUserEmail();
            int count = cartService.getCartItemCount(email, true);
            log.info("Cart item count for user {}: {}", email, count);
            return ResponseEntity.ok(count);
        } else {
            String sessionId = getOrCreateSessionId();
            int count = cartService.getCartItemCount(sessionId, false);
            log.info("Cart item count for session {}: {}", sessionId, count);
            return ResponseEntity.ok(count);
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
        log.info("=== MERGE CARTS ===");
        log.info("Session ID to merge: {}", sessionId);

        String email = securityUtils.getCurrentUserEmail();
        log.info("Merging session cart {} with user cart for {}", sessionId, email);

        CartResponse mergedCart = cartService.mergeCarts(email, sessionId);
        log.info("Cart merged successfully. New item count: {}", mergedCart.getItems().size());

        return ResponseEntity.ok(mergedCart);
    }

    // ============= HELPER METHODS =============

    /**
     * Get existing session ID from cookie/header or create a new one
     * Priority: Cookie > Header > Generate new
     * Sets cookie in response for new sessions
     * @return Session ID string
     */
    private String getOrCreateSessionId() {
        log.info("=== getOrCreateSessionId called ===");

        // Log all request cookies for debugging
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.info("Found {} cookies in request:", cookies.length);
            for (Cookie cookie : cookies) {
                log.info("  Cookie: {} = {}", cookie.getName(), cookie.getValue());
                if ("CART_SESSION_ID".equals(cookie.getName())) {
                    String sessionId = cookie.getValue();
                    log.info("✅ Found existing session cookie: {}", sessionId);

                    // Refresh the cookie
                    Cookie refreshedCookie = new Cookie("CART_SESSION_ID", sessionId);
                    refreshedCookie.setPath("/");
                    refreshedCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                    refreshedCookie.setHttpOnly(true);
                    refreshedCookie.setSecure(false);
                    response.addCookie(refreshedCookie);
                    log.info("✅ Refreshed session cookie: {}", sessionId);

                    return sessionId;
                }
            }
        } else {
            log.info("No cookies found in request");
        }

        // Try to get from header
        String headerSessionId = request.getHeader("X-Session-ID");
        if (headerSessionId != null && !headerSessionId.isEmpty()) {
            log.info("📤 Found session in header: {}", headerSessionId);

            // Set as cookie for future requests
            Cookie sessionCookie = new Cookie("CART_SESSION_ID", headerSessionId);
            sessionCookie.setPath("/");
            sessionCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            sessionCookie.setHttpOnly(true);
            sessionCookie.setSecure(false);
            response.addCookie(sessionCookie);
            log.info("✅ Set cookie from header: {}", headerSessionId);

            return headerSessionId;
        }

        // Create new session ID
        String newSessionId = UUID.randomUUID().toString();
        log.info("🆕 Creating new session ID: {}", newSessionId);

        // Set cookie in response
        Cookie sessionCookie = new Cookie("CART_SESSION_ID", newSessionId);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(false);
        response.addCookie(sessionCookie);

        // Also set as header for debugging/fallback
        response.setHeader("X-Session-ID", newSessionId);

        log.info("✅ New session cookie set: {}", newSessionId);
        log.info("=== getOrCreateSessionId completed ===");

        return newSessionId;
    }
}