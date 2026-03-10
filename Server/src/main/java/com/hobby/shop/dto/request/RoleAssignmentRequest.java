package com.hobby.shop.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleAssignmentRequest {

    @NotNull(message = "Role ID is required")
    private Long roleId;

    // You can add more fields if needed
    private String action; // "ADD" or "REMOVE" if you want to combine endpoints
}