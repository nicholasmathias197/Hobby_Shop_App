import api from './api';

/**
 * Login user
 * @param {string} email - User email
 * @param {string} password - User password
 */
export const login = async (email, password) => {
  const sessionId = localStorage.getItem('sessionId');
  const response = await api.post('/auth/login', { email, password }, {
    params: sessionId ? { sessionId } : {},
    headers: sessionId ? { 'X-Session-ID': sessionId } : {}
  });
  
  if (response.data.token) {
    localStorage.setItem('token', response.data.token);
  }
  
  return response.data;
};

/**
 * Register a new user
 * @param {Object} userData - User registration data
 */
export const register = async (userData) => {
  const sessionId = localStorage.getItem('sessionId');
  const response = await api.post('/auth/register', userData, {
    params: sessionId ? { sessionId } : {},
    headers: sessionId ? { 'X-Session-ID': sessionId } : {}
  });
  
  if (response.data.token) {
    localStorage.setItem('token', response.data.token);
  }
  
  return response.data;
};

/**
 * Register a new admin (admin only)
 */
export const registerAdmin = async (userData) => {
  const response = await api.post('/auth/register/admin', userData);
  return response.data;
};

/**
 * Get current user profile
 */
export const getProfile = async () => {
  const response = await api.get('/customers/profile');
  return response.data;
};

/**
 * Verify email with token
 * @param {string} token - Email verification token
 */
export const verifyEmail = async (token) => {
  const response = await api.get('/auth/verify-email', {
    params: { token }
  });
  return response.data;
};

/**
 * Request password reset
 * @param {string} email - User email
 */
export const forgotPassword = async (email) => {
  await api.post('/auth/forgot-password', null, {
    params: { email }
  });
};

/**
 * Reset password with token
 * @param {string} token - Reset token
 * @param {string} newPassword - New password
 */
export const resetPassword = async (token, newPassword) => {
  await api.post('/auth/reset-password', null, {
    params: { token, newPassword }
  });
};

/**
 * Change password (authenticated)
 * @param {string} oldPassword - Current password
 * @param {string} newPassword - New password
 */
export const changePassword = async (oldPassword, newPassword) => {
  await api.post('/auth/change-password', null, {
    params: { oldPassword, newPassword }
  });
};

/**
 * Logout user
 */
export const logout = () => {
  localStorage.removeItem('token');
  // Don't remove sessionId - guest cart persists
};