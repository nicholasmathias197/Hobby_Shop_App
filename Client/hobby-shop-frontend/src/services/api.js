import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
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
    if (sessionId) {
      config.headers['X-Session-ID'] = sessionId;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors and capture session ID
api.interceptors.response.use(
  (response) => {
    // Capture session ID from cookie for guest users
    const setCookie = response.headers['set-cookie'];
    if (setCookie) {
      const sessionCookie = setCookie.find(c => c.includes('CART_SESSION_ID'));
      if (sessionCookie) {
        const sessionId = sessionCookie.split('=')[1].split(';')[0];
        localStorage.setItem('sessionId', sessionId);
        console.log('Session ID saved:', sessionId);
      }
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Only clear token for auth errors, keep session for guest cart
      if (error.config.url.includes('/auth/') === false) {
        localStorage.removeItem('token');
      }
    }
    return Promise.reject(error);
  }
);

// Helper function to extract content from paginated responses
export const extractContent = (response) => {
  // If it's a paginated response (Spring Boot Page)
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
  
  // If it's already an array
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
  
  // If it's a single object
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
  
  // Default: return empty
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