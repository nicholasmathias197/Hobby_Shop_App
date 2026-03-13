package com.hobby.shop.service;

import com.hobby.shop.dto.request.CategoryRequest;
import com.hobby.shop.dto.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse getCategoryByName(String name);

    Page<CategoryResponse> getAllCategories(Pageable pageable);

    List<CategoryResponse> getAllActiveCategories();

    Page<CategoryResponse> getActiveCategories(Pageable pageable);
}