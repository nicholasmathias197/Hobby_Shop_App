// src/pages/ProductDetailPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getProductById } from '../services/productService';
import { useCart } from '../hooks/useCart';
import { Button } from '../components/ui';
import ProductReviews from '../components/common/ProductReviews';

const ProductDetailPage = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [rating, setRating] = useState({ average: 0, total: 0 });
  const { addToCart } = useCart();

  useEffect(() => {
    loadProduct();
  }, [id]);

  const loadProduct = async () => {
    try {
      const data = await getProductById(id);
      setProduct(data);
    } catch (error) {
      console.error('Error loading product:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddToCart = () => {
    addToCart(product, quantity);
  };

  const handleRatingUpdate = (newRating) => {
    setRating(newRating);
  };

  if (loading) return <div>Loading product details...</div>;
  if (!product) return <div>Product not found</div>;

  return (
    <div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '3rem' }}>
        {/* Product Image */}
        <div>
          {product.imageUrl ? (
            <img 
              src={product.imageUrl} 
              alt={product.name}
              style={{ width: '100%', borderRadius: '12px', border: '2px solid rgba(0,217,255,0.3)' }}
            />
          ) : (
            <div style={{
              width: '100%',
              height: '400px',
              backgroundColor: 'rgba(0,0,0,0.6)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: '12px',
              border: '2px solid rgba(0,217,255,0.3)'
            }}>
              No Image Available
            </div>
          )}
        </div>
        
        {/* Product Details */}
        <div>
          {/* Brand Logo */}
          {product.brandLogoUrl && (
            <div style={{
              width: '160px',
              height: '60px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '1rem',
              background: 'rgba(255,255,255,0.08)',
              border: '1px solid rgba(0,217,255,0.2)',
              borderRadius: '8px',
              padding: '0.5rem'
            }}>
              <img
                src={product.brandLogoUrl}
                alt={product.brandName}
                style={{
                  maxWidth: '100%',
                  maxHeight: '100%',
                  objectFit: 'contain'
                }}
              />
            </div>
          )}

          <h1>{product.name}</h1>
          
          {/* Rating Summary */}
          {rating.total > 0 && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center' }}>
                {[1,2,3,4,5].map(star => (
                  <span key={star} style={{ color: star <= Math.round(rating.average) ? '#ffc107' : '#e4e5e9' }}>
                    ★
                  </span>
                ))}
              </div>
              <span style={{ color: '#00d9ff' }}>{rating.average.toFixed(1)} out of 5</span>
              <span style={{ color: '#e0e1dd' }}>({rating.total} reviews)</span>
            </div>
          )}
          
          <p style={{ fontSize: '1.2rem', color: '#e0e1dd', marginBottom: '1rem' }}>
            {product.brandName} - {product.categoryName}
          </p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#00d9ff', marginBottom: '1rem' }}>
            ${product.price}
          </p>
          <p style={{ marginBottom: '1rem' }}>{product.description}</p>
          
          {product.scale && (
            <p><strong style={{ color: '#00d9ff' }}>Scale:</strong> {product.scale}</p>
          )}
          <p><strong style={{ color: '#00d9ff' }}>SKU:</strong> {product.sku}</p>
          <p><strong style={{ color: '#00d9ff' }}>Stock:</strong> {product.stockQuantity}</p>
          
          <div style={{ marginTop: '2rem' }}>
            <label style={{ marginRight: '1rem' }}>Quantity:</label>
            <input
              type="number"
              min="1"
              max={product.stockQuantity}
              value={quantity}
              onChange={(e) => setQuantity(parseInt(e.target.value))}
              style={{
                width: '60px',
                padding: '0.5rem',
                marginRight: '1rem',
                background: 'rgba(0,0,0,0.6)',
                border: '2px solid rgba(0,217,255,0.3)',
                borderRadius: '4px',
                color: '#e0e1dd'
              }}
            />
            <Button 
              onClick={handleAddToCart}
              disabled={product.stockQuantity === 0}
              variant="primary"
            >
              Add to Cart
            </Button>
          </div>
        </div>
      </div>

      {/* Product Reviews Section */}
      <ProductReviews productId={product.id} onRatingUpdate={handleRatingUpdate} />
    </div>
  );
};

export default ProductDetailPage;