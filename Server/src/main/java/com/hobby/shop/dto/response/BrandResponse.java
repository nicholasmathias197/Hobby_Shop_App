package com.hobby.shop.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrandResponse {
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String website;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Long productCount;
}