// src/components/common/ProductCard.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../../hooks/useCart';
import { Button } from '../ui';

const ProductCard = ({ product }) => {
  const { addToCart } = useCart();

  const handleAddToCart = (e) => {
    e.preventDefault(); // Prevent Link navigation when clicking button
    e.stopPropagation(); // Prevent event bubbling
    addToCart(product, 1);
  };

  // Generate star rating display
  const renderRating = () => {
    const rating = product.averageRating || 0;
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const stars = [];

    for (let i = 1; i <= 5; i++) {
      if (i <= fullStars) {
        stars.push(<span key={i} className="star filled">★</span>);
      } else if (i === fullStars + 1 && hasHalfStar) {
        stars.push(<span key={i} className="star half-filled">★</span>);
      } else {
        stars.push(<span key={i} className="star">★</span>);
      }
    }

    return (
      <div className="product-rating">
        <div className="stars">{stars}</div>
        {product.reviewCount > 0 && (
          <span className="rating-count">({product.reviewCount})</span>
        )}
      </div>
    );
  };

  return (
    <div className="product-card">
      <Link to={`/product/${product.id}`} className="product-card-link">
        <div className="product-image-container">
          {product.featured && (
            <span style={{
              position: 'absolute',
              top: '0.5rem',
              left: '0.5rem',
              backgroundColor: '#00d9ff',
              color: '#05345f',
              padding: '0.2rem 0.6rem',
              borderRadius: '4px',
              fontSize: '0.75rem',
              fontWeight: 'bold',
              zIndex: 3
            }}>FEATURED</span>
          )}
          {product.imageUrl ? (
            <div className="product-image-wrapper">
              <img 
                src={product.imageUrl} 
                alt={product.name}
                className="product-image"
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.src = 'https://via.placeholder.com/300x200?text=No+Image';
                }}
              />
            </div>
          ) : (
            <div className="product-image-placeholder">
              No Image Available
            </div>
          )}
        </div>
      </Link>
      
      <div className="product-info">
        <Link to={`/product/${product.id}`} className="product-title-link">
          <h3 className="product-title">
            {product.name}
          </h3>
        </Link>
        
        <p className="product-meta">
          {product.brandName} • {product.categoryName}
        </p>
        
        {/* Rating Stars */}
        {renderRating()}
        
        <div className="product-footer">
          <span className="product-price">
            ${product.price?.toFixed(2)}
          </span>
          
          <Button 
            variant="primary" 
            onClick={handleAddToCart}
            disabled={product.stockQuantity === 0}
            className="product-add-to-cart"
          >
            {product.stockQuantity > 0 ? 'Add to Cart' : 'Out of Stock'}
          </Button>
        </div>
        
        {product.stockQuantity < 10 && product.stockQuantity > 0 && (
          <p className="product-stock-warning">
            Only {product.stockQuantity} left in stock!
          </p>
        )}
      </div>
    </div>
  );
};

export default ProductCard;