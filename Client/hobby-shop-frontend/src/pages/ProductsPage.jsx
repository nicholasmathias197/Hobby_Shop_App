// src/pages/ProductsPage.jsx
import React, { useReducer, useEffect, useCallback, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { getAllProducts, filterProducts, getFeaturedProductsArray } from '../services/productService';
import ProductGrid from '../components/common/ProductGrid';
import FilterSidebar from '../components/common/FilterSidebar';
import ProductCardSkeleton from '../components/common/ProductCardSkeleton';
import { useCart } from '../hooks/useCart';
import { Button } from '../components/ui';

const initialState = {
  products: [],
  loading: true,
  error: null,
  currentPage: 0,
  totalPages: 0,
  totalProducts: 0,
  filters: {}
};

function productsReducer(state, action) {
  switch (action.type) {
    case 'FETCH_START':
      return { ...state, loading: true, error: null };
    case 'FETCH_SUCCESS':
      return { ...state, loading: false, products: action.payload.products,
        totalPages: action.payload.totalPages, totalProducts: action.payload.totalProducts };
    case 'FETCH_ERROR':
      return { ...state, loading: false, error: action.payload, products: [] };
    case 'SET_FILTERS':
      return { ...state, filters: action.payload, currentPage: 0 };
    case 'SET_PAGE':
      return { ...state, currentPage: action.payload };
    default:
      return state;
  }
}

const ProductsPage = () => {
  const location = useLocation();
  const [state, dispatch] = useReducer(productsReducer, initialState);
  const { products, loading, error, currentPage, totalPages, totalProducts, filters } = state;
  const { addToCart } = useCart();
  const [featuredProducts, setFeaturedProducts] = useState([]);

  useEffect(() => {
    getFeaturedProductsArray(0, 6).then(setFeaturedProducts);
  }, []);

  // Parse URL params for featured filter
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get('featured') === 'true') {
      dispatch({ type: 'SET_FILTERS', payload: { ...filters, featured: true } });
    }
  }, [location]);

  const loadProducts = useCallback(async () => {
    dispatch({ type: 'FETCH_START' });
    try {
      let response;
      const hasFilters = Object.keys(filters).some(key =>
        filters[key] !== '' && filters[key] !== null && filters[key] !== undefined
      );
      if (hasFilters) {
        response = await filterProducts(filters, currentPage, 12);
      } else {
        response = await getAllProducts(currentPage, 12);
      }
      dispatch({ type: 'FETCH_SUCCESS', payload: {
        products: response?.content || [],
        totalPages: response?.totalPages || 0,
        totalProducts: response?.totalElements || 0
      }});
    } catch (err) {
      dispatch({ type: 'FETCH_ERROR', payload: err.message || 'Failed to load products' });
    }
  }, [currentPage, filters]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  const handleFilterChange = useCallback((newFilters) => {
    dispatch({ type: 'SET_FILTERS', payload: newFilters });
  }, []);

  const handlePageChange = useCallback((newPage) => {
    dispatch({ type: 'SET_PAGE', payload: newPage });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);

  if (loading && products.length === 0) {
    return (
      <div className="products-layout" style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '2rem', padding: '2rem 0' }}>
        <div />
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '1.5rem' }}>
          {[...Array(8)].map((_, i) => <ProductCardSkeleton key={i} />)}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <h2 style={{ color: '#dc3545', marginBottom: '1rem' }}>Error Loading Products</h2>
        <p style={{ marginBottom: '2rem', color: '#666' }}>{error}</p>
        <Button variant="primary" onClick={() => window.location.reload()}>
          Try Again
        </Button>
      </div>
    );
  }

  return (
    <div className="products-layout" style={{ 
      display: 'grid', 
      gridTemplateColumns: '320px 1fr', 
      gap: '2rem',
      padding: '2rem 0'
    }}>
      {/* Sidebar with filters */}
      <div>
        <FilterSidebar onFilterChange={handleFilterChange} />
      </div>

      {/* Main content */}
      <div>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginBottom: '2rem'
        }}>
          <h1 style={{ margin: 0 }}>All Products</h1>
          <p style={{ color: '#666', margin: 0 }}>
            Showing {products.length} of {totalProducts} products
          </p>
        </div>

        {/* Featured section — only shown on page 0 with no filters */}
        {featuredProducts.length > 0 && currentPage === 0 && Object.keys(filters).every(k => !filters[k]) && (
          <section style={{ marginBottom: '2.5rem' }}>
            <h2 style={{ marginBottom: '1rem', color: '#00d9ff' }}>⭐ Featured Products</h2>
            <ProductGrid products={featuredProducts} onAddToCart={addToCart} />
            <hr style={{ margin: '2.5rem 0', borderColor: 'rgba(0, 217, 255, 0.2)' }} />
          </section>
        )}

        {/* Products grid */}
        {products.length > 0 ? (
          <ProductGrid products={products} onAddToCart={addToCart} />
        ) : (
          <div style={{ 
            textAlign: 'center', 
            padding: '3rem',
            backgroundColor: 'rgba(0, 217, 255, 0.3)',
            borderRadius: '8px'
          }}>
            <p style={{ fontSize: '1.1rem', marginBottom: '1rem' }}>No products found</p>
            <p style={{ color: '#666', marginBottom: '2rem' }}>
              Try adjusting your filters or check back later.
            </p>
            <Button variant="primary" onClick={() => {
              dispatch({ type: 'SET_FILTERS', payload: {} });
            }}>
              Clear Filters
            </Button>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div style={{
            marginTop: '3rem',
            display: 'flex',
            gap: '0.5rem',
            justifyContent: 'center',
            alignItems: 'center'
          }}>
            <Button
              variant="secondary"
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
            >
              Previous
            </Button>
            
            <div style={{ display: 'flex', gap: '0.25rem' }}>
              {[...Array(Math.min(5, totalPages))].map((_, i) => {
                let pageNum;
                if (totalPages <= 5) {
                  pageNum = i;
                } else if (currentPage < 3) {
                  pageNum = i;
                } else if (currentPage > totalPages - 3) {
                  pageNum = totalPages - 5 + i;
                } else {
                  pageNum = currentPage - 2 + i;
                }
                
                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    style={{
                      width: '40px',
                      height: '40px',
                      border: '1px solid #ddd',
                      borderRadius: '4px',
                      backgroundColor: currentPage === pageNum ? '#007bff' : 'white',
                      color: currentPage === pageNum ? 'white' : '#333',
                      cursor: 'pointer',
                      fontWeight: currentPage === pageNum ? 'bold' : 'normal'
                    }}
                  >
                    {pageNum + 1}
                  </button>
                );
              })}
            </div>
            
            <Button
              variant="secondary"
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
            >
              Next
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProductsPage;