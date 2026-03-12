// src/pages/RegisterPage.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useCart } from '../hooks/useCart';
import { Input, Button } from '../components/ui';

const RegisterPage = () => {
  const navigate = useNavigate();
  const { register } = useAuth();
  const { refreshCart, cartItemsCount } = useCart();
  
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    city: '',
    state: '',
    postalCode: '',
    country: 'USA'
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [hasGuestItems, setHasGuestItems] = useState(false);

  useEffect(() => {
    // Check if guest has items in cart
    const checkGuestItems = async () => {
      if (cartItemsCount > 0) {
        setHasGuestItems(true);
        console.log('🛒 Guest has', cartItemsCount, 'items in cart');
      }
    };
    checkGuestItems();
  }, [cartItemsCount]);

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
      // Log the session ID before registration
      const sessionId = localStorage.getItem('sessionId');
      console.log('📝 Registering with email:', formData.email);
      console.log('🔑 Session ID before registration:', sessionId);
      console.log('🛒 Cart items before registration:', cartItemsCount);
      
      // Register - this should trigger backend cart merge
      await register(formData);
      console.log('✅ Registration successful');
      
      // Wait longer for backend to process cart merge
      console.log('⏳ Waiting for backend to process cart merge...');
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Force cart refresh to get merged cart
      console.log('🔄 Refreshing cart after registration...');
      const refreshedCart = await refreshCart();
      console.log('✅ Cart refreshed:', refreshedCart);
      
      // Check if cart has items after refresh
      if (refreshedCart?.items?.length > 0) {
        console.log('✅ Cart merged successfully with', refreshedCart.items.length, 'items');
      } else {
        console.log('❌ Cart is empty after registration - merge may have failed');
      }
      
      navigate('/');
    } catch (error) {
      console.error('❌ Registration error:', error);
      setError(error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-page">
      <div className="register-container">
        <div className="register-header">
          <h1>Create Account</h1>
          <p className="register-subtitle">Join the U197 Hobbies community</p>
        </div>

        {hasGuestItems && (
          <div className="cart-merge-message">
            <span className="cart-icon">🛒</span>
            <div className="message-content">
              <strong>Your cart has {cartItemsCount} items!</strong>
              <p>These items will be saved to your account after registration.</p>
            </div>
          </div>
        )}
        
        {error && (
          <div className="alert alert-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="register-form">
          <div className="form-row">
            <Input
              label="First Name"
              name="firstName"
              value={formData.firstName}
              onChange={handleChange}
              required
              placeholder="John"
            />
            <Input
              label="Last Name"
              name="lastName"
              value={formData.lastName}
              onChange={handleChange}
              required
              placeholder="Doe"
            />
          </div>

          <Input
            label="Email"
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            placeholder="john@example.com"
          />

          <Input
            label="Password"
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            placeholder="••••••••"
          />

          <Input
            label="Phone"
            name="phone"
            value={formData.phone}
            onChange={handleChange}
            required
            placeholder="(555) 123-4567"
          />

          <Input
            label="Address"
            name="address"
            value={formData.address}
            onChange={handleChange}
            required
            placeholder="123 Main St"
          />

          <div className="form-row">
            <Input
              label="City"
              name="city"
              value={formData.city}
              onChange={handleChange}
              required
              placeholder="New York"
            />
            <Input
              label="State"
              name="state"
              value={formData.state}
              onChange={handleChange}
              required
              placeholder="NY"
            />
          </div>

          <div className="form-row">
            <Input
              label="Postal Code"
              name="postalCode"
              value={formData.postalCode}
              onChange={handleChange}
              required
              placeholder="10001"
            />
            <Input
              label="Country"
              name="country"
              value={formData.country}
              onChange={handleChange}
              required
              placeholder="USA"
            />
          </div>

          <Button 
            type="submit" 
            variant="primary" 
            disabled={loading} 
            fullWidth
            className="register-button"
          >
            {loading ? 'Creating Account...' : 'Register'}
          </Button>
        </form>

        <div className="register-footer">
          <p>
            Already have an account? <Link to="/login" className="login-link">Sign In</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;