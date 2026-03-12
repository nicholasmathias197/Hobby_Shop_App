package com.hobby.shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    // Shipping Information
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Shipping city is required")
    private String shippingCity;

    @NotBlank(message = "Postal code is required")
    private String shippingPostalCode;

    @NotBlank(message = "Country is required")
    private String shippingCountry;

    // Payment Information
    private String paymentMethod;

    // For credit card payments (store only last 4 digits in production)
    private String cardLastFour; // You can derive this from cardNumber on backend

    // Billing Information
    private String billingAddress;
    private String billingCity;
    private String billingPostalCode;
    private String billingCountry;

    // Customer Information
    private String customerEmail;
    private String customerPhone;
    private String customerName;

    private String notes;

    // Guest Email (for guest checkout)
    @Email(message = "Valid email is required for guest checkout")
    private String guestEmail;

    // Order Items
    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "At least one item is required")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}