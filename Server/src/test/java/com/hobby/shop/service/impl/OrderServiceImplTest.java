package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.OrderRequest;
import com.hobby.shop.dto.response.OrderResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest extends BaseServiceTest {

    @Mock
    private OrderRepository orderRepository;

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
    private OrderServiceImpl orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private Customer customer;
    private Cart cart;
    private Product product1;
    private Product product2;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private Order order;
    private OrderRequest orderRequest;
    private OrderRequest guestOrderRequest;

    private final String email = "test@example.com";
    private final String sessionId = "session-123";
    private final String guestEmail = "guest@example.com";
    private final String orderNumber = "ORD-123456789-ABC123";

    @BeforeEach
    void setUp() {
        customer = createTestCustomer(1L, email);

        product1 = createTestProduct(1L, "Product 1", "SKU001", 10);
        product2 = createTestProduct(2L, "Product 2", "SKU002", 5);

        cart = createTestCart(1L, customer);
        cartItem1 = createTestCartItem(1L, cart, product1, 2);
        cartItem2 = createTestCartItem(2L, cart, product2, 1);
        cart.getItems().add(cartItem1);
        cart.getItems().add(cartItem2);

        order = createTestOrder(1L, customer, orderNumber);

        OrderItem orderItem1 = createTestOrderItem(1L, order, product1, 2);
        OrderItem orderItem2 = createTestOrderItem(2L, order, product2, 1);
        order.getItems().add(orderItem1);
        order.getItems().add(orderItem2);

        orderRequest = createTestOrderRequest();

        guestOrderRequest = createTestGuestOrderRequest(guestEmail);
    }

    // ==================== CREATE ORDER (AUTHENTICATED USER) TESTS ====================

    @Test
    void createOrder_User_Success() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.of(cart));
        when(productService.checkStock(product1.getId(), 2)).thenReturn(true);
        when(productService.checkStock(product2.getId(), 1)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order savedOrder = i.getArgument(0);
            savedOrder.setId(1L);
            savedOrder.setOrderNumber(orderNumber);
            return savedOrder;
        });
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // Act
        OrderResponse response = orderService.createOrder(email, orderRequest);

        // Assert
        assertThat(response).isNotNull();

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getCustomer()).isEqualTo(customer);
        assertThat(savedOrder.getStatus()).isEqualTo("PENDING");
        assertThat(savedOrder.getPaymentStatus()).isEqualTo("PENDING");
        assertThat(savedOrder.getPaymentMethod()).isEqualTo(orderRequest.getPaymentMethod());
        assertThat(savedOrder.getShippingAddress()).isEqualTo(orderRequest.getShippingAddress());

        // Verify stock was reduced
        verify(productRepository, times(2)).save(any(Product.class));

        // Verify cart was saved (after clearing items)
        verify(cartRepository).save(cart);
        assertThat(cart.getItems()).isEmpty();
    }


    @Test
    void createOrder_User_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(email, orderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);
    }

    @Test
    void createOrder_User_CartNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(customer.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createOrder(email, orderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart not found for user");
    }


    // ==================== CREATE GUEST ORDER TESTS ====================

    @Test
    void createGuestOrder_Success() {
        // Arrange
        Cart guestCart = createTestSessionCart(2L, sessionId);
        CartItem guestItem1 = createTestCartItem(3L, guestCart, product1, 2);
        guestCart.getItems().add(guestItem1);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(guestCart));
        when(productService.checkStock(product1.getId(), 2)).thenReturn(true);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order savedOrder = i.getArgument(0);
            savedOrder.setId(2L);
            savedOrder.setOrderNumber("ORD-987654321-DEF456");
            return savedOrder;
        });
        when(cartRepository.save(any(Cart.class))).thenReturn(guestCart);

        // Act
        OrderResponse response = orderService.createGuestOrder(sessionId, guestOrderRequest);

        // Assert
        assertThat(response).isNotNull();

        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getCustomer()).isNull();
        assertThat(savedOrder.getGuestEmail()).isEqualTo(guestEmail);
        assertThat(savedOrder.getStatus()).isEqualTo("PENDING");

        // Verify stock was reduced
        verify(productRepository).save(product1);
        assertThat(product1.getStockQuantity()).isEqualTo(8); // 10 - 2

        // Verify cart was saved (after clearing items)
        verify(cartRepository).save(guestCart);
        assertThat(guestCart.getItems()).isEmpty();
    }

    @Test
    void createGuestOrder_CartNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.createGuestOrder(sessionId, guestOrderRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cart not found for session: " + sessionId);
    }

    @Test
    void createGuestOrder_EmptyCart_ThrowsBadRequestException() {
        // Arrange
        Cart emptyCart = createTestSessionCart(2L, sessionId);
        emptyCart.setItems(new HashSet<>()); // Ensure items are empty

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(emptyCart));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createGuestOrder(sessionId, guestOrderRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot create order with empty cart");
    }

    @Test
    void createGuestOrder_MissingGuestEmail_ThrowsBadRequestException() {
        // Arrange
        Cart guestCart = createTestSessionCart(2L, sessionId);
        CartItem guestItem = createTestCartItem(3L, guestCart, product1, 2);
        guestCart.getItems().add(guestItem);

        OrderRequest invalidRequest = createTestOrderRequest(); // No guest email

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(guestCart));

        // Act & Assert
        assertThatThrownBy(() -> orderService.createGuestOrder(sessionId, invalidRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Guest email is required");
    }

    @Test
    void createGuestOrder_InsufficientStock_ThrowsBadRequestException() {
        // Arrange
        Cart guestCart = createTestSessionCart(2L, sessionId);
        CartItem guestItem = createTestCartItem(3L, guestCart, product1, 2);
        guestCart.getItems().add(guestItem);

        when(cartRepository.findBySessionId(sessionId)).thenReturn(Optional.of(guestCart));
        when(productService.checkStock(product1.getId(), 2)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> orderService.createGuestOrder(sessionId, guestOrderRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock for product: " + product1.getName());

        verify(orderRepository, never()).save(any());
        verify(productRepository, never()).save(any());
    }

    // ==================== GET USER ORDERS TESTS ====================

    @Test
    void getUserOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(order, createAnotherOrder());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findByCustomerId(customer.getId(), pageable)).thenReturn(orderPage);

        // Act
        Page<OrderResponse> result = orderService.getUserOrders(email, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);

        verify(customerRepository).findByEmail(email);
        verify(orderRepository).findByCustomerId(customer.getId(), pageable);
    }

    @Test
    void getUserOrders_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getUserOrders(email, pageable))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: " + email);
    }

    // ==================== GET ORDER BY NUMBER TESTS ====================

    @Test
    void getOrderByNumber_User_Success() {
        // Arrange
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(order));

        // Act
        OrderResponse result = orderService.getOrderByNumber(email, orderNumber);

        // Assert
        assertThat(result).isNotNull();

        verify(customerRepository).findByEmail(email);
        verify(orderRepository).findByOrderNumber(orderNumber);
    }

    @Test
    void getOrderByNumber_User_OrderNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String invalidOrderNumber = "INVALID-123";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findByOrderNumber(invalidOrderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderByNumber(email, invalidOrderNumber))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with number: " + invalidOrderNumber);
    }

    @Test
    void getOrderByNumber_User_OrderBelongsToDifferentUser_ThrowsBadRequestException() {
        // Arrange
        Customer differentCustomer = createTestCustomer(2L, "other@example.com");
        Order otherUserOrder = createTestOrder(2L, differentCustomer, "ORD-OTHER-123");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findByOrderNumber("ORD-OTHER-123")).thenReturn(Optional.of(otherUserOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrderByNumber(email, "ORD-OTHER-123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Order does not belong to this user");
    }

    // ==================== GET GUEST ORDER TESTS ====================

    @Test
    void getGuestOrder_Success() {
        // Arrange
        Order guestOrder = createTestGuestOrder(2L, guestEmail, "ORD-GUEST-123");

        when(orderRepository.findByOrderNumber("ORD-GUEST-123")).thenReturn(Optional.of(guestOrder));

        // Act
        OrderResponse result = orderService.getGuestOrder("ORD-GUEST-123", guestEmail);

        // Assert
        assertThat(result).isNotNull();
        verify(orderRepository).findByOrderNumber("ORD-GUEST-123");
    }

    @Test
    void getGuestOrder_OrderNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String invalidOrderNumber = "INVALID-123";
        when(orderRepository.findByOrderNumber(invalidOrderNumber)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.getGuestOrder(invalidOrderNumber, guestEmail))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with number: " + invalidOrderNumber);
    }

    @Test
    void getGuestOrder_EmailMismatch_ThrowsBadRequestException() {
        // Arrange
        Order guestOrder = createTestGuestOrder(2L, guestEmail, "ORD-GUEST-123");
        String wrongEmail = "wrong@example.com";

        when(orderRepository.findByOrderNumber("ORD-GUEST-123")).thenReturn(Optional.of(guestOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.getGuestOrder("ORD-GUEST-123", wrongEmail))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Order not found for this email");
    }

    // ==================== CANCEL ORDER TESTS ====================

    @Test
    void cancelOrder_Success() {
        // Arrange
        String cancelReason = "Changed my mind";

        // Set initial stock values to what they would be after order placement
        product1.setStockQuantity(8); // After order was placed (10-2)
        product2.setStockQuantity(4); // After order was placed (5-1)

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = orderService.cancelOrder(email, order.getId(), cancelReason);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo("CANCELLED");

        // Verify stock was restored - should go back to original values
        verify(productRepository, times(2)).save(any(Product.class));

        // After restoration, stock should be back to original
        assertThat(product1.getStockQuantity()).isEqualTo(10); // Restored from 8 to 10
        assertThat(product2.getStockQuantity()).isEqualTo(5);  // Restored from 4 to 5
    }

    @Test
    void cancelOrder_OrderNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(email, invalidId, "reason"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Order not found with id: " + invalidId);
    }

    @Test
    void cancelOrder_OrderBelongsToDifferentUser_ThrowsBadRequestException() {
        // Arrange
        Customer differentCustomer = createTestCustomer(2L, "other@example.com");
        Order otherUserOrder = createTestOrder(2L, differentCustomer, "ORD-OTHER-123");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(otherUserOrder.getId())).thenReturn(Optional.of(otherUserOrder));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(email, otherUserOrder.getId(), "reason"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Order does not belong to this user");
    }

    @Test
    void cancelOrder_OrderNotPending_ThrowsBadRequestException() {
        // Arrange
        order.setStatus("SHIPPED");

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> orderService.cancelOrder(email, order.getId(), "reason"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only pending orders can be cancelled");

        verify(productRepository, never()).save(any());
    }

    // ==================== ADMIN METHODS TESTS ====================

    @Test
    void getAllOrders_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(order, createAnotherOrder());
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // Act
        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    void getOrdersByStatus_Success() {
        // Arrange
        String status = "PENDING";
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> pendingOrders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(pendingOrders, pageable, pendingOrders.size());

        when(orderRepository.findByStatus(status, pageable)).thenReturn(orderPage);

        // Act
        Page<OrderResponse> result = orderService.getOrdersByStatus(status, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByStatus(status, pageable);
    }

    @Test
    void updateOrderStatus_Success() {
        // Arrange
        Long orderId = order.getId();
        String newStatus = "PROCESSING";
        String comment = "Processing order";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = orderService.updateOrderStatus(orderId, newStatus, comment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(newStatus);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    void updatePaymentStatus_Success() {
        // Arrange
        Long orderId = order.getId();
        String newPaymentStatus = "PAID";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = orderService.updatePaymentStatus(orderId, newPaymentStatus);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getPaymentStatus()).isEqualTo(newPaymentStatus);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    void updateTrackingNumber_Success() {
        // Arrange
        Long orderId = order.getId();
        String trackingNumber = "TRACK-123456";

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        OrderResponse result = orderService.updateTrackingNumber(orderId, trackingNumber);

        // Assert
        assertThat(result).isNotNull();
        assertThat(order.getTrackingNumber()).isEqualTo(trackingNumber);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private Order createAnotherOrder() {
        Customer anotherCustomer = createTestCustomer(2L, "another@example.com");
        Order anotherOrder = createTestOrder(2L, anotherCustomer, "ORD-ANOTHER-123");

        Product anotherProduct = createTestProduct(3L, "Another Product", "SKU003", 8);
        OrderItem anotherItem = createTestOrderItem(3L, anotherOrder, anotherProduct, 1);
        anotherOrder.getItems().add(anotherItem);

        return anotherOrder;
    }
}