package com.hobby.shop.controller;

import com.hobby.shop.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    // ============= HEALTH CHECK ENDPOINT =============

    /**
     * Simple health check endpoint for monitoring
     * GET /api/health
     * Used by load balancers, monitoring tools, and CI/CD pipelines
     * @return ApiResponse with status UP and service information
     */
    @GetMapping
    public ResponseEntity<ApiResponse> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Hobby Shop API is running");

        return ResponseEntity.ok(new ApiResponse(true, "Service is healthy", status));
    }
}