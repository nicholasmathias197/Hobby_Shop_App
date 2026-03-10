import React, { useState, useEffect } from 'react';
import { getAllProducts } from '../services/productService';
import ProductGrid from '../components/common/ProductGrid';
import FilterSidebar from '../components/common/FilterSidebar';
import { useCart } from '../hooks/useCart';

const ProductsPage = () => {
  const [products, setProducts] = useState([]);
  const [filteredProducts, setFilteredProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToCart } = useCart();

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      const data = await getAllProducts();
      setProducts(data);
      setFilteredProducts(data);
    } catch (error) {
      console.error('Error loading products:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = (filters) => {
    let filtered = [...products];

    if (filters.search) {
      filtered = filtered.filter(p => 
        p.name.toLowerCase().includes(filters.search.toLowerCase()) ||
        p.description.toLowerCase().includes(filters.search.toLowerCase())
      );
    }

    if (filters.categoryId) {
      filtered = filtered.filter(p => p.categoryId === parseInt(filters.categoryId));
    }

    if (filters.brandId) {
      filtered = filtered.filter(p => p.brandId === parseInt(filters.brandId));
    }

    if (filters.minPrice) {
      filtered = filtered.filter(p => p.price >= parseFloat(filters.minPrice));
    }

    if (filters.maxPrice) {
      filtered = filtered.filter(p => p.price <= parseFloat(filters.maxPrice));
    }

    setFilteredProducts(filtered);
  };

  if (loading) return <div>Loading products...</div>;

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '250px 1fr', gap: '2rem' }}>
      <FilterSidebar onFilterChange={handleFilterChange} />
      <div>
        <h1 style={{ marginBottom: '1rem' }}>All Products</h1>
        <ProductGrid products={filteredProducts} onAddToCart={addToCart} />
      </div>
    </div>
  );
};

export default ProductsPage;