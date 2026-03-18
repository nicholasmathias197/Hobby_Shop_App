// src/components/common/FilterSidebar.jsx
import React, { useState, useEffect } from 'react';
import { getCategoriesArray } from '../../services/categoryService';
import { getBrandsArray } from '../../services/brandService';

const FilterSidebar = ({ onFilterChange }) => {
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [mobileOpen, setMobileOpen] = useState(false);
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
    <>
      {/* Mobile toggle button */}
      <button
        className="filter-toggle-btn"
        onClick={() => setMobileOpen(!mobileOpen)}
        aria-label={mobileOpen ? 'Close filters' : 'Open filters'}
        aria-expanded={mobileOpen}
      >
        {mobileOpen ? '✕' : '☰'}
      </button>

      {/* Overlay backdrop */}
      {mobileOpen && (
        <div className="filter-overlay" onClick={() => setMobileOpen(false)} />
      )}

      {/* Sidebar */}
      <div className={`filter-sidebar${mobileOpen ? ' filter-sidebar--open' : ''}`} role="search" aria-label="Product filters">
      <h3 id="filter-heading">Filters</h3>
      
      <div className="filter-group">
        <label htmlFor="filter-search">Search</label>
        <input
          id="filter-search"
          type="text"
          name="search"
          value={filters.search}
          onChange={handleChange}
          placeholder="Search products..."
          className="filter-input"
        />
      </div>

      <div className="filter-group">
        <label htmlFor="filter-category">Category</label>
        <select
          id="filter-category"
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
        <label htmlFor="filter-brand">Brand</label>
        <select
          id="filter-brand"
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
        <label htmlFor="filter-min-price">Price Range</label>
        <div className="price-range">
          <input
            id="filter-min-price"
            type="number"
            name="minPrice"
            placeholder="Min"
            aria-label="Minimum price"
            value={filters.minPrice}
            onChange={handleChange}
            className="filter-input"
          />
          <input
            type="number"
            name="maxPrice"
            placeholder="Max"
            aria-label="Maximum price"
            value={filters.maxPrice}
            onChange={handleChange}
            className="filter-input"
          />
        </div>
      </div>

      <button
        onClick={handleClear}
        className="btn btn-secondary clear-filters-btn"
        aria-label="Clear all filters"
      >
        Clear Filters
      </button>
      </div>
    </>
  );
};

export default FilterSidebar;