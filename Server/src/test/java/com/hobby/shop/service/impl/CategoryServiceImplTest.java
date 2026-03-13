package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.CategoryRequest;
import com.hobby.shop.dto.response.CategoryResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.CategoryMapper;
import com.hobby.shop.model.Category;
import com.hobby.shop.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest categoryRequest;
    private Category category;
    private CategoryResponse categoryResponse;
    private final Long categoryId = 1L;
    private final String categoryName = "Test Category";

    @BeforeEach
    void setUp() {
        categoryRequest = new CategoryRequest();
        categoryRequest.setName(categoryName);
        categoryRequest.setDescription("Test Description");

        category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        category.setDescription("Test Description");
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);
        categoryResponse.setName(categoryName);
        categoryResponse.setDescription("Test Description");
        categoryResponse.setIsActive(true);
    }

    @Test
    void createCategory_Success() {
        // Arrange
        when(categoryRepository.existsByName(categoryName)).thenReturn(false);
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.createCategory(categoryRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo(categoryName);
        assertThat(result.getIsActive()).isTrue();

        verify(categoryRepository).existsByName(categoryName);
        verify(categoryMapper).toEntity(categoryRequest);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void createCategory_NameAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        when(categoryRepository.existsByName(categoryName)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(categoryRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Category with name " + categoryName + " already exists");

        verify(categoryRepository).existsByName(categoryName);
        verify(categoryMapper, never()).toEntity(any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_Success() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Updated Category");
        updateRequest.setDescription("Updated Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Updated Category")).thenReturn(false);
        doNothing().when(categoryMapper).updateEntity(category, updateRequest);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse updatedResponse = new CategoryResponse();
        updatedResponse.setId(categoryId);
        updatedResponse.setName("Updated Category");
        updatedResponse.setDescription("Updated Description");
        when(categoryMapper.toResponse(category)).thenReturn(updatedResponse);

        // Act
        CategoryResponse result = categoryService.updateCategory(categoryId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Category");

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByName("Updated Category");
        verify(categoryMapper).updateEntity(category, updateRequest);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void updateCategory_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(999L, categoryRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: 999");

        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).existsByName(anyString());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_NameAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Existing Category");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByName("Existing Category")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Category with name Existing Category already exists");
    }

    @Test
    void updateCategory_SameName_DoesNotCheckUniqueness() {
        // Arrange
        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName(categoryName); // Same name
        updateRequest.setDescription("Updated Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryMapper).updateEntity(category, updateRequest);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.updateCategory(categoryId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository, never()).existsByName(anyString());
    }

    @Test
    void deleteCategory_Success() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(category);
        assertThat(category.getIsActive()).isFalse();
    }

    @Test
    void deleteCategory_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: 999");
    }

    @Test
    void getCategoryById_Success() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.getCategoryById(categoryId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
        verify(categoryRepository).findById(categoryId);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getCategoryById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: 999");
    }

    @Test
    void getCategoryByName_Success() {
        // Arrange
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.getCategoryByName(categoryName);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(categoryName);
        verify(categoryRepository).findByName(categoryName);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getCategoryByName_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findByName("Nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryByName("Nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with name: Nonexistent");
    }

    @Test
    void getAllCategories_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categories = Arrays.asList(category, createAnotherCategory());
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());

        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(categoryRepository).findAll(pageable);
        verify(categoryMapper, times(2)).toResponse(any(Category.class));
    }

    @Test
    void getAllActiveCategories_Unpaged_Success() {
        // Arrange
        List<Category> activeCategories = Arrays.asList(category, createAnotherCategory());
        Page<Category> categoryPage = new PageImpl<>(activeCategories);

        when(categoryRepository.findByIsActiveTrue(Pageable.unpaged())).thenReturn(categoryPage);
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(categoryResponse);

        // Act
        List<CategoryResponse> result = categoryService.getAllActiveCategories();

        // Assert
        assertThat(result).hasSize(2);
        verify(categoryRepository).findByIsActiveTrue(Pageable.unpaged());
        verify(categoryMapper, times(2)).toResponse(any(Category.class));
    }

    @Test
    void getActiveCategories_Paginated_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> activeCategories = Arrays.asList(category);
        Page<Category> categoryPage = new PageImpl<>(activeCategories, pageable, 1);

        when(categoryRepository.findByIsActiveTrue(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        Page<CategoryResponse> result = categoryService.getActiveCategories(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(categoryRepository).findByIsActiveTrue(pageable);
        verify(categoryMapper).toResponse(category);
    }

    private Category createAnotherCategory() {
        Category another = new Category();
        another.setId(2L);
        another.setName("Another Category");
        another.setIsActive(true);
        return another;
    }
}