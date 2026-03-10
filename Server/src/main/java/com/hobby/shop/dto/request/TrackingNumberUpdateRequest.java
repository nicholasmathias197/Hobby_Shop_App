package com.hobby.shop.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class TrackingNumberUpdateRequest {
    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;
}