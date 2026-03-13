package com.hobby.shop.service;

import com.hobby.shop.dto.request.CartItemRequest;
import com.hobby.shop.dto.response.CartResponse;

public interface CartService {

    // User cart operations
    CartResponse getCartByUser(String email);

    CartResponse addItemToUserCart(String email, CartItemRequest request);

    CartResponse updateCartItemQuantity(String email, Long cartItemId, Integer quantity);

    CartResponse removeItemFromUserCart(String email, Long cartItemId);

    void clearUserCart(String email);

    // Session/guest cart operations
    CartResponse getCartBySession(String sessionId);

    CartResponse addItemToSessionCart(String sessionId, CartItemRequest request);

    CartResponse updateSessionCartItemQuantity(String sessionId, Long cartItemId, Integer quantity);

    CartResponse removeItemFromSessionCart(String sessionId, Long cartItemId);

    void clearSessionCart(String sessionId);

    // Merge cart after login
    CartResponse mergeCarts(String email, String sessionId);

    // Cart calculations
    int getCartItemCount(String cartIdentifier, boolean isUser);
}