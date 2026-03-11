package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.CartItemRequest;
import com.hobby.shop.dto.response.CartResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.model.*;
import com.hobby.shop.repository.*;
import com.hobby.shop.service.CartService;
import com.hobby.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;

/**
 * Implementation of the CartService interface.
 * Manages shopping cart operations for both authenticated users and guest sessions.
 *
 * This service handles:
 * - Cart creation and retrieval for users and guests
 * - Adding, updating, and removing items from carts
 * - Cart merging when guests log in
 * - Stock validation during cart operations
 *
 * @author Hobby Shop Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    // ==================== USER CART METHODS ====================

    /**
     * Retrieves the cart for an authenticated user.
     * If the user doesn't have a cart, a new one is created.
     *
     * @param email the email of the authenticated user
     * @return CartResponse containing cart details
     * @throws ResourceNotFoundException if user not found
     */
    @Override
    public CartResponse getCartByUser(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> createNewCartForUser(customer));

        return mapToCartResponse(cart);
    }

    /**
     * Adds an item to an authenticated user's cart.
     * If the product already exists in the cart, the quantity is increased.
     *
     * @param email the email of the authenticated user
     * @param request the cart item request containing product ID and quantity
     * @return CartResponse with updated cart
     * @throws ResourceNotFoundException if user or product not found
     * @throws BadRequestException if product is inactive or insufficient stock
     */
    @Override
    public CartResponse addItemToUserCart(String email, CartItemRequest request) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> createNewCartForUser(customer));

        return addItemToCart(cart, request);
    }

    /**
     * Updates the quantity of an item in a user's cart.
     * If quantity becomes zero or negative, the item is removed.
     *
     * @param email the email of the authenticated user
     * @param cartItemId the ID of the cart item to update
     * @param quantity the new quantity
     * @return CartResponse with updated cart
     * @throws ResourceNotFoundException if user, cart, or cart item not found
     * @throws BadRequestException if item doesn't belong to user or insufficient stock
     */
    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(String email, Long cartItemId, Integer quantity) {
        log.info("=== UPDATE CART ITEM QUANTITY (USER) ===");
        log.info("Email: {}, CartItemId: {}, Quantity: {}", email, cartItemId, quantity);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        // Verify item belongs to user's cart
        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this user");
        }

        if (quantity <= 0) {
            log.info("Quantity <= 0, removing item from cart");
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            cartItemRepository.flush();
        } else {
            // Check stock availability before updating quantity
            if (!productService.checkStock(cartItem.getProduct().getId(), quantity)) {
                throw new BadRequestException("Insufficient stock for product: " + cartItem.getProduct().getName());
            }
            log.info("Updating quantity from {} to {}", cartItem.getQuantity(), quantity);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            cartItemRepository.flush();
        }

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    /**
     * Removes an item from a user's cart.
     *
     * @param email the email of the authenticated user
     * @param cartItemId the ID of the cart item to remove
     * @return CartResponse with updated cart
     * @throws ResourceNotFoundException if user, cart, or cart item not found
     * @throws BadRequestException if item doesn't belong to user
     */
    @Override
    @Transactional
    public CartResponse removeItemFromUserCart(String email, Long cartItemId) {
        log.info("=== REMOVE ITEM FROM USER CART ===");
        log.info("Email: {}", email);
        log.info("CartItemId: {}", cartItemId);

        try {
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

            Cart cart = cartRepository.findByCustomerId(customer.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
            log.info("Found cart with ID: {}, Items before deletion: {}", cart.getId(), cart.getItems().size());

            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
            log.info("Found cart item: ID={}, Product={}, Quantity={}",
                    cartItem.getId(), cartItem.getProduct().getName(), cartItem.getQuantity());

            // Verify item belongs to user's cart
            if (!cartItem.getCart().getId().equals(cart.getId())) {
                log.error("Cart item {} does not belong to cart {}", cartItemId, cart.getId());
                throw new BadRequestException("Cart item does not belong to this user");
            }
            log.info("Item belongs to cart ✓");

            // Remove from cart's collection to maintain consistency
            cart.getItems().remove(cartItem);
            log.info("Removed item from cart's collection");

            // Delete the item
            log.info("Deleting cart item from database...");
            cartItemRepository.delete(cartItem);
            log.info("Delete method called");

            // Force flush to ensure deletion is committed
            cartItemRepository.flush();
            log.info("Flush completed");

            // Save cart to update the collection
            cart = cartRepository.save(cart);
            log.info("Cart saved. Items after deletion: {}", cart.getItems().size());

            // Refresh to ensure we have latest state
            cart = cartRepository.findById(cart.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
            log.info("Cart refreshed. Final item count: {}", cart.getItems().size());

            CartResponse response = mapToCartResponse(cart);
            log.info("Returning response with {} items", response.getItems().size());

            return response;

        } catch (Exception e) {
            log.error("Error removing item from user cart: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Clears all items from a user's cart.
     *
     * @param email the email of the authenticated user
     * @throws ResourceNotFoundException if user not found
     */
    @Override
    @Transactional
    public void clearUserCart(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        cartRepository.findByCustomerId(customer.getId()).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
            cartItemRepository.flush();
        });
    }

    // ==================== SESSION CART METHODS ====================

    /**
     * Retrieves the cart for a guest session.
     * If the session doesn't have a cart, a new one is created.
     *
     * @param sessionId the session ID for the guest
     * @return CartResponse containing cart details
     */
    @Override
    public CartResponse getCartBySession(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewCartForSession(sessionId));

        return mapToCartResponse(cart);
    }

    /**
     * Adds an item to a guest session cart.
     *
     * @param sessionId the session ID for the guest
     * @param request the cart item request
     * @return CartResponse with updated cart
     * @throws ResourceNotFoundException if product not found
     * @throws BadRequestException if product is inactive or insufficient stock
     */
    @Override
    public CartResponse addItemToSessionCart(String sessionId, CartItemRequest request) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewCartForSession(sessionId));

        return addItemToCart(cart, request);
    }

    /**
     * Updates the quantity of an item in a guest session cart.
     *
     * @param sessionId the session ID for the guest
     * @param cartItemId the ID of the cart item to update
     * @param quantity the new quantity
     * @return CartResponse with updated cart
     * @throws ResourceNotFoundException if cart or cart item not found
     * @throws BadRequestException if item doesn't belong to session or insufficient stock
     */
    @Override
    @Transactional
    public CartResponse updateSessionCartItemQuantity(String sessionId, Long cartItemId, Integer quantity) {
        log.info("=== UPDATE CART ITEM QUANTITY (SESSION) ===");
        log.info("SessionId: {}, CartItemId: {}, Quantity: {}", sessionId, cartItemId, quantity);

        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for session"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this session");
        }

        if (quantity <= 0) {
            log.info("Quantity <= 0, removing item from cart");
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            cartItemRepository.flush();
        } else {
            // Check stock availability
            if (!productService.checkStock(cartItem.getProduct().getId(), quantity)) {
                throw new BadRequestException("Insufficient stock for product: " + cartItem.getProduct().getName());
            }
            log.info("Updating quantity from {} to {}", cartItem.getQuantity(), quantity);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
            cartItemRepository.flush();
        }

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    /**
     * Removes an item from a guest session cart.
     *
     * @param sessionId the session ID for the guest
     * @param cartItemId the ID of the cart item to remove
     * @return CartResponse with updated cart
     * @throws ResourceNotFoundException if cart or cart item not found
     * @throws BadRequestException if item doesn't belong to session
     */
    @Override
    @Transactional
    public CartResponse removeItemFromSessionCart(String sessionId, Long cartItemId) {
        log.info("=== REMOVE ITEM FROM SESSION CART ===");
        log.info("SessionId: {}", sessionId);
        log.info("CartItemId: {}", cartItemId);

        try {
            // Find cart
            Cart cart = cartRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found for session"));
            log.info("Found cart with ID: {}, Items before deletion: {}", cart.getId(), cart.getItems().size());

            // Find cart item
            CartItem cartItem = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));
            log.info("Found cart item: ID={}, Product={}, Quantity={}",
                    cartItem.getId(), cartItem.getProduct().getName(), cartItem.getQuantity());

            // Verify item belongs to cart
            if (!cartItem.getCart().getId().equals(cart.getId())) {
                log.error("Cart item {} does not belong to cart {}", cartItemId, cart.getId());
                throw new BadRequestException("Cart item does not belong to this session");
            }
            log.info("Item belongs to cart ✓");

            // Remove from cart's collection to maintain consistency
            cart.getItems().remove(cartItem);
            log.info("Removed item from cart's collection");

            // Delete the item
            log.info("Deleting cart item from database...");
            cartItemRepository.delete(cartItem);
            log.info("Delete method called");

            // Force flush to ensure deletion is committed
            cartItemRepository.flush();
            log.info("Flush completed");

            // Save cart to update the collection
            cart = cartRepository.save(cart);
            log.info("Cart saved. Items after deletion: {}", cart.getItems().size());

            // Refresh to ensure we have latest state
            cart = cartRepository.findById(cart.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
            log.info("Cart refreshed. Final item count: {}", cart.getItems().size());

            CartResponse response = mapToCartResponse(cart);
            log.info("Returning response with {} items", response.getItems().size());

            return response;

        } catch (Exception e) {
            log.error("Error removing item from session cart: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Clears all items from a guest session cart.
     *
     * @param sessionId the session ID for the guest
     */
    @Override
    @Transactional
    public void clearSessionCart(String sessionId) {
        cartRepository.findBySessionId(sessionId).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
            cartItemRepository.flush();
        });
    }

    // ==================== CART MERGING ====================

    /**
     * Merges a guest session cart into a user's cart when the guest logs in.
     *
     * Process flow:
     * 1. Get or create user cart
     * 2. If session cart exists, merge its items into user cart
     * 3. Delete the session cart after merging
     *
     * @param email the email of the authenticated user
     * @param sessionId the guest session ID
     * @return CartResponse of the merged user cart
     * @throws ResourceNotFoundException if user not found
     */
    @Override
    @Transactional
    public CartResponse mergeCarts(String email, String sessionId) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart userCart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> createNewCartForUser(customer));

        Optional<Cart> sessionCartOpt = cartRepository.findBySessionId(sessionId);

        if (sessionCartOpt.isPresent()) {
            Cart sessionCart = sessionCartOpt.get();

            // Merge session cart items into user cart
            for (CartItem sessionItem : sessionCart.getItems()) {
                Optional<CartItem> existingItem = userCart.getItems().stream()
                        .filter(item -> item.getProduct().getId().equals(sessionItem.getProduct().getId()))
                        .findFirst();

                if (existingItem.isPresent()) {
                    // Update quantity if product already in cart
                    CartItem item = existingItem.get();
                    item.setQuantity(item.getQuantity() + sessionItem.getQuantity());
                    cartItemRepository.save(item);
                } else {
                    // Add new item to user cart
                    CartItem newItem = new CartItem();
                    newItem.setCart(userCart);
                    newItem.setProduct(sessionItem.getProduct());
                    newItem.setQuantity(sessionItem.getQuantity());
                    cartItemRepository.save(newItem);
                    userCart.getItems().add(newItem);
                }
            }

            // Delete session cart after merge
            cartRepository.delete(sessionCart);
            cartRepository.flush();
        }

        return mapToCartResponse(userCart);
    }

    /**
     * Gets the total number of items in a cart.
     *
     * @param cartIdentifier either email (for users) or sessionId (for guests)
     * @param isUser true if cartIdentifier is an email, false if it's a sessionId
     * @return total quantity of items in the cart
     */
    @Override
    public int getCartItemCount(String cartIdentifier, boolean isUser) {
        Optional<Cart> cartOpt;

        if (isUser) {
            Customer customer = customerRepository.findByEmail(cartIdentifier)
                    .orElse(null);
            if (customer == null) return 0;
            cartOpt = cartRepository.findByCustomerId(customer.getId());
        } else {
            cartOpt = cartRepository.findBySessionId(cartIdentifier);
        }

        return cartOpt.map(cart -> cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum()).orElse(0);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Creates a new cart for an authenticated user.
     *
     * @param customer the customer to associate with the cart
     * @return the newly created Cart entity
     */
    private Cart createNewCartForUser(Customer customer) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setItems(new HashSet<>());
        return cartRepository.save(cart);
    }

    /**
     * Creates a new cart for a guest session.
     *
     * @param sessionId the session ID to associate with the cart
     * @return the newly created Cart entity
     */
    private Cart createNewCartForSession(String sessionId) {
        Cart cart = new Cart();
        cart.setSessionId(sessionId);
        cart.setItems(new HashSet<>());
        return cartRepository.save(cart);
    }

    /**
     * Adds an item to a cart with validation.
     *
     * @param cart the cart to add the item to
     * @param request the item request containing product ID and quantity
     * @return CartResponse with updated cart
     * @throws ResourceNotFoundException if product not found
     * @throws BadRequestException if product is inactive or insufficient stock
     */
    private CartResponse addItemToCart(Cart cart, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        // Check if product is active
        if (!product.getIsActive()) {
            throw new BadRequestException("Product is not available: " + product.getName());
        }

        // Check stock availability
        if (!productService.checkStock(product.getId(), request.getQuantity())) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity if product exists
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
            cart.getItems().add(cartItem);
        }

        cartItemRepository.flush();

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    /**
     * Maps a Cart entity to a CartResponse DTO.
     * Calculates total items and total price.
     *
     * @param cart the Cart entity to map
     * @return CartResponse containing cart details and calculated totals
     */
    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null);
        response.setSessionId(cart.getSessionId());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        // IMPORTANT: Initialize the items set
        response.setItems(new HashSet<>());

        int totalItems = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                CartResponse.CartItemResponse itemResponse = new CartResponse.CartItemResponse();
                itemResponse.setId(item.getId());
                itemResponse.setProductId(item.getProduct().getId());
                itemResponse.setProductName(item.getProduct().getName());
                itemResponse.setProductSku(item.getProduct().getSku());
                itemResponse.setProductImage(item.getProduct().getImageUrl());
                itemResponse.setPrice(item.getProduct().getPrice());
                itemResponse.setQuantity(item.getQuantity());

                BigDecimal subtotal = item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                itemResponse.setSubtotal(subtotal);

                response.getItems().add(itemResponse);

                totalItems += item.getQuantity();
                totalPrice = totalPrice.add(subtotal);
            }
        }

        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);

        return response;
    }
}