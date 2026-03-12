// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { Input, Button } from '../components/ui';

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const { refreshCart } = useCart();
  
  // Get redirect from query params or state
  const searchParams = new URLSearchParams(location.search);
  let redirect = searchParams.get('redirect') || '/';
  
  // Normalize the redirect path
  if (redirect === 'checkout') {
    redirect = '/checkout';
  } else if (redirect === 'cart') {
    redirect = '/cart';
  } else if (redirect === 'profile') {
    redirect = '/profile';
  } else if (redirect === 'orders') {
    redirect = '/orders';
  }

  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      console.log('🔐 Login attempt for:', formData.email);
      console.log('Redirect target:', redirect);
      
      await login(formData.email, formData.password);
      console.log('✅ Login successful');
      
      // Wait for backend to process cart merge
      await new Promise(resolve => setTimeout(resolve, 500));
      
      console.log('🔄 Refreshing cart...');
      await refreshCart();
      console.log('✅ Cart refreshed');
      
      // Navigate to the redirect path
      navigate(redirect, { replace: true });
    } catch (error) {
      console.error('❌ Login error:', error);
      setError(error.response?.data?.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-header">
          <h1>Welcome Back</h1>
          <p className="login-subtitle">Sign in to continue your hobby journey</p>
        </div>

        {redirect !== '/' && (
          <div className="login-redirect-message">
            <span className="info-icon">ℹ️</span>
            <span>Please log in to {redirect === '/checkout' ? 'complete your purchase' : 'continue'}</span>
          </div>
        )}
        
        {error && (
          <div className="alert alert-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="login-form">
          <Input
            label="Email"
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            placeholder="Enter your email"
          />
          
          <Input
            label="Password"
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            placeholder="Enter your password"
          />

          <div className="form-options">
            <label className="remember-me">
              <input type="checkbox" /> Remember me
            </label>
            <Link to="/forgot-password" className="forgot-password">
              Forgot Password?
            </Link>
          </div>

          <Button 
            type="submit" 
            variant="primary" 
            disabled={loading} 
            fullWidth
            className="login-button"
          >
            {loading ? 'Logging in...' : 'Login'}
          </Button>
        </form>

        <div className="login-footer">
          <p>
            Don't have an account? <Link to="/register" className="register-link">Create Account</Link>
          </p>
          <p className="guest-note">
            <span className="guest-icon">🛒</span>
            Shopping as a guest? Your cart will be saved when you log in.
          </p>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;