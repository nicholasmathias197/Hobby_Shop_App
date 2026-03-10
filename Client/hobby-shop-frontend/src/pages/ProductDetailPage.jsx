import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getProductById } from '../services/productService';
import { useCart } from '../hooks/useCart';
import { Button } from '../components/ui';

const ProductDetailPage = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
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

  if (loading) return <div>Loading product details...</div>;
  if (!product) return <div>Product not found</div>;

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
      <div>
        {product.imageUrl ? (
          <img 
            src={product.imageUrl} 
            alt={product.name}
            style={{ width: '100%', borderRadius: '4px' }}
          />
        ) : (
          <div style={{
            width: '100%',
            height: '400px',
            backgroundColor: '#f8f9fa',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderRadius: '4px'
          }}>
            No Image Available
          </div>
        )}
      </div>
      
      <div>
        <h1>{product.name}</h1>
        <p style={{ fontSize: '1.2rem', color: '#666', marginBottom: '1rem' }}>
          {product.brandName} - {product.categoryName}
        </p>
        <p style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1rem' }}>
          ${product.price}
        </p>
        <p style={{ marginBottom: '1rem' }}>{product.description}</p>
        
        {product.scale && (
          <p><strong>Scale:</strong> {product.scale}</p>
        )}
        <p><strong>SKU:</strong> {product.sku}</p>
        <p><strong>Stock:</strong> {product.stockQuantity}</p>
        
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
              marginRight: '1rem'
            }}
          />
          <Button 
            onClick={handleAddToCart}
            disabled={product.stockQuantity === 0}
          >
            Add to Cart
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ProductDetailPage;