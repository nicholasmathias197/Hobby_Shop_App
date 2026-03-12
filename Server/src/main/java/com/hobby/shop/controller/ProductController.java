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

    // ============= PUBLIC ENDPOINTS =============

    /**
     * Get paginated list of all products
     * GET /api/products
     * @param pageable Pagination information (default: size=20, sort by name ASC)
     * @return Paginated list of product responses
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     * @param id Product ID
     * @return Product response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Get product by SKU (Stock Keeping Unit)
     * GET /api/products/sku/{sku}
     * @param sku Product SKU
     * @return Product response
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getProductBySku(sku));
    }

    /**
     * Get paginated list of featured products
     * GET /api/products/featured
     * @param pageable Pagination information (default: size=10)
     * @return Paginated list of featured product responses
     */
    @GetMapping("/featured")
    public ResponseEntity<Page<ProductResponse>> getFeaturedProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(productService.getFeaturedProducts(pageable));
    }

    /**
     * Get paginated list of products by brand
     * GET /api/products/brand/{brandId}
     * @param brandId Brand ID
     * @param pageable Pagination information (default: size=20)
     * @return Paginated list of product responses
     */
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByBrand(
            @PathVariable Long brandId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByBrand(brandId, pageable));
    }

    /**
     * Get paginated list of products by category
     * GET /api/products/category/{categoryId}
     * @param categoryId Category ID
     * @param pageable Pagination information (default: size=20)
     * @return Paginated list of product responses
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    /**
     * Search products by keyword
     * GET /api/products/search?q={searchTerm}
     * @param q Search term
     * @param pageable Pagination information (default: size=20)
     * @return Paginated list of matching product responses
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(q, pageable));
    }

    /**
     * Filter products with multiple criteria
     * GET /api/products/filter
     * @param categoryId Optional category filter
     * @param brandId Optional brand filter
     * @param minPrice Optional minimum price filter
     * @param maxPrice Optional maximum price filter
     * @param searchTerm Optional search term
     * @param pageable Pagination information (default: size=20)
     * @return Paginated list of filtered product responses
     */
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

    // ============= ADMIN ENDPOINTS =============

    /**
     * Create a new product (admin only)
     * POST /api/products
     * @param request Product creation request
     * @return Created product response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return new ResponseEntity<>(productService.createProduct(request), HttpStatus.CREATED);
    }

    /**
     * Update an existing product (admin only)
     * PUT /api/products/{id}
     * @param id Product ID
     * @param request Product update request
     * @return Updated product response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * Delete a product (soft delete) (admin only)
     * DELETE /api/products/{id}
     * @param id Product ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Get all products including inactive (soft-deleted) (admin only)
            * GET /api/products/admin/all
 */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ProductResponse>> getAllProductsIncludingInactive(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProductsIncludingInactive(pageable));
    }

    /**
     * Restore a soft-deleted product (admin only)
     * PUT /api/products/{id}/restore
     */
    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> restoreProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.restoreProduct(id));
    }
    /**
     * Get count of active products (admin only)
     * GET /api/products/count/active
     */
    @GetMapping("/count/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveProductsCount() {
        return ResponseEntity.ok(productService.getActiveProductsCount());
    }

    /**
     * Get count of inactive products (admin only)
     * GET /api/products/count/inactive
     */
    @GetMapping("/count/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getInactiveProductsCount() {
        return ResponseEntity.ok(productService.getInactiveProductsCount());
    }

    /**
     * Get count of featured products (admin only)
     * GET /api/products/count/featured
     */
    @GetMapping("/count/featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> getFeaturedProductsCount() {
        return ResponseEntity.ok(productService.getFeaturedProductsCount());
    }
}