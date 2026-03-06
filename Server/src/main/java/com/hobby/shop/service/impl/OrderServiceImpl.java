package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.OrderRequest;
import com.hobby.shop.dto.response.OrderResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.model.*;
import com.hobby.shop.repository.*;
import com.hobby.shop.service.CartService;
import com.hobby.shop.service.OrderService;
import com.hobby.shop.service.ProductService;
import com.hobby.shop.util.AppConstants;
import com.hobby.shop.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final CartService cartService;
    private final OrderNumberGenerator orderNumberGenerator;

    @Override
    public OrderResponse createOrder(String email, OrderRequest request) {
        log.info("Creating order for user: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Get user's cart to verify items
        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BadRequestException("No items in cart"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot create order with empty cart");
        }

        // Validate and prepare order items from cart
        validateStockForCartItems(cart.getItems());

        Order order = buildOrderFromRequest(request, customer, null);
        order = orderRepository.save(order);

        // Create order items from cart
        List<OrderItem> orderItems = createOrderItemsFromCart(order, cart);
        order.setItems(orderItems);

        // Update totals
        calculateOrderTotals(order, orderItems);

        // Save order items
        orderItemRepository.saveAll(orderItems);

        // Update product stock
        updateProductStock(orderItems);

        // Clear the cart
        cartService.clearUserCart(email);

        // Create order status history
        createOrderStatusHistory(order, AppConstants.ORDER_STATUS_PENDING, "Order placed successfully");

        log.info("Order created successfully with number: {}", order.getOrderNumber());

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse createGuestOrder(String sessionId, OrderRequest request, String guestEmail) {
        log.info("Creating guest order for session: {}", sessionId);

        // Get guest cart
        Cart cart = cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BadRequestException("No items in cart"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot create order with empty cart");
        }

        // Validate stock
        validateStockForCartItems(cart.getItems());

        Order order = buildOrderFromRequest(request, null, guestEmail);
        order = orderRepository.save(order);

        // Create order items from cart
        List<OrderItem> orderItems = createOrderItemsFromCart(order, cart);
        order.setItems(orderItems);

        // Update totals
        calculateOrderTotals(order, orderItems);

        // Save order items
        orderItemRepository.saveAll(orderItems);

        // Update product stock
        updateProductStock(orderItems);

        // Clear the guest cart
        cartService.clearSessionCart(sessionId);

        // Create order status history
        createOrderStatusHistory(order, AppConstants.ORDER_STATUS_PENDING, "Guest order placed successfully");

        log.info("Guest order created successfully with number: {}", order.getOrderNumber());

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        return mapToOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getUserOrders(String email, Pageable pageable) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return orderRepository.findByCustomerId(customer.getId(), pageable)
                .map(this::mapToOrderResponse);
    }

    @Override
    public Page<OrderResponse> getGuestOrders(String guestEmail, Pageable pageable) {
        // This would require a custom query to find orders by guest email
        // For now, return empty page or implement custom repository method
        return Page.empty(pageable);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, String status, String comment) {
        log.info("Updating order status: {} for order ID: {}", status, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        String oldStatus = order.getStatus();
        order.setStatus(status);
        order = orderRepository.save(order);

        createOrderStatusHistory(order, status, comment, "SYSTEM");

        log.info("Order status updated from {} to {}", oldStatus, status);

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse updatePaymentStatus(Long orderId, String paymentStatus) {
        log.info("Updating payment status: {} for order ID: {}", paymentStatus, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setPaymentStatus(paymentStatus);
        order = orderRepository.save(order);

        createOrderStatusHistory(order, order.getStatus(), "Payment status updated to: " + paymentStatus, "SYSTEM");

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, String reason) {
        log.info("Cancelling order ID: {} with reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        if (!canCancelOrder(order)) {
            throw new BadRequestException("Order cannot be cancelled in its current state: " + order.getStatus());
        }

        order.setStatus(AppConstants.ORDER_STATUS_CANCELLED);
        order = orderRepository.save(order);

        // Restore product stock
        restoreProductStock(order.getItems());

        createOrderStatusHistory(order, AppConstants.ORDER_STATUS_CANCELLED, "Order cancelled: " + reason, "SYSTEM");

        return mapToOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToOrderResponse);
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(String status, Pageable pageable) {
        // This would require a custom query method in OrderRepository
        // For now, return all orders or implement custom repository method
        return getAllOrders(pageable);
    }

    @Override
    public OrderResponse updateTrackingNumber(Long orderId, String trackingNumber) {
        log.info("Updating tracking number for order ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setTrackingNumber(trackingNumber);

        if (AppConstants.ORDER_STATUS_PROCESSING.equals(order.getStatus())) {
            order.setStatus(AppConstants.ORDER_STATUS_SHIPPED);
            createOrderStatusHistory(order, AppConstants.ORDER_STATUS_SHIPPED,
                    "Order shipped with tracking: " + trackingNumber, "SYSTEM");
        }

        order = orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    // Private helper methods

    private Order buildOrderFromRequest(OrderRequest request, Customer customer, String guestEmail) {
        Order order = new Order();
        order.setOrderNumber(orderNumberGenerator.generateOrderNumber());
        order.setCustomer(customer);
        order.setGuestEmail(guestEmail);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(AppConstants.PAYMENT_STATUS_PENDING);
        order.setStatus(AppConstants.ORDER_STATUS_PENDING);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingPostalCode(request.getShippingPostalCode());
        order.setShippingCountry(request.getShippingCountry());
        order.setNotes(request.getNotes());

        return order;
    }

    private List<OrderItem> createOrderItemsFromCart(Order order, Cart cart) {
        return cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setProductName(cartItem.getProduct().getName());
                    orderItem.setProductSku(cartItem.getProduct().getSku());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPricePerUnit(cartItem.getProduct().getPrice());
                    orderItem.setSubtotal(cartItem.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                    return orderItem;
                })
                .collect(Collectors.toList());
    }

    private void calculateOrderTotals(Order order, List<OrderItem> items) {
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);

        // Calculate tax (example: 10% tax rate)
        BigDecimal taxRate = new BigDecimal("0.10");
        order.setTax(subtotal.multiply(taxRate));

        // Calculate total
        order.setTotalAmount(subtotal.add(order.getTax()).add(order.getShippingCost()));
    }

    private void validateStockForCartItems(java.util.Set<CartItem> items) {
        for (CartItem item : items) {
            if (!productService.checkStock(item.getProduct().getId(), item.getQuantity())) {
                throw new BadRequestException("Insufficient stock for product: " + item.getProduct().getName());
            }
        }
    }

    private void updateProductStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                productService.updateStock(item.getProduct().getId(), item.getQuantity());
            }
        }
    }

    private void restoreProductStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private void createOrderStatusHistory(Order order, String status, String comment) {
        createOrderStatusHistory(order, status, comment, null);
    }

    private void createOrderStatusHistory(Order order, String status, String comment, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setComment(comment);
        history.setChangedBy(changedBy);
        history.setCreatedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);
    }

    private boolean canCancelOrder(Order order) {
        return AppConstants.ORDER_STATUS_PENDING.equals(order.getStatus()) ||
                AppConstants.ORDER_STATUS_PROCESSING.equals(order.getStatus());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerId(order.getCustomer() != null ? order.getCustomer().getId() : null);
        response.setCustomerName(order.getCustomer() != null ?
                order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() : null);
        response.setGuestEmail(order.getGuestEmail());
        response.setOrderDate(order.getOrderDate());
        response.setStatus(order.getStatus());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setSubtotal(order.getSubtotal());
        response.setTax(order.getTax());
        response.setShippingCost(order.getShippingCost());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setShippingCity(order.getShippingCity());
        response.setShippingPostalCode(order.getShippingPostalCode());
        response.setShippingCountry(order.getShippingCountry());
        response.setTrackingNumber(order.getTrackingNumber());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());

        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    private OrderResponse.OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        response.setProductName(item.getProductName());
        response.setProductSku(item.getProductSku());
        response.setQuantity(item.getQuantity());
        response.setPricePerUnit(item.getPricePerUnit());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}