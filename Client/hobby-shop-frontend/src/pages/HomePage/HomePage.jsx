// src/pages/HomePage/HomePage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import HeroBanner from './HeroBanner';
import FeaturedProducts from './FeaturedProducts';
import CategoryShowcase from './CategoryShowcase';
import { getAllProductsArray } from '../../services/productService';
import { getActiveCategoriesArray } from '../../services/categoryService';
import ProductGrid from "/src/components/common/ProductGrid.jsx";
import { useCart } from '../../hooks/useCart'; 
import { Button } from '../../components/ui';    

const HomePage = () => {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [newArrivals, setNewArrivals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { addToCart } = useCart();

  useEffect(() => {
    const fetchHomePageData = async () => {
      try {
        // Fetch all data in parallel
        const [productsData, categoriesData, newArrivalsData] = await Promise.all([
          getAllProductsArray(0, 8), // Get first 8 products
          getActiveCategoriesArray(),  // Get all active categories
          getAllProductsArray(0, 4)    // Get 4 products for new arrivals
        ]);
        
        setProducts(productsData);
        setCategories(categoriesData.slice(0, 6)); // Show only first 6 categories
        setNewArrivals(newArrivalsData);
        setError(null);
      } catch (err) {
        setError(err.message);
        console.error('Error loading home page:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchHomePageData();
  }, []);

  if (loading) return (
    <div style={{ textAlign: 'center', padding: '3rem' }}>
      <div className="spinner"></div>
      <p>Loading amazing products...</p>
    </div>
  );
  
  if (error) return (
    <div style={{ textAlign: 'center', padding: '3rem' }}>
      <h2 style={{ color: '#dc3545' }}>Oops! Something went wrong</h2>
      <p>{error}</p>
      <Button variant="primary" onClick={() => window.location.reload()}>
        Try Again
      </Button>
    </div>
  );

  return (
    <div>
      <HeroBanner />
      
      {/* Categories Showcase */}
      {categories.length > 0 && (
        <section style={{ marginBottom: '3rem' }}>
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            marginBottom: '1.5rem'
          }}>
            <h2>Shop by Category</h2>
          </div>
          <CategoryShowcase categories={categories} />
        </section>
      )}
      
      {/* Featured Products */}
      <FeaturedProducts />
      
      {/* New Arrivals Section */}
      {newArrivals.length > 0 && (
        <section style={{ marginBottom: '3rem' }}>
          <div style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            marginBottom: '1.5rem'
          }}>
            <h2>New Arrivals</h2>
            <Link to="/products?sort=newest" style={{ color: '#007bff', textDecoration: 'none' }}>
              View All New Products →
            </Link>
          </div>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', 
            gap: '1rem' 
          }}>
            {newArrivals.map(product => (
              <div key={product.id} style={{
                border: '1px solid #ddd',
                borderRadius: '4px',
                padding: '1rem',
                position: 'relative'
              }}>
                {!product.imageUrl ? (
                  <div style={{
                    width: '100%',
                    height: '150px',
                    backgroundColor: '#f8f9fa',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    marginBottom: '0.5rem'
                  }}>
                    No Image
                  </div>
                ) : (
                  <img 
                    src={product.imageUrl} 
                    alt={product.name}
                    style={{ width: '100%', height: '150px', objectFit: 'cover', marginBottom: '0.5rem' }}
                  />
                )}
                <span style={{
                  position: 'absolute',
                  top: '0.5rem',
                  right: '0.5rem',
                  backgroundColor: '#28a745',
                  color: 'white',
                  padding: '0.25rem 0.5rem',
                  borderRadius: '4px',
                  fontSize: '0.75rem'
                }}>
                  NEW
                </span>
                <h3 style={{ fontSize: '1rem', marginBottom: '0.25rem' }}>{product.name}</h3>
                <p style={{ fontWeight: 'bold', color: '#007bff' }}>${product.price}</p>
                <Button 
                  variant="primary" 
                  onClick={() => addToCart(product)}
                  disabled={product.stockQuantity === 0}
                  fullWidth
                >
                  {product.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
                </Button>
              </div>
            ))}
          </div>
        </section>
      )}
      
      {/* Popular Products Section */}
      {products.length > 0 && (
        <section style={{ marginBottom: '3rem' }}>
          <h2 style={{ marginBottom: '1.5rem' }}>Popular Products</h2>
          <ProductGrid products={products} onAddToCart={addToCart} />
          <div style={{ textAlign: 'center', marginTop: '2rem' }}>
            <Link to="/products">
              <Button variant="primary">View All Products</Button>
            </Link>
          </div>
        </section>
      )}
      
  
    </div>
  );
};

export default HomePage;