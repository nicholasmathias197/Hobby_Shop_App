package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.OrderRequest;
import com.hobby.shop.dto.response.OrderResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.exception.UnauthorizedException;
import com.hobby.shop.model.*;
import com.hobby.shop.repository.*;
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
import java.util.ArrayList;
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
    private final ProductService productService;
    private final OrderNumberGenerator orderNumberGenerator;

    @Override
    public OrderResponse createOrder(String email, OrderRequest request) {
        log.info("Creating order for authenticated user: {}", email);

        // Verify user exists
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Validate request has items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Order must contain at least one item");
        }

        // Validate stock for all items
        validateStockForItems(request.getItems());

        // Create order
        Order order = buildOrderFromRequest(request, customer, null);
        order = orderRepository.save(order);

        // Create order items from request
        List<OrderItem> orderItems = createOrderItemsFromRequest(order, request.getItems());
        order.setItems(orderItems);

        // Calculate totals
        calculateOrderTotals(order, orderItems);

        // Save order items
        orderItemRepository.saveAll(orderItems);

        // Update product stock
        updateProductStock(orderItems);

        // Create order status history
        createOrderStatusHistory(order, AppConstants.ORDER_STATUS_PENDING, "Order placed successfully");

        log.info("Order created successfully with number: {} for user: {}", order.getOrderNumber(), email);

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByNumber(String email, String orderNumber) {
        log.info("Fetching order {} for user: {}", orderNumber, email);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        // Security check: ensure order belongs to the user
        if (order.getCustomer() == null || !order.getCustomer().getEmail().equals(email)) {
            log.warn("User {} attempted to access order {} belonging to {}",
                    email, orderNumber,
                    order.getCustomer() != null ? order.getCustomer().getEmail() : "guest");
            throw new UnauthorizedException("You don't have permission to view this order");
        }

        return mapToOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getUserOrders(String email, Pageable pageable) {
        log.info("Fetching all orders for user: {}", email);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return orderRepository.findByCustomerId(customer.getId(), pageable)
                .map(this::mapToOrderResponse);
    }

    @Override
    public OrderResponse cancelOrder(String email, Long orderId, String reason) {
        log.info("User {} cancelling order ID: {}", email, orderId);

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        // Security check: ensure order belongs to the user
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customer.getId())) {
            log.warn("User {} attempted to cancel order {} belonging to another user", email, orderId);
            throw new UnauthorizedException("You don't have permission to cancel this order");
        }

        // Check if order can be cancelled
        if (!canCancelOrder(order)) {
            throw new BadRequestException("Order cannot be cancelled in its current state: " + order.getStatus());
        }

        order.setStatus(AppConstants.ORDER_STATUS_CANCELLED);
        order = orderRepository.save(order);

        // Restore product stock
        restoreProductStock(order.getItems());

        createOrderStatusHistory(order, AppConstants.ORDER_STATUS_CANCELLED,
                "Order cancelled by user: " + (reason != null ? reason : "No reason provided"), email);

        log.info("Order {} cancelled successfully by user: {}", orderId, email);

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse getGuestOrder(String orderNumber, String guestEmail) {
        log.info("Guest looking up order: {} for email: {}", orderNumber, guestEmail);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));

        // Verify this is a guest order and email matches
        if (order.getCustomer() != null) {
            log.warn("Attempted guest lookup of user order: {}", orderNumber);
            throw new UnauthorizedException("This is a registered user order. Please login to view.");
        }

        if (!guestEmail.equals(order.getGuestEmail())) {
            log.warn("Guest email {} does not match order email {}", guestEmail, order.getGuestEmail());
            throw new UnauthorizedException("Email does not match this order");
        }

        return mapToOrderResponse(order);
    }

    // ============= ADMIN METHODS =============

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        log.info("Admin fetching all orders");
        return orderRepository.findAll(pageable)
                .map(this::mapToOrderResponse);
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(String status, Pageable pageable) {
        log.info("Admin fetching orders with status: {}", status);
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToOrderResponse);
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, String status, String comment) {
        log.info("Admin updating order {} status to: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        String oldStatus = order.getStatus();
        order.setStatus(status);
        order = orderRepository.save(order);

        createOrderStatusHistory(order, status, comment, "ADMIN");

        log.info("Order status updated from {} to {} by admin", oldStatus, status);

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse updatePaymentStatus(Long orderId, String paymentStatus) {
        log.info("Admin updating payment status for order {} to: {}", orderId, paymentStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setPaymentStatus(paymentStatus);
        order = orderRepository.save(order);

        createOrderStatusHistory(order, order.getStatus(),
                "Payment status updated to: " + paymentStatus + " by admin", "ADMIN");

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse updateTrackingNumber(Long orderId, String trackingNumber) {
        log.info("Admin updating tracking number for order {} to: {}", orderId, trackingNumber);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setTrackingNumber(trackingNumber);

        // If order is processing, automatically mark as shipped
        if (AppConstants.ORDER_STATUS_PROCESSING.equals(order.getStatus())) {
            order.setStatus(AppConstants.ORDER_STATUS_SHIPPED);
            createOrderStatusHistory(order, AppConstants.ORDER_STATUS_SHIPPED,
                    "Order shipped with tracking: " + trackingNumber + " by admin", "ADMIN");
        }

        order = orderRepository.save(order);

        return mapToOrderResponse(order);
    }

    // ============= PRIVATE HELPER METHODS =============

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
        order.setShippingCost(BigDecimal.ZERO); // Default shipping cost, can be calculated later

        return order;
    }

    private List<OrderItem> createOrderItemsFromRequest(Order order, List<OrderRequest.OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderRequest.OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductSku(product.getSku());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPricePerUnit(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            orderItems.add(orderItem);
        }

        return orderItems;
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
        order.setTotalAmount(subtotal
                .add(order.getTax())
                .add(order.getShippingCost()));
    }

    private void validateStockForItems(List<OrderRequest.OrderItemRequest> items) {
        for (OrderRequest.OrderItemRequest item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));

            if (product.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException(
                        String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                                product.getName(), product.getStockQuantity(), item.getQuantity())
                );
            }
        }
    }

    private void updateProductStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
                log.debug("Updated stock for product {}: new quantity {}", product.getId(), product.getStockQuantity());
            }
        }
    }

    private void restoreProductStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
                log.debug("Restored stock for product {}: new quantity {}", product.getId(), product.getStockQuantity());
            }
        }
    }

    private void createOrderStatusHistory(Order order, String status, String comment) {
        createOrderStatusHistory(order, status, comment, "SYSTEM");
    }

    private void createOrderStatusHistory(Order order, String status, String comment, String changedBy) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        history.setComment(comment);
        history.setChangedBy(changedBy);
        history.setCreatedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);
        log.debug("Created status history for order {}: {} - {}", order.getOrderNumber(), status, comment);
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