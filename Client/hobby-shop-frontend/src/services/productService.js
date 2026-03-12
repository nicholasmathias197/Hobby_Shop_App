import api, { extractContent, extractContentArray } from './api';

// ============= PUBLIC ENDPOINTS =============

/**
 * Get paginated list of all products
 * @param {number} page - Page number (0-based)
 * @param {number} size - Page size
 * @returns {Promise<Object>} Paginated response with content array
 */
export const getAllProducts = async (page = 0, size = 20) => {
  const response = await api.get('/products', {
    params: { page, size }
  });
  console.log('Products API Response:', response.data);
  return extractContent(response.data);
};

/**
 * Get products array only (convenience for home page)
 */
export const getAllProductsArray = async (page = 0, size = 20) => {
  try {
    const response = await api.get('/products', {
      params: { page, size }
    });
    console.log('getAllProductsArray raw data:', response.data);
    const result = extractContentArray(response.data);
    console.log('getAllProductsArray extracted:', result);
    return result;
  } catch (error) {
    console.error('Error in getAllProductsArray:', error);
    return []; // Return empty array on error
  }
};

/**
 * Get product by ID
 * @param {number} id - Product ID
 * @returns {Promise<Object>} Product response
 */
export const getProductById = async (id) => {
  const response = await api.get(`/products/${id}`);
  return response.data;
};

/**
 * Get product by SKU
 * @param {string} sku - Product SKU
 * @returns {Promise<Object>} Product response
 */
export const getProductBySku = async (sku) => {
  const response = await api.get(`/products/sku/${sku}`);
  return response.data;
};

/**
 * Get paginated list of featured products
 * @param {number} page - Page number (0-based)
 * @param {number} size - Page size (defaults to 10)
 */
export const getFeaturedProducts = async (page = 0, size = 10) => {
  const response = await api.get('/products/featured', {
    params: { page, size }
  });
  console.log('Featured API Response:', response.data);
  return extractContent(response.data);
};

/**
 * Get featured products array only
 */
export const getFeaturedProductsArray = async (page = 0, size = 10) => {
  try {
    const response = await api.get('/products/featured', {
      params: { page, size }
    });
    console.log('getFeaturedProductsArray raw:', response.data);
    const result = extractContentArray(response.data);
    console.log('getFeaturedProductsArray extracted:', result);
    return result;
  } catch (error) {
    console.error('Error in getFeaturedProductsArray:', error);
    return [];
  }
};

/**
 * Get products by brand
 * @param {number} brandId - Brand ID
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const getProductsByBrand = async (brandId, page = 0, size = 20) => {
  const response = await api.get(`/products/brand/${brandId}`, {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Get products by category
 * @param {number} categoryId - Category ID
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const getProductsByCategory = async (categoryId, page = 0, size = 20) => {
  const response = await api.get(`/products/category/${categoryId}`, {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Search products by keyword
 * @param {string} searchTerm - Search term
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const searchProducts = async (searchTerm, page = 0, size = 20) => {
  const response = await api.get('/products/search', {
    params: { q: searchTerm, page, size }
  });
  return extractContent(response.data);
};

/**
 * Filter products with multiple criteria
 * @param {Object} filters - Filter criteria
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const filterProducts = async (filters = {}, page = 0, size = 20) => {
  const params = { 
    ...filters,
    page, 
    size 
  };
  // Remove undefined values
  Object.keys(params).forEach(key => params[key] === undefined && delete params[key]);
  
  const response = await api.get('/products/filter', { params });
  return extractContent(response.data);
};

// ============= ADMIN ENDPOINTS =============

/**
 * Create a new product (admin only)
 * @param {Object} productData - Product data
 */
export const createProduct = async (productData) => {
  const response = await api.post('/products', productData);
  return response.data;
};

/**
 * Update an existing product (admin only)
 * @param {number} id - Product ID
 * @param {Object} productData - Updated product data
 */
export const updateProduct = async (id, productData) => {
  const response = await api.put(`/products/${id}`, productData);
  return response.data;
};

/**
 * Delete a product (soft delete) (admin only)
 * @param {number} id - Product ID
 */
export const deleteProduct = async (id) => {
  const response = await api.delete(`/products/${id}`);
  return response.data;
};
/**
 * Get all products including inactive (soft-deleted) - Admin only
 */
export const getAllProductsIncludingInactive = async (page = 0, size = 20) => {
  const response = await api.get('/products/admin/all', {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Restore a soft-deleted product - Admin only
 * @param {number} id - Product ID
 */
export const restoreProduct = async (id) => {
  const response = await api.put(`/products/${id}/restore`);
  return response.data;
};
// src/services/productService.js - add these functions

/**
 * Get count of active products (admin only)
 */
export const getActiveProductsCount = async () => {
  const response = await api.get('/products/count/active');
  return response.data;
};

/**
 * Get count of inactive products (admin only)
 */
export const getInactiveProductsCount = async () => {
  const response = await api.get('/products/count/inactive');
  return response.data;
};

/**
 * Get count of featured products (admin only)
 */
export const getFeaturedProductsCount = async () => {
  const response = await api.get('/products/count/featured');
  return response.data;
};