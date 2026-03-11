import api, { extractContent } from './api';

// ============= PUBLIC ENDPOINTS =============

/**
 * Get paginated list of all brands
 */
export const getBrands = async (page = 0, size = 20) => {
  const response = await api.get('/brands', {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Get all brands as array (for dropdowns, filters)
 */
export const getBrandsArray = async () => {
  const response = await api.get('/brands/all');
  return response.data; // This endpoint returns List
};

/**
 * Get paginated list of active brands
 */
export const getActiveBrands = async (page = 0, size = 20) => {
  const response = await api.get('/brands/active', {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Get active brands array
 */
export const getActiveBrandsArray = async () => {
  const response = await api.get('/brands/all');
  return response.data;
};

/**
 * Get brand by ID
 */
export const getBrandById = async (id) => {
  const response = await api.get(`/brands/${id}`);
  return response.data;
};

/**
 * Get brand by name
 */
export const getBrandByName = async (name) => {
  const response = await api.get(`/brands/name/${name}`);
  return response.data;
};

// ============= ADMIN ENDPOINTS =============

/**
 * Create a new brand (admin only)
 */
export const createBrand = async (brandData) => {
  const response = await api.post('/brands', brandData);
  return response.data;
};

/**
 * Update a brand (admin only)
 */
export const updateBrand = async (id, brandData) => {
  const response = await api.put(`/brands/${id}`, brandData);
  return response.data;
};

/**
 * Delete a brand (soft delete) (admin only)
 */
export const deleteBrand = async (id) => {
  const response = await api.delete(`/brands/${id}`);
  return response.data;
};