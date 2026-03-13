package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.ProductRequest;
import com.hobby.shop.dto.response.ProductResponse;
import com.hobby.shop.exception.BadRequestException;
import com.hobby.shop.exception.ResourceNotFoundException;
import com.hobby.shop.mapper.ProductMapper;
import com.hobby.shop.model.Brand;
import com.hobby.shop.model.Category;
import com.hobby.shop.model.Product;
import com.hobby.shop.repository.BrandRepository;
import com.hobby.shop.repository.CategoryRepository;
import com.hobby.shop.repository.ProductRepository;
import com.hobby.shop.repository.ProductReviewRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest extends BaseServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private ProductReviewRepository reviewRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private Product featuredProduct;
    private Product inactiveProduct;
    private ProductResponse productResponse;
    private ProductRequest productRequest;
    private Category category;
    private Brand brand;

    private final Long productId = 1L;
    private final String sku = "TEST-SKU-123";
    private final String productName = "Test Product";

    @BeforeEach
    void setUp() {
        category = createTestCategory(1L, "Test Category");
        brand = createTestBrand(1L, "Test Brand");

        product = createTestProduct(productId, productName, sku, 10);
        product.setCategory(category);
        product.setBrand(brand);

        featuredProduct = createTestFeaturedProduct(2L, "Featured Product", "SKU-FEAT-123", 5);
        featuredProduct.setCategory(category);
        featuredProduct.setBrand(brand);

        inactiveProduct = createTestInactiveProduct(3L, "Inactive Product", "SKU-INACT-123", 0);
        inactiveProduct.setCategory(category);
        inactiveProduct.setBrand(brand);

        productResponse = new ProductResponse();
        productResponse.setId(productId);
        productResponse.setName(productName);
        productResponse.setSku(sku);
        productResponse.setDescription("Test description");
        productResponse.setPrice(new BigDecimal("99.99"));
        productResponse.setStockQuantity(10);
        productResponse.setIsActive(true);
        productResponse.setIsFeatured(false);
        productResponse.setCategoryId(category.getId());
        productResponse.setCategoryName(category.getName());
        productResponse.setBrandId(brand.getId());
        productResponse.setBrandName(brand.getName());
        productResponse.setAverageRating(4.5);

        productRequest = createTestProductRequest();
    }

    // ==================== CREATE PRODUCT TESTS ====================

    @Test
    void createProduct_Success() {
        // Arrange
        when(productRepository.existsBySku(sku)).thenReturn(false);
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.createProduct(productRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getSku()).isEqualTo(sku);
        assertThat(result.getCategoryId()).isEqualTo(category.getId());
        assertThat(result.getBrandId()).isEqualTo(brand.getId());

        verify(productRepository).existsBySku(sku);
        verify(productMapper).toEntity(productRequest);
        verify(categoryRepository).findById(1L);
        verify(brandRepository).findById(1L);
        verify(productRepository).save(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void createProduct_SkuAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        when(productRepository.existsBySku(sku)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product with SKU " + sku + " already exists");

        verify(productRepository).existsBySku(sku);
        verify(productMapper, never()).toEntity(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(productRepository.existsBySku(sku)).thenReturn(false);
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found with id: 1");

        verify(productRepository).existsBySku(sku);
        verify(categoryRepository).findById(1L);
        verify(brandRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_BrandNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(productRepository.existsBySku(sku)).thenReturn(false);
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Brand not found with id: 1");

        verify(productRepository).existsBySku(sku);
        verify(categoryRepository).findById(1L);
        verify(brandRepository).findById(1L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithoutCategoryAndBrand_Success() {
        // Arrange
        ProductRequest requestWithoutIds = createTestProductRequest();
        requestWithoutIds.setCategoryId(null);
        requestWithoutIds.setBrandId(null);

        Product productWithoutAssociations = createTestProduct(productId, productName, sku, 10);
        productWithoutAssociations.setCategory(null);
        productWithoutAssociations.setBrand(null);

        when(productRepository.existsBySku(sku)).thenReturn(false);
        when(productMapper.toEntity(requestWithoutIds)).thenReturn(productWithoutAssociations);
        when(productRepository.save(any(Product.class))).thenReturn(productWithoutAssociations);

        ProductResponse responseWithoutAssociations = new ProductResponse();
        responseWithoutAssociations.setId(productId);
        responseWithoutAssociations.setName(productName);
        when(productMapper.toResponse(productWithoutAssociations)).thenReturn(responseWithoutAssociations);

        // Act
        ProductResponse result = productService.createProduct(requestWithoutIds);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository, never()).findById(any());
        verify(brandRepository, never()).findById(any());
    }

    // ==================== UPDATE PRODUCT TESTS ====================

    @Test
    void updateProduct_AllFields_Success() {
        // Arrange
        ProductRequest updateRequest = createTestProductRequest();
        updateRequest.setName("Updated Product");
        updateRequest.setSku("UPDATED-SKU-456");

        Product updatedProduct = createTestProduct(productId, "Updated Product", "UPDATED-SKU-456", 15);
        updatedProduct.setCategory(category);
        updatedProduct.setBrand(brand);

        ProductResponse updatedResponse = new ProductResponse();
        updatedResponse.setId(productId);
        updatedResponse.setName("Updated Product");
        updatedResponse.setSku("UPDATED-SKU-456");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySku("UPDATED-SKU-456")).thenReturn(false);
        doNothing().when(productMapper).updateEntity(product, updateRequest);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.toResponse(updatedProduct)).thenReturn(updatedResponse);

        // Act
        ProductResponse result = productService.updateProduct(productId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getSku()).isEqualTo("UPDATED-SKU-456");

        verify(productRepository).findById(productId);
        verify(productRepository).existsBySku("UPDATED-SKU-456");
        verify(productMapper).updateEntity(product, updateRequest);
        verify(categoryRepository).findById(1L);
        verify(brandRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(invalidId, productRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + invalidId);

        verify(productRepository).findById(invalidId);
        verify(productRepository, never()).existsBySku(anyString());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_SkuAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        ProductRequest updateRequest = createTestProductRequest();
        updateRequest.setSku("EXISTING-SKU");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct(productId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product with SKU EXISTING-SKU already exists");

        verify(productRepository).findById(productId);
        verify(productRepository).existsBySku("EXISTING-SKU");
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_SameSku_DoesNotCheckUniqueness() {
        // Arrange
        ProductRequest updateRequest = createTestProductRequest();
        updateRequest.setSku(sku); // Same SKU

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productMapper).updateEntity(product, updateRequest);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.updateProduct(productId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(productRepository, never()).existsBySku(anyString());
    }

    @Test
    void updateProduct_UpdateCategoryOnly_Success() {
        // Arrange
        Category newCategory = createTestCategory(2L, "New Category");

        ProductRequest updateRequest = createTestProductRequest();
        updateRequest.setCategoryId(2L);
        updateRequest.setBrandId(null); // Don't update brand

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productMapper).updateEntity(product, updateRequest);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));
        // brandRepository.findById should not be called
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.updateProduct(productId, updateRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(2L);
        verify(brandRepository, never()).findById(any());
    }

    // ==================== DELETE PRODUCT TESTS ====================

    @Test
    void deleteProduct_Success() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        assertThat(product.getIsActive()).isFalse();
    }

    @Test
    void deleteProduct_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + invalidId);
    }

    // ==================== RESTORE PRODUCT TESTS ====================

    @Test
    void restoreProduct_Success() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(inactiveProduct));
        when(productRepository.save(any(Product.class))).thenReturn(inactiveProduct);
        when(productMapper.toResponse(inactiveProduct)).thenReturn(productResponse);

        // Act
        ProductResponse result = productService.restoreProduct(productId);

        // Assert
        assertThat(result).isNotNull();
        verify(productRepository).findById(productId);
        verify(productRepository).save(inactiveProduct);
        assertThat(inactiveProduct.getIsActive()).isTrue();
    }

    @Test
    void restoreProduct_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.restoreProduct(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + invalidId);
    }

    // ==================== GET PRODUCT BY ID TESTS ====================

    @Test
    void getProductById_Success() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(productId)).thenReturn(Optional.of(4.5));

        // Act
        ProductResponse result = productService.getProductById(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        assertThat(result.getAverageRating()).isEqualTo(4.5);

        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(product);
        verify(reviewRepository).findAverageRatingByProductId(productId);
    }

    @Test
    void getProductById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductById(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + invalidId);
    }

    @Test
    void getProductById_NoRatings_ReturnsZeroRating() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(productId)).thenReturn(Optional.empty());

        // Act
        ProductResponse result = productService.getProductById(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAverageRating()).isEqualTo(0.0);
    }

    // ==================== GET PRODUCT BY SKU TESTS ====================

    @Test
    void getProductBySku_Success() {
        // Arrange
        when(productRepository.findBySku(sku)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(productId)).thenReturn(Optional.of(4.5));

        // Act
        ProductResponse result = productService.getProductBySku(sku);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo(sku);
        verify(productRepository).findBySku(sku);
    }

    @Test
    void getProductBySku_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        String invalidSku = "INVALID-SKU";
        when(productRepository.findBySku(invalidSku)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductBySku(invalidSku))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with SKU: " + invalidSku);
    }

    // ==================== GET ALL PRODUCTS TESTS ====================

    @Test
    void getAllProducts_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> activeProducts = Arrays.asList(product, featuredProduct);
        Page<Product> productPage = new PageImpl<>(activeProducts, pageable, activeProducts.size());

        when(productRepository.findByIsActiveTrue(pageable)).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productRepository).findByIsActiveTrue(pageable);
        verify(productMapper, times(2)).toResponse(any(Product.class));
        verify(reviewRepository, times(2)).findAverageRatingByProductId(anyLong());
    }

    @Test
    void getAllProducts_EmptyPage_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = Page.empty(pageable);

        when(productRepository.findByIsActiveTrue(pageable)).thenReturn(emptyPage);

        // Act
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(productMapper, never()).toResponse(any());
    }

    @Test
    void getAllProductsIncludingInactive_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> allProducts = Arrays.asList(product, featuredProduct, inactiveProduct);
        Page<Product> productPage = new PageImpl<>(allProducts, pageable, allProducts.size());

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.getAllProductsIncludingInactive(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        verify(productRepository).findAll(pageable);
    }

    // ==================== GET PRODUCTS BY CATEGORY TESTS ====================

    @Test
    void getProductsByCategory_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> categoryProducts = Arrays.asList(product, featuredProduct);
        Page<Product> productPage = new PageImpl<>(categoryProducts, pageable, categoryProducts.size());

        when(productRepository.findByCategoryIdAndIsActiveTrue(category.getId(), pageable)).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.getProductsByCategory(category.getId(), pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productRepository).findByCategoryIdAndIsActiveTrue(category.getId(), pageable);
    }

    @Test
    void getProductsByCategory_NoProducts_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = Page.empty(pageable);

        when(productRepository.findByCategoryIdAndIsActiveTrue(999L, pageable)).thenReturn(emptyPage);

        // Act
        Page<ProductResponse> result = productService.getProductsByCategory(999L, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    // ==================== GET PRODUCTS BY BRAND TESTS ====================

    @Test
    void getProductsByBrand_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> brandProducts = Arrays.asList(product, featuredProduct);
        Page<Product> productPage = new PageImpl<>(brandProducts, pageable, brandProducts.size());

        when(productRepository.findByBrandIdAndIsActiveTrue(brand.getId(), pageable)).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.getProductsByBrand(brand.getId(), pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productRepository).findByBrandIdAndIsActiveTrue(brand.getId(), pageable);
    }

    // ==================== GET FEATURED PRODUCTS TESTS ====================

    @Test
    void getFeaturedProducts_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> featuredProducts = Arrays.asList(featuredProduct);
        Page<Product> productPage = new PageImpl<>(featuredProducts, pageable, featuredProducts.size());

        when(productRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable)).thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.getFeaturedProducts(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByIsFeaturedTrueAndIsActiveTrue(pageable);
    }

    // ==================== SEARCH PRODUCTS TESTS ====================

    @Test
    void searchProducts_Success() {
        // Arrange
        String searchTerm = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> searchResults = Arrays.asList(product, featuredProduct);
        Page<Product> productPage = new PageImpl<>(searchResults, pageable, searchResults.size());

        when(productRepository.findProductsByFilters(isNull(), isNull(), isNull(), isNull(), eq(searchTerm), eq(pageable)))
                .thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.searchProducts(searchTerm, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productRepository).findProductsByFilters(isNull(), isNull(), isNull(), isNull(), eq(searchTerm), eq(pageable));
    }

    @Test
    void searchProducts_NoResults_ReturnsEmptyPage() {
        // Arrange
        String searchTerm = "Nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = Page.empty(pageable);

        when(productRepository.findProductsByFilters(isNull(), isNull(), isNull(), isNull(), eq(searchTerm), eq(pageable)))
                .thenReturn(emptyPage);

        // Act
        Page<ProductResponse> result = productService.searchProducts(searchTerm, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    // ==================== FILTER PRODUCTS TESTS ====================

    @Test
    void filterProducts_AllFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        BigDecimal minPrice = new BigDecimal("50");
        BigDecimal maxPrice = new BigDecimal("150");
        String searchTerm = "Test";

        List<Product> filteredProducts = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(filteredProducts, pageable, filteredProducts.size());

        when(productRepository.findProductsByFilters(category.getId(), brand.getId(), minPrice, maxPrice, searchTerm, pageable))
                .thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.filterProducts(
                category.getId(), brand.getId(), minPrice, maxPrice, searchTerm, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findProductsByFilters(
                category.getId(), brand.getId(), minPrice, maxPrice, searchTerm, pageable);
    }

    @Test
    void filterProducts_NoFilters_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> allProducts = Arrays.asList(product, featuredProduct);
        Page<Product> productPage = new PageImpl<>(allProducts, pageable, allProducts.size());

        when(productRepository.findProductsByFilters(isNull(), isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                .thenReturn(productPage);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);
        when(reviewRepository.findAverageRatingByProductId(anyLong())).thenReturn(Optional.of(4.5));

        // Act
        Page<ProductResponse> result = productService.filterProducts(null, null, null, null, null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
    }

    // ==================== STOCK MANAGEMENT TESTS ====================

    @Test
    void checkStock_SufficientStock_ReturnsTrue() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        boolean result = productService.checkStock(productId, 5);

        // Assert
        assertThat(result).isTrue();
        verify(productRepository).findById(productId);
    }

    @Test
    void checkStock_InsufficientStock_ReturnsFalse() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act
        boolean result = productService.checkStock(productId, 15);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void checkStock_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.checkStock(invalidId, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + invalidId);
    }

    @Test
    void updateStock_Success() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act
        productService.updateStock(productId, 3);

        // Assert
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        assertThat(product.getStockQuantity()).isEqualTo(7); // 10 - 3
    }

    @Test
    void updateStock_InsufficientStock_ThrowsBadRequestException() {
        // Arrange
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // Act & Assert
        assertThatThrownBy(() -> productService.updateStock(productId, 15))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient stock for product: " + product.getName());

        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStock_ProductNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateStock(invalidId, 5))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: " + invalidId);
    }

    // ==================== COUNT METHODS TESTS ====================

    @Test
    void getActiveProductsCount_Success() {
        // Arrange
        when(productRepository.countByIsActiveTrue()).thenReturn(5L);

        // Act
        long result = productService.getActiveProductsCount();

        // Assert
        assertThat(result).isEqualTo(5L);
        verify(productRepository).countByIsActiveTrue();
    }

    @Test
    void getInactiveProductsCount_Success() {
        // Arrange
        when(productRepository.countByIsActiveFalse()).thenReturn(2L);

        // Act
        long result = productService.getInactiveProductsCount();

        // Assert
        assertThat(result).isEqualTo(2L);
        verify(productRepository).countByIsActiveFalse();
    }

    @Test
    void getFeaturedProductsCount_Success() {
        // Arrange
        when(productRepository.countByIsFeaturedTrueAndIsActiveTrue()).thenReturn(3L);

        // Act
        long result = productService.getFeaturedProductsCount();

        // Assert
        assertThat(result).isEqualTo(3L);
        verify(productRepository).countByIsFeaturedTrueAndIsActiveTrue();
    }
}