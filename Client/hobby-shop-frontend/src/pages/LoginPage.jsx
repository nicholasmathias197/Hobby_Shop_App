// src/pages/LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { Input, Button } from '../components/ui';

const LoginPage = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
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
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      console.log('1. Starting login process for:', formData.email);
      console.log('2. Current localStorage before login:', {
        token: localStorage.getItem('token'),
        sessionId: localStorage.getItem('sessionId')
      });
      
      const response = await login(formData.email, formData.password);
      
      console.log('3. Login response received:', response);
      console.log('4. Token in response:', response.token);
      console.log('5. User data in response:', {
        id: response.id,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        roles: response.roles
      });
      
      console.log('6. localStorage after login:', {
        token: localStorage.getItem('token'),
        sessionId: localStorage.getItem('sessionId')
      });
      
      navigate('/');
    } catch (error) {
      console.error('❌ Login error:', error);
      console.error('Error response data:', error.response?.data);
      console.error('Error status:', error.response?.status);
      console.error('Error headers:', error.response?.headers);
      
      setError(error.response?.data?.message || error.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '2rem auto' }}>
      <h1 style={{ textAlign: 'center', marginBottom: '2rem' }}>Login</h1>
      
      {error && (
        <div style={{
          padding: '0.75rem',
          backgroundColor: '#f8d7da',
          color: '#721c24',
          borderRadius: '4px',
          marginBottom: '1rem'
        }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <Input
          label="Email"
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          required
        />
        <Input
          label="Password"
          type="password"
          name="password"
          value={formData.password}
          onChange={handleChange}
          required
        />
        <Button type="submit" variant="primary" disabled={loading} fullWidth>
          {loading ? 'Logging in...' : 'Login'}
        </Button>
      </form>

      <p style={{ textAlign: 'center', marginTop: '1rem' }}>
        Don't have an account? <Link to="/register">Register</Link>
      </p>
    </div>
  );
};

export default LoginPage;