import React from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../ui';

const ProductCard = ({ product, onAddToCart }) => {
  const handleAddToCart = () => {
    onAddToCart(product, 1);
  };

  return (
    <div style={{
      border: '1px solid rgba(0, 217, 255, 0.3)',
      borderRadius: '8px',
      overflow: 'hidden',
      backgroundColor: 'white',
      transition: 'transform 0.2s, box-shadow 0.2s',
      cursor: 'pointer',
      height: '100%',
      display: 'flex',
      flexDirection: 'column'
    }}
    onMouseEnter={(e) => {
      e.currentTarget.style.transform = 'translateY(-4px)';
      e.currentTarget.style.boxShadow = '0 4px 12px rgba(0,0,0,0.15)';
    }}
    onMouseLeave={(e) => {
      e.currentTarget.style.transform = 'translateY(0)';
      e.currentTarget.style.boxShadow = '0 2px 8px rgba(0,0,0,0.1)';
    }}
    >
      <Link to={`/product/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
        <div style={{ 
          height: '200px', 
          backgroundColor: 'rgba(0, 217, 255, 0.3)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          overflow: 'hidden'
        }}>
          {product.imageUrl ? (
            <img 
              src={product.imageUrl} 
              alt={product.name}
              style={{ 
                width: '100%', 
                height: '100%', 
                objectFit: 'cover' 
              }}
              onError={(e) => {
                e.target.onerror = null;
                e.target.src = 'https://via.placeholder.com/300x200?text=No+Image';
              }}
            />
          ) : (
            <div style={{
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: '#e9ecef',
              color: '#6c757d',
              fontSize: '1rem'
            }}>
              No Image Available
            </div>
          )}
        </div>
      </Link>
      
      <div style={{ padding: '1rem', flex: 1, display: 'flex', flexDirection: 'column' }}>
        <Link to={`/product/${product.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
          <h3 style={{ 
            margin: '0 0 0.5rem 0', 
            fontSize: '1.1rem',
            fontWeight: '600',
            color: '#333',
            lineHeight: '1.4'
          }}>
            {product.name}
          </h3>
        </Link>
        
        <p style={{ 
          color: '#666', 
          fontSize: '0.9rem', 
          marginBottom: '0.75rem',
          flex: 1
        }}>
          {product.brandName} • {product.categoryName}
        </p>
        
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginTop: 'auto'
        }}>
          <span style={{ 
            fontSize: '1.25rem', 
            fontWeight: 'bold', 
            color: '#007bff' 
          }}>
            ${product.price?.toFixed(2)}
          </span>
          
          <Button 
            variant="primary" 
            onClick={handleAddToCart}
            disabled={product.stockQuantity === 0}
            style={{ padding: '0.5rem 1rem' }}
          >
            {product.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
          </Button>
        </div>
        
        {product.stockQuantity < 10 && product.stockQuantity > 0 && (
          <p style={{ 
            marginTop: '0.5rem', 
            fontSize: '0.8rem', 
            color: '#dc3545',
            textAlign: 'right'
          }}>
            Only {product.stockQuantity} left in stock!
          </p>
        )}
      </div>
    </div>
  );
};

export default ProductCard;