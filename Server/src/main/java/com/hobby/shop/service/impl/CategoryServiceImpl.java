package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.CategoryRequest;
import com.hobby.shop.dto.response.CategoryResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.CategoryMapper;
import com.hobby.shop.model.Category;
import com.hobby.shop.repository.CategoryRepository;
import com.hobby.shop.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the CategoryService interface.
 * Manages product categories including CRUD operations and category activation/deactivation.
 *
 * Categories help organize products and improve navigation. This service provides
 * both admin functionality for managing categories and public access to active categories.
 *
 * @author Hobby Shop Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Creates a new product category.
     *
     * Process flow:
     * 1. Validates that the category name is unique
     * 2. Maps the request to a Category entity
     * 3. Saves the category to the database
     *
     * @param request the category creation request containing name and description
     * @return CategoryResponse of the created category
     * @throws BadRequestException if a category with the same name already exists
     */
    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category with name: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name " + request.getName() + " already exists");
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Updates an existing category.
     *
     * @param id the ID of the category to update
     * @param request the update request containing new category data
     * @return CategoryResponse with updated information
     * @throws ResourceNotFoundException if category not found
     * @throws BadRequestException if trying to rename to an already existing name
     */
    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check if name is being changed and if it's already taken
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name " + request.getName() + " already exists");
        }

        categoryMapper.updateEntity(category, request);
        Category updatedCategory = categoryRepository.save(category);

        log.info("Category updated successfully with ID: {}", updatedCategory.getId());
        return categoryMapper.toResponse(updatedCategory);
    }

    /**
     * Soft-deletes a category by marking it as inactive.
     * The category record remains in the database for historical purposes
     * but is no longer available for new product assignments.
     *
     * @param id the ID of the category to delete
     * @throws ResourceNotFoundException if category not found
     */
    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Soft delete - just mark as inactive
        category.setIsActive(false);
        categoryRepository.save(category);

        log.info("Category deactivated successfully with ID: {}", id);
    }

    /**
     * Retrieves a category by its ID.
     *
     * @param id the category ID to search for
     * @return CategoryResponse containing category details
     * @throws ResourceNotFoundException if category not found
     */
    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return categoryMapper.toResponse(category);
    }

    /**
     * Retrieves a category by its name.
     *
     * @param name the category name to search for
     * @return CategoryResponse containing category details
     * @throws ResourceNotFoundException if category not found
     */
    @Override
    public CategoryResponse getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));

        return categoryMapper.toResponse(category);
    }

    /**
     * Retrieves a paginated list of all categories (including inactive ones).
     * Primarily used for admin purposes.
     *
     * @param pageable pagination information
     * @return Page of CategoryResponse objects
     */
    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    /**
     * Retrieves a list of all active categories.
     * Used for public-facing displays like navigation menus.
     *
     * @return List of active CategoryResponse objects
     */
    @Override
    public List<CategoryResponse> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue(Pageable.unpaged())
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a paginated list of active categories.
     *
     * @param pageable pagination information
     * @return Page of active CategoryResponse objects
     */
    @Override
    public Page<CategoryResponse> getActiveCategories(Pageable pageable) {
        return categoryRepository.findByIsActiveTrue(pageable)
                .map(categoryMapper::toResponse);
    }
}