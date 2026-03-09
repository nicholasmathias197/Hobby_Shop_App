package com.hobby.shop.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String guestEmail;
    private LocalDateTime orderDate;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingCountry;
    private String trackingNumber;
    private String notes;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    public void setUpdatedAt(LocalDateTime updatedAt) {

    }

    @Data
    public static class OrderItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal pricePerUnit;
        private BigDecimal subtotal;
    }
}