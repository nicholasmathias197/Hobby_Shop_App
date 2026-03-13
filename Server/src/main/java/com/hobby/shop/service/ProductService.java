package com.hobby.shop.service;

import com.hobby.shop.dto.request.ProductRequest;
import com.hobby.shop.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);

    Page<ProductResponse> getAllProducts(Pageable pageable);

    Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<ProductResponse> getProductsByBrand(Long brandId, Pageable pageable);

    Page<ProductResponse> getFeaturedProducts(Pageable pageable);

    Page<ProductResponse> searchProducts(String searchTerm, Pageable pageable);

    Page<ProductResponse> filterProducts(Long categoryId, Long brandId,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         String searchTerm, Pageable pageable);

    boolean checkStock(Long productId, Integer quantity);

    void updateStock(Long productId, Integer quantity);

    Page<ProductResponse> getAllProductsIncludingInactive(Pageable pageable);

    ProductResponse restoreProduct(Long id);

    long getActiveProductsCount();

    long getInactiveProductsCount();

    long getFeaturedProductsCount();
}