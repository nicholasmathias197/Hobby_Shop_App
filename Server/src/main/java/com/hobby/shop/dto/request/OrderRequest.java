package com.hobby.shop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    // Payment Information
    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "credit_card", "paypal", etc.

    private String cardNumber;
    private String cardHolderName;
    private String cardExpiryMonth;
    private String cardExpiryYear;
    private String cardCvv;

    // Billing Address (can be same as shipping)
    private boolean sameAsShipping = true;
    private String billingAddress;
    private String billingCity;
    private String billingPostalCode;
    private String billingCountry;

    // Shipping Information
    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Shipping city is required")
    private String shippingCity;

    @NotBlank(message = "Postal code is required")
    private String shippingPostalCode;

    @NotBlank(message = "Country is required")
    private String shippingCountry;

    private String notes;

    @Email(message = "Valid email is required for guest checkout")
    private String guestEmail;

    @NotNull(message = "Order items are required")
    @Size(min = 1, message = "At least one item is required")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}