// src/services/authService.js
import api from './api';

export const login = async (email, password) => {
  try {
    const sessionId = localStorage.getItem('sessionId');
    console.log('📤 AuthService.login - Sending request to /api/auth/login');
    console.log('Request payload:', { email, password: '***' });
    console.log('Session ID:', sessionId);
    
    const response = await api.post('/auth/login', { email, password }, {
      params: sessionId ? { sessionId } : {},
      headers: sessionId ? { 'X-Session-ID': sessionId } : {}
    });
    
    console.log('📥 AuthService.login - Response status:', response.status);
    console.log('📥 AuthService.login - Response headers:', response.headers);
    console.log('📥 AuthService.login - Response data:', response.data);
    
    return response.data;
  } catch (error) {
    console.error('❌ AuthService.login - Error:', error.message);
    if (error.response) {
      console.error('Error status:', error.response.status);
      console.error('Error data:', error.response.data);
      console.error('Error headers:', error.response.headers);
    } else if (error.request) {
      console.error('No response received:', error.request);
    } else {
      console.error('Error setting up request:', error.message);
    }
    throw error;
  }
};

export const register = async (userData) => {
  const sessionId = localStorage.getItem('sessionId');
  const response = await api.post('/auth/register', userData, {
    params: sessionId ? { sessionId } : {},
    headers: sessionId ? { 'X-Session-ID': sessionId } : {}
  });
  return response.data;
};

export const getProfile = async () => {
  const response = await api.get('/customers/profile');
  return response.data;
};

export const logout = () => {
  localStorage.removeItem('token');
};