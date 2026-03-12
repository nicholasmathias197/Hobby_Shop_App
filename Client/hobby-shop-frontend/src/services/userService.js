// src/services/userService.js
import api from './api';

// ============= USER PROFILE =============

export const getProfile = async () => {
  const response = await api.get('/customers/profile');
  return response.data;
};

export const updateProfile = async (profileData) => {
  const response = await api.put('/customers/profile', profileData);
  return response.data;
};

export const changePassword = async (oldPassword, newPassword) => {
  const response = await api.put('/customers/profile/change-password', {
    oldPassword,
    newPassword
  });
  return response.data;
};

// ============= ADMIN CUSTOMER MANAGEMENT =============

/**
 * Get all customers (admin only)
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const getAllCustomers = async (page = 0, size = 20) => {
  const response = await api.get('/customers', {
    params: { page, size }
  });
  return response.data;
};

/**
 * Get customer by ID (admin only)
 * @param {number} id - Customer ID
 */
export const getCustomerById = async (id) => {
  const response = await api.get(`/customers/${id}`);
  return response.data;
};

export const getCustomerByEmail = async (email) => {
  const response = await api.get(`/customers/email/${email}`);
  return response.data;
};

export const updateCustomer = async (id, customerData) => {
  const response = await api.put(`/customers/${id}`, customerData);
  return response.data;
};

/**
 * Toggle customer status (enable/disable)
 * @param {number} id - Customer ID
 * @param {boolean} enabled - Desired status
 */
export const toggleCustomerStatus = async (id, enabled) => {
  const response = await api.put(`/customers/${id}/toggle-status`, { enabled });
  return response.data;
};

export const updateCustomerRole = async (id, roleId) => {
  const response = await api.post(`/customers/${id}/roles`, { roleId });
  return response.data;
};

export const deleteCustomerRole = async (id, roleId) => {
  const response = await api.delete(`/customers/${id}/roles`, {
    data: { roleId }
  });
  return response.data;
};

export const deleteCustomer = async (id) => {
  const response = await api.delete(`/customers/${id}`);
  return response.data;
};

// ============= STATISTICS =============

export const getCustomerStats = async () => {
  const response = await api.get('/customers/statistics');
  return response.data;
};

export const getActiveCustomers = async () => {
  const response = await api.get('/customers/statistics/active');
  return response.data;
};

export const getInactiveCustomers = async () => {
  const response = await api.get('/customers/statistics/inactive');
  return response.data;
};