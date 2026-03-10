package com.hobby.shop.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class OrderStatusUpdateRequest {
    @NotBlank(message = "Status is required")
    private String status;

    private String comment;
}