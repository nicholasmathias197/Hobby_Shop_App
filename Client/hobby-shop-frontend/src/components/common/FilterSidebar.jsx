// src/components/common/FilterSidebar.jsx
import React, { useState, useEffect } from 'react';
import { getCategoriesArray } from '../../services/categoryService';
import { getBrandsArray } from '../../services/brandService';

const FilterSidebar = ({ onFilterChange }) => {
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [filters, setFilters] = useState({
    categoryId: '',
    brandId: '',
    minPrice: '',
    maxPrice: '',
    search: ''
  });

  useEffect(() => {
    const loadFilters = async () => {
      try {
        const [categoriesData, brandsData] = await Promise.all([
          getCategoriesArray(),
          getBrandsArray()
        ]);
        setCategories(categoriesData || []);
        setBrands(brandsData || []);
      } catch (error) {
        console.error('Error loading filters:', error);
      }
    };
    loadFilters();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    const newFilters = { ...filters, [name]: value };
    setFilters(newFilters);
    onFilterChange?.(newFilters);
  };

  const handleClear = () => {
    const cleared = {
      categoryId: '',
      brandId: '',
      minPrice: '',
      maxPrice: '',
      search: ''
    };
    setFilters(cleared);
    onFilterChange?.(cleared);
  };

  return (
    <div className="filter-sidebar">
      <h3>Filters</h3>
      
      <div className="filter-group">
        <label>Search</label>
        <input
          type="text"
          name="search"
          value={filters.search}
          onChange={handleChange}
          placeholder="Search products..."
          className="filter-input"
        />
      </div>

      <div className="filter-group">
        <label>Category</label>
        <select
          name="categoryId"
          value={filters.categoryId}
          onChange={handleChange}
          className="filter-select"
        >
          <option value="">All Categories</option>
          {categories.map(category => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-group">
        <label>Brand</label>
        <select
          name="brandId"
          value={filters.brandId}
          onChange={handleChange}
          className="filter-select"
        >
          <option value="">All Brands</option>
          {brands.map(brand => (
            <option key={brand.id} value={brand.id}>
              {brand.name}
            </option>
          ))}
        </select>
      </div>

      <div className="filter-group">
        <label>Price Range</label>
        <div className="price-range">
          <input
            type="number"
            name="minPrice"
            placeholder="Min"
            value={filters.minPrice}
            onChange={handleChange}
            className="filter-input"
          />
          <input
            type="number"
            name="maxPrice"
            placeholder="Max"
            value={filters.maxPrice}
            onChange={handleChange}
            className="filter-input"
          />
        </div>
      </div>

      <button
        onClick={handleClear}
        className="btn btn-secondary clear-filters-btn"
      >
        Clear Filters
      </button>
    </div>
  );
};

export default FilterSidebar;