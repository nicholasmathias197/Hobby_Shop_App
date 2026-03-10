import { useState, useEffect } from 'react';
import { getAllProducts, getProductById, getProductsByCategory, getProductsByBrand } from '../services/productService';

export const useProducts = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchProducts = async (filters = {}) => {
    setLoading(true);
    setError(null);
    try {
      let data;
      if (filters.categoryId) {
        data = await getProductsByCategory(filters.categoryId);
      } else if (filters.brandId) {
        data = await getProductsByBrand(filters.brandId);
      } else {
        data = await getAllProducts();
      }
      
      // Apply additional filters
      if (filters.search) {
        data = data.filter(p => 
          p.name.toLowerCase().includes(filters.search.toLowerCase()) ||
          p.description.toLowerCase().includes(filters.search.toLowerCase())
        );
      }
      
      if (filters.minPrice) {
        data = data.filter(p => p.price >= filters.minPrice);
      }
      
      if (filters.maxPrice) {
        data = data.filter(p => p.price <= filters.maxPrice);
      }
      
      setProducts(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const getProduct = async (id) => {
    setLoading(true);
    setError(null);
    try {
      return await getProductById(id);
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    products,
    loading,
    error,
    fetchProducts,
    getProduct
  };
};