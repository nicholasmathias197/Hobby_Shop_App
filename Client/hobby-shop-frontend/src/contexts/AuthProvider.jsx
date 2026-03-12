// src/contexts/AuthProvider.jsx
import React, { useState, useEffect } from 'react';
import { AuthContext } from './AuthContext';
import { login as apiLogin, register as apiRegister, getProfile } from '../services/authService';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(localStorage.getItem('token'));

  useEffect(() => {
    if (token) {
      loadUser();
    } else {
      setLoading(false);
    }
  }, [token]);

  const loadUser = async () => {
    try {
      const userData = await getProfile();
      console.log('User profile loaded:', userData);
      setUser(userData);
    } catch (error) {
      console.error('Error loading user:', error);
      logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    try {
      console.log('🔑 AuthProvider.login called with:', email);
      
      const response = await apiLogin(email, password);
      console.log('✅ AuthProvider login response:', response);
      
      if (!response.token) {
        throw new Error('No token received');
      }
      
      localStorage.setItem('token', response.token);
      setToken(response.token);
      
      const userData = {
        id: response.id,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        roles: response.roles || [],
        role: response.roles?.[0]
      };
      setUser(userData);
      
      return response;
    } catch (error) {
      console.error('❌ AuthProvider.login error:', error);
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      console.log('🔑 AuthProvider.register called with email:', userData.email);
      console.log('Session ID in localStorage:', localStorage.getItem('sessionId'));
      
      const response = await apiRegister(userData);
      console.log('✅ AuthProvider register response:', response);
      
      if (!response.token) {
        throw new Error('No token received');
      }
      
      localStorage.setItem('token', response.token);
      setToken(response.token);
      
      const newUser = {
        id: response.id,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        roles: response.roles || [],
        role: response.roles?.[0]
      };
      setUser(newUser);
      
      // Don't clear sessionId yet - cart merge happens on backend
      console.log('✅ User registered successfully, token saved');
      
      return response;
    } catch (error) {
      console.error('❌ AuthProvider.register error:', error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    // Keep sessionId for guest cart
    setToken(null);
    setUser(null);
  };

  const updateUser = (updatedUser) => {
    setUser(updatedUser);
  };

  const isAuthenticated = () => {
    return !!user && !!token;
  };

  const isAdmin = () => {
    return user?.role === 'ADMIN' || user?.roles?.includes('ADMIN');
  };

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      login,
      register,
      logout,
      updateUser,
      isAuthenticated,
      isAdmin
    }}>
      {children}
    </AuthContext.Provider>
  );
};