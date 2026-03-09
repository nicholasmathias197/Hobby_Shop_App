package com.hobby.shop.controller;

import com.hobby.shop.dto.request.CategoryRequest;
import com.hobby.shop.dto.response.CategoryResponse;
import com.hobby.shop.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ============= PUBLIC ENDPOINTS =============

    /**
     * Get paginated list of all categories
     * GET /api/categories
     * @param pageable Pagination information (default: size=20, sort by name ASC)
     * @return Paginated list of category responses
     */
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(categoryService.getAllCategories(pageable));
    }

    /**
     * Get paginated list of only active categories
     * GET /api/categories/active
     * @param pageable Pagination information (default: size=20)
     * @return Paginated list of active category responses
     */
    @GetMapping("/active")
    public ResponseEntity<Page<CategoryResponse>> getActiveCategories(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(categoryService.getActiveCategories(pageable));
    }

    /**
     * Get all active categories as a list (no pagination)
     * GET /api/categories/all
     * @return List of all active category responses
     */
    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponse>> getAllActiveCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    /**
     * Get category by ID
     * GET /api/categories/{id}
     * @param id Category ID
     * @return Category response
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Get category by exact name match
     * GET /api/categories/name/{name}
     * @param name Category name
     * @return Category response
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<CategoryResponse> getCategoryByName(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.getCategoryByName(name));
    }

    // ============= ADMIN ENDPOINTS =============

    /**
     * Create a new category (admin only)
     * POST /api/categories
     * @param request Category creation request
     * @return Created category response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return new ResponseEntity<>(categoryService.createCategory(request), HttpStatus.CREATED);
    }

    /**
     * Update an existing category (admin only)
     * PUT /api/categories/{id}
     * @param id Category ID
     * @param request Category update request
     * @return Updated category response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    /**
     * Delete a category (soft delete) (admin only)
     * DELETE /api/categories/{id}
     * @param id Category ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}