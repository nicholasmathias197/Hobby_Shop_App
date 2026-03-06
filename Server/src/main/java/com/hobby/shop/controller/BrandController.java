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

    // Public endpoints
    @GetMapping
    public ResponseEntity<Page<BrandResponse>> getAllBrands(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(brandService.getAllBrands(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<BrandResponse>> getActiveBrands(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(brandService.getActiveBrands(pageable));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BrandResponse>> getAllActiveBrands() {
        return ResponseEntity.ok(brandService.getAllActiveBrands());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<BrandResponse> getBrandByName(@PathVariable String name) {
        return ResponseEntity.ok(brandService.getBrandByName(name));
    }

    // Admin endpoints
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        return new ResponseEntity<>(brandService.createBrand(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}