import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Add session ID for guest cart
    const sessionId = localStorage.getItem('sessionId');
    if (sessionId && !token) {
      config.headers['X-Session-ID'] = sessionId;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => {
    // Capture session ID from cookie for guest users
    if (response.headers['set-cookie']) {
      const cookies = response.headers['set-cookie'];
      const sessionCookie = cookies.find(c => c.includes('CART_SESSION_ID'));
      if (sessionCookie) {
        const sessionId = sessionCookie.split('=')[1].split(';')[0];
        localStorage.setItem('sessionId', sessionId);
      }
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('sessionId');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;