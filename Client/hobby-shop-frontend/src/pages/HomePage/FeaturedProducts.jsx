import React, { useState, useEffect } from 'react';
import { getFeaturedProducts } from '../../services/productService';
import ProductGrid from '../../components/common/ProductGrid';
import { useCart } from '../../hooks/useCart';

const FeaturedProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCart();

  useEffect(() => {
    loadFeaturedProducts();
  }, []);

  const loadFeaturedProducts = async () => {
    try {
      const data = await getFeaturedProducts();
      setProducts(data);
    } catch (error) {
      console.error('Error loading featured products:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading featured products...</div>;

  return (
    <div>
      <h2 style={{ marginBottom: '1rem' }}>Featured Products</h2>
      <ProductGrid products={products} onAddToCart={addToCart} />
    </div>
  );
};

export default FeaturedProducts;