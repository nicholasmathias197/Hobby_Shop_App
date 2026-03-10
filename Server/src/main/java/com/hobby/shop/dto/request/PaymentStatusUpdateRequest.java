package com.hobby.shop.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PaymentStatusUpdateRequest {
    @NotBlank(message = "Payment status is required")
    private String paymentStatus;
}
