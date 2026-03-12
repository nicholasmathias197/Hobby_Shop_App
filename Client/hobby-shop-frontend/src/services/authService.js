// src/services/authService.js
import api from './api';

export const login = async (email, password) => {
  const sessionId = localStorage.getItem('sessionId');
  console.log('Login with session ID:', sessionId);
  
  const response = await api.post('/auth/login', { email, password }, {
    params: sessionId ? { sessionId } : {},
    headers: sessionId ? { 'X-Session-ID': sessionId } : {}
  });
  return response.data;
};

export const register = async (userData) => {
  const sessionId = localStorage.getItem('sessionId');
  console.log('🔐 Register with session ID:', sessionId);
  console.log('User data:', userData);
  
  try {
    const response = await api.post('/auth/register', userData, {
      params: sessionId ? { sessionId } : {},
      headers: sessionId ? { 'X-Session-ID': sessionId } : {}
    });
    console.log('Register response:', response.data);
    return response.data;
  } catch (error) {
    console.error('Register error:', error.response?.data || error.message);
    throw error;
  }
};

export const getProfile = async () => {
  const response = await api.get('/customers/profile');
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('token');
  // Keep sessionId for guest cart
};