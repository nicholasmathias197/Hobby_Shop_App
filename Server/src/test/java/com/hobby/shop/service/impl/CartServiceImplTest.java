package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.CartItemRequest;
import com.hobby.shop.dto.response.CartResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.model.*;
import com.hobby.shop.repository.*;
import com.hobby.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest extends BaseServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    @Captor
    private ArgumentCaptor<Cart> cartCaptor;

    @Captor
    private ArgumentCaptor<CartItem> cartItemCaptor;

    private Customer customer;
    private Cart userCart;
    private Cart sessionCart;
    private Product product;
    private Product product2;
    private CartItem cartItem;
    private CartItemRequest cartItemRequest;
    private final String email = "test@example.com";
    private final String sessionId = "session-123";
    private final Long productId = 1L;

    @BeforeEach
    void setUp() {
        customer = createTestCustomer(1L, email);

        product = createTestProduct(productId, "Test Product", "SKU123", 10);
        product2 = createTestProduct(2L, "Product 2", "SKU456", 5);

        userCart = createTestCart(1L, customer);

        sessionCart = createTestSessionCart(2L, sessionId);

        cartItem = createTestCartItem(1L, userCart, product, 2);
        userCart.getItems().add(cartItem);

        cartItemRequest = new CartItemRequest();
        cartItemRequest.setProductId(productId);
        cartItemRequest.setQuantity(2);
    }

    // ==================== USER CART TESTS ====================

    @Test
    void getCartByUser_ExistingCart_Success() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.getCartByUser(email);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(userCart.getId());
        assertThat(response.getCustomerId()).isEqualTo(customer.getId());

        verify(customerRepository).findByEmail(email);
        verify(cartRepository).findByCustomerId(customer.getId());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getCartByUser_NoCart_CreatesNewCart() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart newCart = invocation.getArgument(0);
            newCart.setId(3L);
            return newCart;
        });

        // Act
        CartResponse response = cartService.getCartByUser(email);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(3L);

        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertThat(savedCart.getCustomer()).isEqualTo(customer);
        assertThat(savedCart.getItems()).isEmpty();
    }

    @Test
    void getCartByUser_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.getCartByUser(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);
    }

    @Test
    void addItemToUserCart_NewItem_Success() {
        // Arrange
        Product newProduct = createTestProduct(2L, "New Product", "SKU456", 5);
        CartItemRequest request = new CartItemRequest();
        request.setProductId(2L);
        request.setQuantity(3);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));
        when(productService.checkStock(2L, 3)).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(2L);
            return item;
        });
        when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.addItemToUserCart(email, request);

        // Assert
        assertThat(response).isNotNull();

        verify(cartItemRepository).save(cartItemCaptor.capture());
        CartItem savedItem = cartItemCaptor.getValue();
        assertThat(savedItem.getProduct()).isEqualTo(newProduct);
        assertThat(savedItem.getQuantity()).isEqualTo(3);
        assertThat(savedItem.getCart()).isEqualTo(userCart);
    }

    @Test
    void addItemToUserCart_ExistingItem_UpdatesQuantity() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productService.checkStock(productId, 2)).thenReturn(true);

        // For existing item, we need to set up the cart with the item
        userCart.getItems().clear();
        userCart.getItems().add(cartItem);

        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.addItemToUserCart(email, cartItemRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(cartItemRepository).save(cartItem);
        assertThat(cartItem.getQuantity()).isEqualTo(4); // 2 + 2
    }

    @Test
    void addItemToUserCart_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToUserCart(email, cartItemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + productId);
    }

    @Test
    void addItemToUserCart_ProductInactive_ThrowsBadRequestException() {
        // Arrange
        product.setIsActive(false);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToUserCart(email, cartItemRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product is not available");
    }

    @Test
    void addItemToUserCart_InsufficientStock_ThrowsBadRequestException() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productService.checkStock(productId, 2)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> cartService.addItemToUserCart(email, cartItemRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void updateCartItemQuantity_IncreaseQuantity_Success() {
        // Arrange
        int newQuantity = 5;

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(productService.checkStock(productId, newQuantity)).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.updateCartItemQuantity(email, cartItem.getId(), newQuantity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(newQuantity);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateCartItemQuantity_DecreaseQuantity_Success() {
        // Arrange
        int newQuantity = 1;

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        when(productService.checkStock(productId, newQuantity)).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);
        when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.updateCartItemQuantity(email, cartItem.getId(), newQuantity);

        // Assert
        assertThat(response).isNotNull();
        assertThat(cartItem.getQuantity()).isEqualTo(newQuantity);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateCartItemQuantity_RemoveItem_WhenQuantityZero() {
        // Arrange
        int newQuantity = 0;

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);
        when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.updateCartItemQuantity(email, cartItem.getId(), newQuantity);

        // Assert
        assertThat(response).isNotNull();
        verify(cartItemRepository).delete(cartItem);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void updateCartItemQuantity_ItemNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidItemId = 999L;

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.updateCartItemQuantity(email, invalidItemId, 3))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found with id: " + invalidItemId);
    }

    @Test
    void updateCartItemQuantity_ItemNotInUsersCart_ThrowsBadRequestException() {
        // Arrange
        Cart otherUserCart = createTestCart(3L, createTestCustomer(2L, "other@example.com"));
        CartItem otherCartItem = createTestCartItem(2L, otherUserCart, product, 1);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(otherCartItem.getId())).thenReturn(Optional.of(otherCartItem));

        // Act & Assert
        assertThatThrownBy(() -> cartService.updateCartItemQuantity(email, otherCartItem.getId(), 3))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cart item does not belong to this user");
    }

    @Test
    void removeItemFromUserCart_Success() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(cartItem.getId())).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(userCart);
        when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.removeItemFromUserCart(email, cartItem.getId());

        // Assert
        assertThat(response).isNotNull();
        verify(cartItemRepository).delete(cartItem);
        verify(cartRepository).save(userCart);
    }

    @Test
    void removeItemFromUserCart_ItemNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidItemId = 999L;

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartItemRepository.findById(invalidItemId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.removeItemFromUserCart(email, invalidItemId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart item not found with id: " + invalidItemId);
    }

    @Test
    void clearUserCart_Success() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        doNothing().when(cartItemRepository).deleteAll(userCart.getItems());
        when(cartRepository.save(any(Cart.class))).thenReturn(userCart);

        // Act
        cartService.clearUserCart(email);

        // Assert
        verify(cartItemRepository).deleteAll(userCart.getItems());
        verify(cartRepository).save(userCart);
        assertThat(userCart.getItems()).isEmpty();
    }

    @Test
    void clearUserCart_NoCart_DoesNothing() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.empty());

        // Act
        cartService.clearUserCart(email);

        // Assert
        verify(cartItemRepository, never()).deleteAll(any());
        verify(cartRepository, never()).save(any());
    }

    // ==================== SESSION CART TESTS ====================

    @Test
    void getCartBySession_ExistingCart_Success() {
        // Arrange
        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));

        // Act
        CartResponse response = cartService.getCartBySession(sessionId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getSessionId()).isEqualTo(sessionId);
        verify(cartRepository).findBySessionId(sessionId);
    }

    @Test
    void getCartBySession_NoCart_CreatesNewCart() {
        // Arrange
        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart newCart = invocation.getArgument(0);
            newCart.setId(3L);
            return newCart;
        });

        // Act
        CartResponse response = cartService.getCartBySession(sessionId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(3L);
        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertThat(savedCart.getSessionId()).isEqualTo(sessionId);
    }

    @Test
    void addItemToSessionCart_Success() {
        // Arrange
        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productService.checkStock(productId, 2)).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem item = invocation.getArgument(0);
            item.setId(2L);
            return item;
        });
        when(cartRepository.findById(sessionCart.getId())).thenReturn(Optional.of(sessionCart));

        // Act
        CartResponse response = cartService.addItemToSessionCart(sessionId, cartItemRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateSessionCartItemQuantity_Success() {
        // Arrange
        CartItem sessionCartItem = createTestCartItem(2L, sessionCart, product, 2);
        sessionCart.getItems().add(sessionCartItem);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));
        when(cartItemRepository.findById(sessionCartItem.getId())).thenReturn(Optional.of(sessionCartItem));
        when(productService.checkStock(productId, 3)).thenReturn(true);
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(sessionCartItem);
        when(cartRepository.findById(sessionCart.getId())).thenReturn(Optional.of(sessionCart));

        // Act
        CartResponse response = cartService.updateSessionCartItemQuantity(sessionId, sessionCartItem.getId(), 3);

        // Assert
        assertThat(response).isNotNull();
        assertThat(sessionCartItem.getQuantity()).isEqualTo(3);
        verify(cartItemRepository).save(sessionCartItem);
    }

    @Test
    void removeItemFromSessionCart_Success() {
        // Arrange
        CartItem sessionCartItem = createTestCartItem(2L, sessionCart, product, 2);
        sessionCart.getItems().add(sessionCartItem);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));
        when(cartItemRepository.findById(sessionCartItem.getId())).thenReturn(Optional.of(sessionCartItem));
        doNothing().when(cartItemRepository).delete(sessionCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(sessionCart);
        when(cartRepository.findById(sessionCart.getId())).thenReturn(Optional.of(sessionCart));

        // Act
        CartResponse response = cartService.removeItemFromSessionCart(sessionId, sessionCartItem.getId());

        // Assert
        assertThat(response).isNotNull();
        verify(cartItemRepository).delete(sessionCartItem);
    }

    @Test
    void clearSessionCart_Success() {
        // Arrange
        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));
        doNothing().when(cartItemRepository).deleteAll(sessionCart.getItems());
        when(cartRepository.save(any(Cart.class))).thenReturn(sessionCart);

        // Act
        cartService.clearSessionCart(sessionId);

        // Assert
        verify(cartItemRepository).deleteAll(sessionCart.getItems());
        verify(cartRepository).save(sessionCart);
    }

    // ==================== CART MERGING TESTS ====================

    @Test
    void mergeCarts_UserCartAndSessionCart_MergesSuccessfully() {
        // Arrange
        Cart userEmptyCart = createTestCart(1L, customer);

        // Clear any existing items
        userEmptyCart.getItems().clear();
        sessionCart.getItems().clear();

        CartItem sessionItem1 = createTestCartItem(2L, sessionCart, product, 2);
        CartItem sessionItem2 = createTestCartItem(3L, sessionCart, product2, 1);
        sessionCart.getItems().add(sessionItem1);
        sessionCart.getItems().add(sessionItem2);

        lenient().when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        lenient().when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userEmptyCart));
        lenient().when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));
        lenient().when(cartRepository.findById(userEmptyCart.getId())).thenReturn(Optional.of(userEmptyCart));

        // Act
        CartResponse response = cartService.mergeCarts(email, sessionId);

        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository).delete(sessionCart);
        assertThat(userEmptyCart.getItems()).hasSize(2);
    }

    @Test
    void mergeCarts_UserCartWithExistingItems_MergesWithQuantityUpdate() {
        // Arrange
        CartItem userItem = createTestCartItem(1L, userCart, product, 2);
        userCart.getItems().clear();
        userCart.getItems().add(userItem);

        CartItem sessionItem = createTestCartItem(2L, sessionCart, product, 3); // Same product
        sessionCart.getItems().clear();
        sessionCart.getItems().add(sessionItem);

        lenient().when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        lenient().when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        lenient().when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));
        lenient().when(cartRepository.findById(userCart.getId())).thenReturn(Optional.of(userCart));

        // Act
        CartResponse response = cartService.mergeCarts(email, sessionId);

        // Assert
        assertThat(response).isNotNull();
        // Should update existing item (2 + 3 = 5), not create new one
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        assertThat(userItem.getQuantity()).isEqualTo(5); // 2 + 3
    }

    @Test
    void mergeCarts_NoSessionCart_ReturnsUserCart() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));
        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // Act
        CartResponse response = cartService.mergeCarts(email, sessionId);

        // Assert
        assertThat(response).isNotNull();
        verify(cartRepository, never()).delete(any());
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void mergeCarts_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cartService.mergeCarts(email, sessionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);
    }

    // ==================== CART ITEM COUNT TESTS ====================

    @Test
    void getCartItemCount_ForUser_ReturnsCount() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(userCart));

        // Act
        int count = cartService.getCartItemCount(email, true);

        // Assert
        assertThat(count).isEqualTo(2); // cartItem has quantity 2
    }

    @Test
    void getCartItemCount_ForUser_NoCart_ReturnsZero() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.empty());

        // Act
        int count = cartService.getCartItemCount(email, true);

        // Assert
        assertThat(count).isZero();
    }

    @Test
    void getCartItemCount_ForUser_UserNotFound_ReturnsZero() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        int count = cartService.getCartItemCount(email, true);

        // Assert
        assertThat(count).isZero();
    }

    @Test
    void getCartItemCount_ForSession_ReturnsCount() {
        // Arrange
        CartItem sessionItem = createTestCartItem(2L, sessionCart, product, 3);
        sessionCart.getItems().add(sessionItem);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(sessionCart));

        // Act
        int count = cartService.getCartItemCount(sessionId, false);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    @Test
    void getCartItemCount_ForSession_NoCart_ReturnsZero() {
        // Arrange
        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // Act
        int count = cartService.getCartItemCount(sessionId, false);

        // Assert
        assertThat(count).isZero();
    }
}