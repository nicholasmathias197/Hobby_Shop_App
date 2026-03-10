import React from 'react';
import { Link } from 'react-router-dom';

const HeroBanner = () => {
  return (
    <div style={{
      backgroundColor: '#007bff',
      color: 'white',
      padding: '3rem',
      textAlign: 'center',
      borderRadius: '4px',
      marginBottom: '2rem'
    }}>
      <h1 style={{ fontSize: '2.5rem', marginBottom: '1rem' }}>
        Welcome to Hobby Shop
      </h1>
      <p style={{ fontSize: '1.2rem', marginBottom: '2rem' }}>
        Your one-stop shop for all hobby needs
      </p>
      <Link 
        to="/products"
        style={{
          backgroundColor: 'white',
          color: '#007bff',
          padding: '0.75rem 2rem',
          textDecoration: 'none',
          borderRadius: '4px',
          fontWeight: 'bold'
        }}
      >
        Shop Now
      </Link>
    </div>
  );
};

export default HeroBanner;