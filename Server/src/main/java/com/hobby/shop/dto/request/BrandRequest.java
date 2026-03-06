package com.hobby.shop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {

    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must be less than 100 characters")
    private String name;

    private String description;

    @Size(max = 255, message = "Logo URL must be less than 255 characters")
    private String logoUrl;

    @Size(max = 255, message = "Website URL must be less than 255 characters")
    private String website;

    private Boolean isActive;
}