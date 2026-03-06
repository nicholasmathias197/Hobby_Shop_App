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

    @Override
    public CartResponse getCartByUser(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> createNewCartForUser(customer));

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addItemToUserCart(String email, CartItemRequest request) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> createNewCartForUser(customer));

        return addItemToCart(cart, request);
    }

    @Override
    public CartResponse updateCartItemQuantity(String email, Long cartItemId, Integer quantity) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this user");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            // Check stock availability
            if (!productService.checkStock(cartItem.getProduct().getId(), quantity)) {
                throw new BadRequestException("Insufficient stock for product: " + cartItem.getProduct().getName());
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeItemFromUserCart(String email, Long cartItemId) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this user");
        }

        cartItemRepository.delete(cartItem);

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    @Override
    public void clearUserCart(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        cartRepository.findByCustomerId(customer.getId()).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    @Override
    public CartResponse getCartBySession(String sessionId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewCartForSession(sessionId));

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse addItemToSessionCart(String sessionId, CartItemRequest request) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> createNewCartForSession(sessionId));

        return addItemToCart(cart, request);
    }

    @Override
    public CartResponse updateSessionCartItemQuantity(String sessionId, Long cartItemId, Integer quantity) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for session"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this session");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            // Check stock availability
            if (!productService.checkStock(cartItem.getProduct().getId(), quantity)) {
                throw new BadRequestException("Insufficient stock for product: " + cartItem.getProduct().getName());
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponse removeItemFromSessionCart(String sessionId, Long cartItemId) {
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for session"));

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this session");
        }

        cartItemRepository.delete(cartItem);

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    @Override
    public void clearSessionCart(String sessionId) {
        cartRepository.findBySessionId(sessionId).ifPresent(cart -> {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    @Override
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
        }

        return mapToCartResponse(userCart);
    }

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

    // Private helper methods

    private Cart createNewCartForUser(Customer customer) {
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.setItems(new HashSet<>());
        return cartRepository.save(cart);
    }

    private Cart createNewCartForSession(String sessionId) {
        Cart cart = new Cart();
        cart.setSessionId(sessionId);
        cart.setItems(new HashSet<>());
        return cartRepository.save(cart);
    }

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
            // Update quantity
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

        cart = cartRepository.findById(cart.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        return mapToCartResponse(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null);
        response.setSessionId(cart.getSessionId());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());

        int totalItems = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

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

        response.setTotalItems(totalItems);
        response.setTotalPrice(totalPrice);

        return response;
    }
}