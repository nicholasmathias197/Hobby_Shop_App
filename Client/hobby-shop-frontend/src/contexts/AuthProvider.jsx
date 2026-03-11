// src/contexts/AuthProvider.jsx
import React, { useState, useEffect } from 'react';
import { AuthContext } from './AuthContext';
import { login as apiLogin, register as apiRegister, getProfile } from '../services/authService';

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(localStorage.getItem('token'));

  useEffect(() => {
    console.log('AuthProvider mounted, token:', token);
    if (token) {
      loadUser();
    } else {
      console.log('No token, setting loading to false');
      setLoading(false);
    }
  }, [token]);

  const loadUser = async () => {
    try {
      console.log('Loading user profile with token:', token);
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
      console.log('🔐 AuthProvider.login called with:', email);
      
      const response = await apiLogin(email, password);
      console.log('✅ AuthProvider received response:', response);
      
      if (!response.token) {
        console.error('❌ No token in response!');
        throw new Error('No token received from server');
      }
      
      console.log('💾 Saving token to localStorage:', response.token.substring(0, 20) + '...');
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
      console.log('👤 Setting user data:', userData);
      setUser(userData);
      
      return response;
    } catch (error) {
      console.error('❌ AuthProvider.login error:', error);
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const response = await apiRegister(userData);
      console.log('Register response:', response);
      
      localStorage.setItem('token', response.token);
      setToken(response.token);
      setUser(response);
      return response;
    } catch (error) {
      console.error('Register error:', error);
      throw error;
    }
  };

  const logout = () => {
    console.log('Logging out');
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  const updateUser = (updatedUser) => {
    setUser(updatedUser);
  };

  const isAuthenticated = () => {
    const auth = !!user && !!token;
    console.log('isAuthenticated check:', { user: !!user, token: !!token, result: auth });
    return auth;
  };

  const isAdmin = () => {
    const hasAdminRole = user?.role === 'ADMIN' || user?.roles?.includes('ADMIN');
    console.log('isAdmin check:', {
      user,
      role: user?.role,
      roles: user?.roles,
      hasAdminRole
    });
    return hasAdminRole;
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    updateUser,
    isAuthenticated,
    isAdmin
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};