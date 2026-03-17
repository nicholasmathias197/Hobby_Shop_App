// src/pages/HomePage/FeaturedProducts.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getFeaturedProductsArray } from '../../services/productService';
import ProductGrid from '../../components/common/ProductGrid';
import ProductCardSkeleton from '../../components/common/ProductCardSkeleton';
import { useCart } from '../../hooks/useCart';
import { Button } from '../../components/ui';

const FeaturedProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { addToCart } = useCart();

  useEffect(() => {
    const fetchFeatured = async () => {
      try {
        const featuredArray = await getFeaturedProductsArray(0, 8);
        console.log('Featured products loaded:', featuredArray.length);
        setProducts(featuredArray);
        setError(null);
      } catch (error) {
        console.error('Error loading featured products:', error);
        setError('Failed to load featured products');
      } finally {
        setLoading(false);
      }
    };

    fetchFeatured();
  }, []);

  if (loading) {
    return (
      <section style={{ marginBottom: '3rem' }}>
        <h2 style={{ marginBottom: '1.5rem' }}>Featured Products</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1.5rem' }}>
          {[...Array(4)].map((_, i) => <ProductCardSkeleton key={i} />)}
        </div>
      </section>
    );
  }

  if (error) {
    return (
      <section style={{ marginBottom: '3rem' }}>
        <h2 style={{ marginBottom: '1.5rem' }}>Featured Products</h2>
        <div style={{ 
          textAlign: 'center', 
          padding: '2rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <p style={{ color: '#dc3545' }}>{error}</p>
          <Button variant="secondary" onClick={() => window.location.reload()}>
            Try Again
          </Button>
        </div>
      </section>
    );
  }

  if (products.length === 0) {
    return (
      <section style={{ marginBottom: '3rem' }}>
        <h2 style={{ marginBottom: '1.5rem' }}>Featured Products</h2>
        <div style={{ 
          textAlign: 'center', 
          padding: '3rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <p>No featured products available at the moment.</p>
          <Link to="/products">
            <Button variant="primary" style={{ marginTop: '1rem' }}>
              Browse All Products
            </Button>
          </Link>
        </div>
      </section>
    );
  }

  return (
    <section style={{ marginBottom: '3rem' }}>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '1.5rem'
      }}>
        <h2>Featured Products</h2>
        <Link to="/products?featured=true" style={{ color: '#007bff', textDecoration: 'none' }}>
          View All Featured →
        </Link>
      </div>
      
      <ProductGrid products={products} onAddToCart={addToCart} />
    </section>
  );
};

export default FeaturedProducts;