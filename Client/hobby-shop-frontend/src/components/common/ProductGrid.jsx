// src/components/common/ProductGrid.jsx
import React from 'react';
import ProductCard from './ProductCard';

const ProductGrid = ({ products, onAddToCart }) => {
  // Extra safety: ensure products is an array
  const productsArray = Array.isArray(products) ? products : [];
  
  if (productsArray.length === 0) {
    return (
      <div style={{ 
        textAlign: 'center', 
        padding: '3rem',
        backgroundColor: 'rgba(0, 217, 255, 0.3)',
        borderRadius: '4px'
      }}>
        <p>No products found.</p>
      </div>
    );
  }

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
      gap: '1.5rem'
    }}>
      {productsArray.map(product => (
        <ProductCard 
          key={product.id} 
          product={product} 
          onAddToCart={onAddToCart}
        />
      ))}
    </div>
  );
};

export default ProductGrid;