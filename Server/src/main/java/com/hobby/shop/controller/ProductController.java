package com.hobby.shop.controller;

import com.hobby.shop.dto.request.ProductRequest;
import com.hobby.shop.dto.response.ProductResponse;
import com.hobby.shop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // Public endpoints
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    @GetMapping("/featured")
    public ResponseEntity<Page<ProductResponse>> getFeaturedProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getFeaturedProducts(pageable));
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByBrand(
            @PathVariable Long brandId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByBrand(brandId, pageable));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(q, pageable));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponse>> filterProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.filterProducts(
                categoryId, brandId, minPrice, maxPrice, searchTerm, pageable));
    }

    // Admin endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}