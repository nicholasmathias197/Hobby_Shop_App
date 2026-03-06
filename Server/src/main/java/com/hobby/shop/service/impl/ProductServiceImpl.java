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
import com.hobby.shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductReviewRepository reviewRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(request);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // Check if SKU is being changed and if it's already taken
        if (!product.getSku().equals(request.getSku()) &&
                productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU " + request.getSku() + " already exists");
        }

        productMapper.updateEntity(product, request);

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product deactivated successfully with ID: {}", id);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        ProductResponse response = productMapper.toResponse(product);
        Double avgRating = reviewRepository.getAverageRatingForProduct(id);
        response.setAverageRating(avgRating != null ? avgRating : 0.0);

        return response;
    }

    @Override
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));

        ProductResponse response = productMapper.toResponse(product);
        Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
        response.setAverageRating(avgRating != null ? avgRating : 0.0);

        return response;
    }

    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    @Override
    public Page<ProductResponse> getProductsByBrand(Long brandId, Pageable pageable) {
        return productRepository.findByBrandIdAndIsActiveTrue(brandId, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    @Override
    public Page<ProductResponse> getFeaturedProducts(Pageable pageable) {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    @Override
    public Page<ProductResponse> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.findProductsByFilters(null, null, null, null, searchTerm, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    @Override
    public Page<ProductResponse> filterProducts(Long categoryId, Long brandId,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                String searchTerm, Pageable pageable) {
        return productRepository.findProductsByFilters(categoryId, brandId, minPrice, maxPrice, searchTerm, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    Double avgRating = reviewRepository.getAverageRatingForProduct(product.getId());
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    @Override
    public boolean checkStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return product.getStockQuantity() >= quantity;
    }

    @Override
    public void updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
    }
}