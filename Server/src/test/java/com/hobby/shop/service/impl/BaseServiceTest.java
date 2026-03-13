package com.hobby.shop.service.impl;

import com.hobby.shop.dto.request.*;
import com.hobby.shop.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Base test class providing common test data creation methods
 * for all service implementation tests.
 */
public abstract class BaseServiceTest {

    // ==================== CUSTOMER RELATED ====================

    protected Customer createTestCustomer(Long id, String email) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setEmail(email);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setPassword("encodedPassword");
        customer.setPhone("123-456-7890");
        customer.setAddress("123 Test St");
        customer.setCity("Test City");
        customer.setPostalCode("12345");
        customer.setCountry("Test Country");
        customer.setEnabled(true);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        Set<Role> roles = new HashSet<>();
        Role role = createTestRole(1L, "ROLE_USER");
        roles.add(role);
        customer.setRoles(roles);

        return customer;
    }

    protected Customer createTestCustomerWithRoles(Long id, String email, Set<Role> roles) {
        Customer customer = createTestCustomer(id, email);
        customer.setRoles(roles);
        return customer;
    }

    protected Role createTestRole(Long id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    protected RegisterRequest createTestRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        return request;
    }

    protected CustomerUpdateRequest createTestCustomerUpdateRequest() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setEmail("updated@example.com");
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPhone("987-654-3210");
        request.setAddress("456 Updated St");
        request.setCity("Updated City");
        request.setPostalCode("67890");
        request.setCountry("Updated Country");
        return request;
    }

    // ==================== PRODUCT RELATED ====================

    protected Product createTestProduct(Long id, String name, String sku, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSku(sku);
        product.setDescription("Test description for " + name);
        product.setPrice(new BigDecimal("99.99"));
        product.setStockQuantity(stock);
        product.setIsActive(true);
        product.setIsFeatured(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Category category = createTestCategory(1L, "Test Category");
        product.setCategory(category);

        Brand brand = createTestBrand(1L, "Test Brand");
        product.setBrand(brand);

        return product;
    }

    protected Product createTestFeaturedProduct(Long id, String name, String sku, int stock) {
        Product product = createTestProduct(id, name, sku, stock);
        product.setIsFeatured(true);
        return product;
    }

    protected Product createTestInactiveProduct(Long id, String name, String sku, int stock) {
        Product product = createTestProduct(id, name, sku, stock);
        product.setIsActive(false);
        return product;
    }

    protected ProductRequest createTestProductRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setSku("TEST-SKU-123");
        request.setDescription("Test Description");
        request.setPrice(new BigDecimal("99.99"));
        request.setStockQuantity(10);
        request.setCategoryId(1L);
        request.setBrandId(1L);
        request.setIsActive(true);
        request.setIsFeatured(false);
        return request;
    }

    // ==================== CATEGORY RELATED ====================

    protected Category createTestCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Description for " + name);
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }

    protected Category createTestInactiveCategory(Long id, String name) {
        Category category = createTestCategory(id, name);
        category.setIsActive(false);
        return category;
    }

    protected CategoryRequest createTestCategoryRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Test Category");
        request.setDescription("Test Category Description");
        return request;
    }

    // ==================== BRAND RELATED ====================

    protected Brand createTestBrand(Long id, String name) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        brand.setDescription("Description for " + name);
        brand.setIsActive(true);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());
        return brand;
    }

    protected Brand createTestInactiveBrand(Long id, String name) {
        Brand brand = createTestBrand(id, name);
        brand.setIsActive(false);
        return brand;
    }

    protected BrandRequest createTestBrandRequest() {
        BrandRequest request = new BrandRequest();
        request.setName("Test Brand");
        request.setDescription("Test Brand Description");
        return request;
    }

    // ==================== CART RELATED ====================

    protected Cart createTestCart(Long id, Customer customer) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setCustomer(customer);
        cart.setItems(new HashSet<>());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cart;
    }

    protected Cart createTestSessionCart(Long id, String sessionId) {
        Cart cart = new Cart();
        cart.setId(id);
        cart.setSessionId(sessionId);
        cart.setItems(new HashSet<>());
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cart;
    }

    protected CartItem createTestCartItem(Long id, Cart cart, Product product, int quantity) {
        CartItem item = new CartItem();
        item.setId(id);
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    protected CartItemRequest createTestCartItemRequest(Long productId, int quantity) {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }

    protected Cart createTestCartWithItems(Long id, Customer customer, List<CartItem> items) {
        Cart cart = createTestCart(id, customer);
        for (CartItem item : items) {
            item.setCart(cart);
            cart.getItems().add(item);
        }
        return cart;
    }

    // ==================== ORDER RELATED ====================

    protected Order createTestOrder(Long id, Customer customer, String orderNumber) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setCustomer(customer);
        order.setStatus("PENDING");
        order.setPaymentStatus("PENDING");
        order.setPaymentMethod("CREDIT_CARD");
        order.setSubtotal(new BigDecimal("99.99"));
        order.setTax(new BigDecimal("8.00"));
        order.setShippingCost(new BigDecimal("5.99"));
        order.setTotalAmount(new BigDecimal("113.98"));
        order.setShippingAddress("123 Test St");
        order.setShippingCity("Test City");
        order.setShippingPostalCode("12345");
        order.setShippingCountry("Test Country");
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        return order;
    }

    protected Order createTestGuestOrder(Long id, String guestEmail, String orderNumber) {
        Order order = createTestOrder(id, null, orderNumber);
        order.setGuestEmail(guestEmail);
        return order;
    }

    protected OrderItem createTestOrderItem(Long id, Order order, Product product, int quantity) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrder(order);
        item.setProduct(product);
        item.setProductName(product.getName());
        item.setProductSku(product.getSku());
        item.setQuantity(quantity);
        item.setPricePerUnit(product.getPrice());
        item.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    protected OrderRequest createTestOrderRequest() {
        OrderRequest request = new OrderRequest();
        request.setPaymentMethod("CREDIT_CARD");
        request.setShippingAddress("123 Test St");
        request.setShippingCity("Test City");
        request.setShippingPostalCode("12345");
        request.setShippingCountry("Test Country");
        request.setNotes("Test order notes");
        return request;
    }

    protected OrderRequest createTestGuestOrderRequest(String guestEmail) {
        OrderRequest request = createTestOrderRequest();
        request.setGuestEmail(guestEmail);
        return request;
    }

    // ==================== REVIEW RELATED ====================

    protected ProductReview createTestReview(Long id, Product product, Customer customer, int rating) {
        ProductReview review = new ProductReview();
        review.setId(id);
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(rating);
        review.setComment("Test review comment");
        review.setIsVerifiedPurchase(false);
        review.setCreatedAt(LocalDateTime.now());
        return review;
    }

    protected ProductReview createTestVerifiedReview(Long id, Product product, Customer customer, int rating) {
        ProductReview review = createTestReview(id, product, customer, rating);
        review.setIsVerifiedPurchase(true);
        return review;
    }

    protected ReviewRequest createTestReviewRequest(int rating) {
        ReviewRequest request = new ReviewRequest();
        request.setRating(rating);
        request.setComment("Test review comment");
        return request;
    }

    // ==================== PAGEABLE HELPERS ====================

    protected <T> Page<T> createPageFromList(List<T> list, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<T> content = start > list.size() ? Collections.emptyList() : list.subList(start, end);
        return new PageImpl<>(content, pageable, list.size());
    }

    // ==================== ASSERTION HELPERS ====================

    protected void assertCustomerFields(Customer expected, Customer actual) {
        assert expected.getId().equals(actual.getId());
        assert expected.getEmail().equals(actual.getEmail());
        assert expected.getFirstName().equals(actual.getFirstName());
        assert expected.getLastName().equals(actual.getLastName());
        assert expected.getPhone().equals(actual.getPhone());
        assert expected.getAddress().equals(actual.getAddress());
        assert expected.getCity().equals(actual.getCity());
        assert expected.getPostalCode().equals(actual.getPostalCode());
        assert expected.getCountry().equals(actual.getCountry());
        assert expected.getEnabled().equals(actual.getEnabled());
    }

    protected void assertProductFields(Product expected, Product actual) {
        assert expected.getId().equals(actual.getId());
        assert expected.getName().equals(actual.getName());
        assert expected.getSku().equals(actual.getSku());
        assert expected.getDescription().equals(actual.getDescription());
        assert expected.getPrice().equals(actual.getPrice());
        assert expected.getStockQuantity().equals(actual.getStockQuantity());
        assert expected.getIsActive().equals(actual.getIsActive());
        assert expected.getIsFeatured().equals(actual.getIsFeatured());
    }
}