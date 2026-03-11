import api, { extractContent } from './api';

// ============= PUBLIC ENDPOINTS =============

/**
 * Get paginated list of all categories
 */
export const getCategories = async (page = 0, size = 20) => {
  const response = await api.get('/categories', {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Get all categories as array (for dropdowns, filters)
 */
export const getCategoriesArray = async () => {
  const response = await api.get('/categories/all');
  return response.data; // This endpoint returns List, not Page
};

/**
 * Get paginated list of active categories
 */
export const getActiveCategories = async (page = 0, size = 20) => {
  const response = await api.get('/categories/active', {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Get active categories array
 */
export const getActiveCategoriesArray = async () => {
  const response = await api.get('/categories/all');
  return response.data; // This endpoint returns List
};

/**
 * Get category by ID
 */
export const getCategoryById = async (id) => {
  const response = await api.get(`/categories/${id}`);
  return response.data;
};

/**
 * Get category by name
 */
export const getCategoryByName = async (name) => {
  const response = await api.get(`/categories/name/${name}`);
  return response.data;
};

// ============= ADMIN ENDPOINTS =============

/**
 * Create a new category (admin only)
 */
export const createCategory = async (categoryData) => {
  const response = await api.post('/categories', categoryData);
  return response.data;
};

/**
 * Update a category (admin only)
 */
export const updateCategory = async (id, categoryData) => {
  const response = await api.put(`/categories/${id}`, categoryData);
  return response.data;
};

/**
 * Delete a category (soft delete) (admin only)
 */
export const deleteCategory = async (id) => {
  const response = await api.delete(`/categories/${id}`);
  return response.data;
};