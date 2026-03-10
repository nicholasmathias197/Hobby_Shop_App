import React from 'react';
import { Link } from 'react-router-dom';

const ProductCard = ({ product, onAddToCart }) => {
  return (
    <div style={{
      border: '1px solid #ddd',
      borderRadius: '4px',
      padding: '1rem',
      marginBottom: '1rem'
    }}>
      {product.imageUrl && (
        <img 
          src={product.imageUrl} 
          alt={product.name}
          style={{ maxWidth: '100%', height: '200px', objectFit: 'cover' }}
        />
      )}
      <h3>
        <Link to={`/product/${product.id}`} style={{ textDecoration: 'none', color: '#333' }}>
          {product.name}
        </Link>
      </h3>
      <p>{product.description}</p>
      <p><strong>${product.price}</strong></p>
      <p>Stock: {product.stockQuantity}</p>
      <p>Brand: {product.brandName}</p>
      <p>Category: {product.categoryName}</p>
      <button 
        onClick={() => onAddToCart(product)}
        disabled={product.stockQuantity === 0}
        style={{
          backgroundColor: product.stockQuantity > 0 ? '#007bff' : '#6c757d',
          color: 'white',
          border: 'none',
          padding: '0.5rem 1rem',
          borderRadius: '4px',
          cursor: product.stockQuantity > 0 ? 'pointer' : 'not-allowed'
        }}
      >
        {product.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
      </button>
    </div>
  );
};

export default ProductCard;