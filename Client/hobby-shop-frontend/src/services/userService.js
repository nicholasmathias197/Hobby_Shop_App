import api from './api';

// User profile
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

// Admin customer management
export const getAllCustomers = async () => {
  const response = await api.get('/customers');
  return response.data;
};

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

export const toggleCustomerStatus = async (id) => {
  const response = await api.put(`/customers/${id}/toggle-status`);
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

// Statistics
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