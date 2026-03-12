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

/**
 * Implementation of the ProductService interface.
 * Manages all product-related operations including CRUD, searching, filtering,
 * stock management, and product reviews integration.
 *
 * This service handles:
 * - Product creation, update, and soft deletion
 * - Product retrieval with average ratings
 * - Product search and filtering by various criteria
 * - Stock validation and updates
 * - Featured products management
 *
 * @author Hobby Shop Team
 * @version 1.0
 */
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

    /**
     * Creates a new product.
     *
     * Process flow:
     * 1. Validates SKU uniqueness
     * 2. Maps request to Product entity
     * 3. Associates category and brand if provided
     * 4. Saves product to database
     *
     * @param request the product creation request
     * @return ProductResponse of the created product
     * @throws BadRequestException if SKU already exists
     * @throws ResourceNotFoundException if category or brand not found
     */
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = productMapper.toEntity(request);

        // Associate category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Associate brand if provided
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toResponse(savedProduct);
    }

    /**
     * Updates an existing product.
     *
     * @param id the ID of the product to update
     * @param request the update request
     * @return ProductResponse with updated information
     * @throws ResourceNotFoundException if product not found
     * @throws BadRequestException if trying to change to an existing SKU
     */
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

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Update brand if provided
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + request.getBrandId()));
            product.setBrand(brand);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getId());

        return productMapper.toResponse(updatedProduct);
    }

    /**
     * Soft-deletes a product by marking it as inactive.
     * The product record remains in the database but is no longer available
     * for purchase or display in public-facing views.
     *
     * @param id the ID of the product to delete
     * @throws ResourceNotFoundException if product not found
     */
    @Override
    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product deactivated successfully with ID: {}", id);
    }

    /**
     * Retrieves a product by its ID.
     * Includes the average rating from product reviews.
     *
     * @param id the product ID to search for
     * @return ProductResponse containing product details and average rating
     * @throws ResourceNotFoundException if product not found
     */
    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        ProductResponse response = productMapper.toResponse(product);
        // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
        Double avgRating = reviewRepository.findAverageRatingByProductId(id).orElse(null);
        response.setAverageRating(avgRating != null ? avgRating : 0.0);

        return response;
    }

    /**
     * Retrieves a product by its SKU (Stock Keeping Unit).
     * Includes the average rating from product reviews.
     *
     * @param sku the SKU to search for
     * @return ProductResponse containing product details and average rating
     * @throws ResourceNotFoundException if product not found
     */
    @Override
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));

        ProductResponse response = productMapper.toResponse(product);
        // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
        Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
        response.setAverageRating(avgRating != null ? avgRating : 0.0);

        return response;
    }

    /**
     * Retrieves all active products with pagination.
     * Includes average ratings for each product.
     *
     * @param pageable pagination information
     * @return Page of active ProductResponse objects with ratings
     */
    @Override
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    /**
     * Retrieves products by category with pagination.
     *
     * @param categoryId the category ID to filter by
     * @param pageable pagination information
     * @return Page of products in the specified category
     */
    @Override
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    /**
     * Retrieves products by brand with pagination.
     *
     * @param brandId the brand ID to filter by
     * @param pageable pagination information
     * @return Page of products from the specified brand
     */
    @Override
    public Page<ProductResponse> getProductsByBrand(Long brandId, Pageable pageable) {
        return productRepository.findByBrandIdAndIsActiveTrue(brandId, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    /**
     * Retrieves featured products with pagination.
     * Featured products are typically highlighted on the homepage.
     *
     * @param pageable pagination information
     * @return Page of featured products
     */
    @Override
    public Page<ProductResponse> getFeaturedProducts(Pageable pageable) {
        return productRepository.findByIsFeaturedTrueAndIsActiveTrue(pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    /**
     * Searches for products by search term (searches in name and description).
     *
     * @param searchTerm the term to search for
     * @param pageable pagination information
     * @return Page of products matching the search term
     */
    @Override
    public Page<ProductResponse> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.findProductsByFilters(null, null, null, null, searchTerm, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    /**
     * Filters products by multiple criteria.
     *
     * @param categoryId filter by category (optional)
     * @param brandId filter by brand (optional)
     * @param minPrice minimum price filter (optional)
     * @param maxPrice maximum price filter (optional)
     * @param searchTerm search in name/description (optional)
     * @param pageable pagination information
     * @return Page of products matching all provided filters
     */
    @Override
    public Page<ProductResponse> filterProducts(Long categoryId, Long brandId,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                String searchTerm, Pageable pageable) {
        return productRepository.findProductsByFilters(categoryId, brandId, minPrice, maxPrice, searchTerm, pageable)
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    // FIXED: Use findAverageRatingByProductId instead of getAverageRatingForProduct
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    /**
     * Checks if a product has sufficient stock for a requested quantity.
     * Used primarily by cart operations before adding items.
     *
     * @param productId the ID of the product to check
     * @param quantity the requested quantity
     * @return true if sufficient stock exists, false otherwise
     * @throws ResourceNotFoundException if product not found
     */
    @Override
    public boolean checkStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        return product.getStockQuantity() >= quantity;
    }

    /**
     * Updates product stock after an order is placed.
     * Reduces inventory by the ordered quantity.
     *
     * @param productId the ID of the product
     * @param quantity the quantity to reduce
     * @throws ResourceNotFoundException if product not found
     * @throws BadRequestException if insufficient stock
     */
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
    @Override
    public Page<ProductResponse> getAllProductsIncludingInactive(Pageable pageable) {
        return productRepository.findAll(pageable) // This will include inactive products
                .map(product -> {
                    ProductResponse response = productMapper.toResponse(product);
                    Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(null);
                    response.setAverageRating(avgRating != null ? avgRating : 0.0);
                    return response;
                });
    }

    @Override
    public ProductResponse restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setIsActive(true);
        Product restoredProduct = productRepository.save(product);
        log.info("Product restored successfully with ID: {}", id);

        return productMapper.toResponse(restoredProduct);
    }
    // In ProductServiceImpl.java
    @Override
    public long getActiveProductsCount() {
        return productRepository.countByIsActiveTrue();
    }

    @Override
    public long getInactiveProductsCount() {
        return productRepository.countByIsActiveFalse();
    }

    @Override
    public long getFeaturedProductsCount() {
        return productRepository.countByIsFeaturedTrueAndIsActiveTrue();
    }
}