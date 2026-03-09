package com.hobby.shop.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class CartResponse {
    private Long id;
    private Long customerId;
    private String sessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<CartItemResponse> items = new HashSet<>();  // Initialize here!
    private Integer totalItems;
    private BigDecimal totalPrice;

    @Data
    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private String productSku;
        private String productImage;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;
    }
}