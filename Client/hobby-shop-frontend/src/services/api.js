// src/services/api.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // CRITICAL: This allows cookies to be sent and received
});

// Request interceptor to add token and session ID
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
      console.log('📤 Adding session ID to request:', sessionId);
    }
    
    console.log('📤 Request:', {
      url: config.url,
      method: config.method,
      hasToken: !!token,
      hasSessionId: !!sessionId,
      withCredentials: config.withCredentials
    });
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors and capture session ID
api.interceptors.response.use(
  (response) => {
    console.log('📥 Response:', {
      url: response.config.url,
      status: response.status,
      headers: response.headers
    });
    
    // CAPTURE SESSION ID FROM COOKIE - THIS IS CRITICAL
    const setCookie = response.headers['set-cookie'];
    if (setCookie) {
      console.log('🍪 Set-Cookie header received:', setCookie);
      
      // Handle both string and array formats
      const cookies = Array.isArray(setCookie) ? setCookie : [setCookie];
      
      const sessionCookie = cookies.find(c => c.includes('CART_SESSION_ID'));
      if (sessionCookie) {
        // Parse the cookie value
        const match = sessionCookie.match(/CART_SESSION_ID=([^;]+)/);
        if (match) {
          const sessionId = match[1];
          localStorage.setItem('sessionId', sessionId);
          console.log('✅✅✅ Session ID saved to localStorage:', sessionId);
        }
      }
    } else {
      console.log('❌ No Set-Cookie header in response');
    }
    
    return response;
  },
  (error) => {
    console.error('❌ API Error:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data
    });
    
    if (error.response?.status === 401) {
      if (error.config.url.includes('/auth/') === false) {
        localStorage.removeItem('token');
      }
    }
    return Promise.reject(error);
  }
);

// Helper function to extract content from paginated responses
export const extractContent = (response) => {
  if (response && response.content && Array.isArray(response.content)) {
    return {
      content: response.content,
      totalPages: response.totalPages,
      totalElements: response.totalElements,
      currentPage: response.number,
      pageSize: response.size,
      isFirst: response.first,
      isLast: response.last
    };
  }
  
  if (Array.isArray(response)) {
    return {
      content: response,
      totalPages: 1,
      totalElements: response.length,
      currentPage: 0,
      pageSize: response.length,
      isFirst: true,
      isLast: true
    };
  }
  
  if (response && typeof response === 'object') {
    return {
      content: [response],
      totalPages: 1,
      totalElements: 1,
      currentPage: 0,
      pageSize: 1,
      isFirst: true,
      isLast: true
    };
  }
  
  console.warn('Unexpected response format:', response);
  return {
    content: [],
    totalPages: 0,
    totalElements: 0,
    currentPage: 0,
    pageSize: 0,
    isFirst: true,
    isLast: true
  };
};

// Helper to just get the content array
export const extractContentArray = (response) => {
  const paginated = extractContent(response);
  return paginated.content;
};

export default api;