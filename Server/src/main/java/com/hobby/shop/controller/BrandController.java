package com.hobby.shop.controller;

import com.hobby.shop.dto.request.BrandRequest;
import com.hobby.shop.dto.response.BrandResponse;
import com.hobby.shop.service.BrandService;
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

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    // ============= PUBLIC ENDPOINTS =============

    /**
     * Get paginated list of all brands
     * GET /api/brands
     * @param pageable Pagination information (default: size=20, sort by name ASC)
     * @return Paginated list of brand responses
     */
    @GetMapping
    public ResponseEntity<Page<BrandResponse>> getAllBrands(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(brandService.getAllBrands(pageable));
    }

    /**
     * Get paginated list of only active brands
     * GET /api/brands/active
     * @param pageable Pagination information (default: size=20)
     * @return Paginated list of active brand responses
     */
    @GetMapping("/active")
    public ResponseEntity<Page<BrandResponse>> getActiveBrands(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(brandService.getActiveBrands(pageable));
    }

    /**
     * Get all active brands as a list (no pagination)
     * GET /api/brands/all
     * @return List of all active brand responses
     */
    @GetMapping("/all")
    public ResponseEntity<List<BrandResponse>> getAllActiveBrands() {
        return ResponseEntity.ok(brandService.getAllActiveBrands());
    }

    /**
     * Get brand by ID
     * GET /api/brands/{id}
     * @param id Brand ID
     * @return Brand response
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    /**
     * Get brand by exact name match
     * GET /api/brands/name/{name}
     * @param name Brand name
     * @return Brand response
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<BrandResponse> getBrandByName(@PathVariable String name) {
        return ResponseEntity.ok(brandService.getBrandByName(name));
    }

    // ============= ADMIN ENDPOINTS =============

    /**
     * Create a new brand (admin only)
     * POST /api/brands
     * @param request Brand creation request
     * @return Created brand response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        return new ResponseEntity<>(brandService.createBrand(request), HttpStatus.CREATED);
    }

    /**
     * Update an existing brand (admin only)
     * PUT /api/brands/{id}
     * @param id Brand ID
     * @param request Brand update request
     * @return Updated brand response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    /**
     * Delete a brand (soft delete) (admin only)
     * DELETE /api/brands/{id}
     * @param id Brand ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}