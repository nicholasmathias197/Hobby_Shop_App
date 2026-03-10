import React, { useState, useEffect } from 'react';
import { getCategories, getBrands } from '../../services/productService';

const FilterSidebar = ({ onFilterChange }) => {
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    categoryId: '',
    brandId: '',
    minPrice: '',
    maxPrice: '',
    search: ''
  });

  useEffect(() => {
    let isMounted = true; // Prevent state updates if component unmounts

    const loadFilters = async () => {
      try {
        const [categoriesData, brandsData] = await Promise.all([
          getCategories(),
          getBrands()
        ]);
        
        if (isMounted) {
          setCategories(categoriesData);
          setBrands(brandsData);
          setLoading(false);
        }
      } catch (error) {
        console.error('Error loading filters:', error);
        if (isMounted) {
          setLoading(false);
        }
      }
    };

    loadFilters();

    return () => {
      isMounted = false; // Cleanup function to prevent memory leaks
    };
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    const newFilters = { ...filters, [name]: value };
    setFilters(newFilters);
    onFilterChange(newFilters);
  };

  if (loading) {
    return <div>Loading filters...</div>;
  }

  return (
    <div style={{
      padding: '1rem',
      border: '1px solid #ddd',
      borderRadius: '4px'
    }}>
      <h3>Filters</h3>
      
      <div style={{ marginBottom: '1rem' }}>
        <label>Search:</label>
        <input
          type="text"
          name="search"
          value={filters.search}
          onChange={handleChange}
          style={{ width: '100%', padding: '0.5rem' }}
        />
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label>Category:</label>
        <select
          name="categoryId"
          value={filters.categoryId}
          onChange={handleChange}
          style={{ width: '100%', padding: '0.5rem' }}
        >
          <option value="">All Categories</option>
          {categories.map(category => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label>Brand:</label>
        <select
          name="brandId"
          value={filters.brandId}
          onChange={handleChange}
          style={{ width: '100%', padding: '0.5rem' }}
        >
          <option value="">All Brands</option>
          {brands.map(brand => (
            <option key={brand.id} value={brand.id}>
              {brand.name}
            </option>
          ))}
        </select>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label>Price Range:</label>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <input
            type="number"
            name="minPrice"
            placeholder="Min"
            value={filters.minPrice}
            onChange={handleChange}
            style={{ width: '50%', padding: '0.5rem' }}
          />
          <input
            type="number"
            name="maxPrice"
            placeholder="Max"
            value={filters.maxPrice}
            onChange={handleChange}
            style={{ width: '50%', padding: '0.5rem' }}
          />
        </div>
      </div>
    </div>
  );
};

export default FilterSidebar;