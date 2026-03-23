package com.hobby.shop.controller;

import com.hobby.shop.dto.response.BrandResponse;
import com.hobby.shop.dto.response.CategoryResponse;
import com.hobby.shop.dto.response.ProductResponse;
import com.hobby.shop.service.BrandService;
import com.hobby.shop.service.CategoryService;
import com.hobby.shop.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogControllerTest {

    @Mock
    private BrandService brandService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductService productService;

    private BrandController brandController;
    private CategoryController categoryController;
    private ProductController productController;

    @BeforeEach
    void setUp() {
        brandController = new BrandController(brandService);
        categoryController = new CategoryController(categoryService);
        productController = new ProductController(productService);
    }

    @Test
    void brandControllerDelegatesAllEndpoints() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<BrandResponse> page = new PageImpl<>(List.of(mock(BrandResponse.class)));
        List<BrandResponse> brands = List.of(mock(BrandResponse.class));
        BrandResponse brand = mock(BrandResponse.class);

        when(brandService.getAllBrands(pageable)).thenReturn(page);
        when(brandService.getActiveBrands(pageable)).thenReturn(page);
        when(brandService.getAllActiveBrands()).thenReturn(brands);
        when(brandService.getBrandById(1L)).thenReturn(brand);
        when(brandService.getBrandByName("Acme")).thenReturn(brand);
        when(brandService.createBrand(null)).thenReturn(brand);
        when(brandService.updateBrand(2L, null)).thenReturn(brand);

        assertThat(brandController.getAllBrands(pageable).getBody()).isSameAs(page);
        assertThat(brandController.getActiveBrands(pageable).getBody()).isSameAs(page);
        assertThat(brandController.getAllActiveBrands().getBody()).isSameAs(brands);
        assertThat(brandController.getBrandById(1L).getBody()).isSameAs(brand);
        assertThat(brandController.getBrandByName("Acme").getBody()).isSameAs(brand);
        assertThat(brandController.createBrand(null).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(brandController.createBrand(null).getBody()).isSameAs(brand);
        assertThat(brandController.updateBrand(2L, null).getBody()).isSameAs(brand);
        assertThat(brandController.deleteBrand(3L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(brandService).deleteBrand(3L);
    }

    @Test
    void categoryControllerDelegatesAllEndpoints() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CategoryResponse> page = new PageImpl<>(List.of(mock(CategoryResponse.class)));
        List<CategoryResponse> categories = List.of(mock(CategoryResponse.class));
        CategoryResponse category = mock(CategoryResponse.class);

        when(categoryService.getAllCategories(pageable)).thenReturn(page);
        when(categoryService.getActiveCategories(pageable)).thenReturn(page);
        when(categoryService.getAllActiveCategories()).thenReturn(categories);
        when(categoryService.getCategoryById(1L)).thenReturn(category);
        when(categoryService.getCategoryByName("Models")).thenReturn(category);
        when(categoryService.createCategory(null)).thenReturn(category);
        when(categoryService.updateCategory(2L, null)).thenReturn(category);

        assertThat(categoryController.getAllCategories(pageable).getBody()).isSameAs(page);
        assertThat(categoryController.getActiveCategories(pageable).getBody()).isSameAs(page);
        assertThat(categoryController.getAllActiveCategories().getBody()).isSameAs(categories);
        assertThat(categoryController.getCategoryById(1L).getBody()).isSameAs(category);
        assertThat(categoryController.getCategoryByName("Models").getBody()).isSameAs(category);
        assertThat(categoryController.createCategory(null).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(categoryController.createCategory(null).getBody()).isSameAs(category);
        assertThat(categoryController.updateCategory(2L, null).getBody()).isSameAs(category);
        assertThat(categoryController.deleteCategory(3L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(categoryService).deleteCategory(3L);
    }

    @Test
    void productControllerDelegatesAllEndpoints() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ProductResponse> page = new PageImpl<>(List.of(mock(ProductResponse.class)));
        ProductResponse product = mock(ProductResponse.class);

        when(productService.getAllProducts(pageable)).thenReturn(page);
        when(productService.getProductById(1L)).thenReturn(product);
        when(productService.getProductBySku("SKU-1")).thenReturn(product);
        when(productService.getFeaturedProducts(pageable)).thenReturn(page);
        when(productService.getProductsByBrand(2L, pageable)).thenReturn(page);
        when(productService.getProductsByCategory(3L, pageable)).thenReturn(page);
        when(productService.searchProducts("paint", pageable)).thenReturn(page);
        when(productService.filterProducts(1L, 2L, BigDecimal.ONE, BigDecimal.TEN, "paint", pageable)).thenReturn(page);
        when(productService.createProduct(null)).thenReturn(product);
        when(productService.updateProduct(4L, null)).thenReturn(product);
        when(productService.getAllProductsIncludingInactive(pageable)).thenReturn(page);
        when(productService.restoreProduct(5L)).thenReturn(product);
        when(productService.getActiveProductsCount()).thenReturn(12L);
        when(productService.getInactiveProductsCount()).thenReturn(2L);
        when(productService.getFeaturedProductsCount()).thenReturn(4L);

        assertThat(productController.getAllProducts(pageable).getBody()).isSameAs(page);
        assertThat(productController.getProductById(1L).getBody()).isSameAs(product);
        assertThat(productController.getProductBySku("SKU-1").getBody()).isSameAs(product);
        assertThat(productController.getFeaturedProducts(pageable).getBody()).isSameAs(page);
        assertThat(productController.getProductsByBrand(2L, pageable).getBody()).isSameAs(page);
        assertThat(productController.getProductsByCategory(3L, pageable).getBody()).isSameAs(page);
        assertThat(productController.searchProducts("paint", pageable).getBody()).isSameAs(page);
        assertThat(productController.filterProducts(1L, 2L, BigDecimal.ONE, BigDecimal.TEN, "paint", pageable).getBody()).isSameAs(page);
        assertThat(productController.createProduct(null).getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(productController.createProduct(null).getBody()).isSameAs(product);
        assertThat(productController.updateProduct(4L, null).getBody()).isSameAs(product);
        assertThat(productController.deleteProduct(4L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(productController.getAllProductsIncludingInactive(pageable).getBody()).isSameAs(page);
        assertThat(productController.restoreProduct(5L).getBody()).isSameAs(product);
        assertThat(productController.getActiveProductsCount().getBody()).isEqualTo(12L);
        assertThat(productController.getInactiveProductsCount().getBody()).isEqualTo(2L);
        assertThat(productController.getFeaturedProductsCount().getBody()).isEqualTo(4L);

        verify(productService).deleteProduct(4L);
    }
}