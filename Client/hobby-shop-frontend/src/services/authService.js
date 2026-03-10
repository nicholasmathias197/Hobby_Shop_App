import api from './api';

export const login = async (email, password) => {
  const response = await api.post('/auth/login', { email, password });
  return response.data;
};

export const register = async (userData) => {
  const sessionId = localStorage.getItem('sessionId');
  const response = await api.post('/auth/register', userData, {
    headers: sessionId ? { 'X-Session-ID': sessionId } : {}
  });
  return response.data;
};

export const getProfile = async () => {
  const response = await api.get('/customers/profile');
  return response.data;
};