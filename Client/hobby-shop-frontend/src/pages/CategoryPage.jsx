import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getCategoryById } from '../services/categoryService';
import { getProductsByCategory } from '../services/productService';
import ProductGrid from '../components/common/ProductGrid';
import FilterSidebar from '../components/common/FilterSidebar';
import { useCart } from '../hooks/useCart';
import { Button } from '../components/ui';

const CategoryPage = () => {
  const { categoryId } = useParams();
  const [category, setCategory] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalProducts, setTotalProducts] = useState(0);
  const { addToCart } = useCart();

  useEffect(() => {
    loadCategoryAndProducts();
  }, [categoryId, currentPage]);

  const loadCategoryAndProducts = async () => {
    setLoading(true);
    try {
      // Load category details and products in parallel
      const [categoryData, productsData] = await Promise.all([
        getCategoryById(categoryId),
        getProductsByCategory(categoryId, currentPage, 12)
      ]);
      
      setCategory(categoryData);
      setProducts(productsData.content);
      setTotalPages(productsData.totalPages);
      setTotalProducts(productsData.totalElements);
      setError(null);
    } catch (err) {
      setError(err.message);
      console.error('Error loading category page:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = async (filters) => {
    setLoading(true);
    try {
      // Apply filters to category products
      const filteredData = await getProductsByCategory(categoryId, 0, 12, filters);
      setProducts(filteredData.content);
      setTotalPages(filteredData.totalPages);
      setTotalProducts(filteredData.totalElements);
      setCurrentPage(0);
    } catch (err) {
      console.error('Error filtering products:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading && !category) return <div>Loading category...</div>;
  if (error) return (
    <div style={{ textAlign: 'center', padding: '3rem' }}>
      <h2 style={{ color: '#dc3545' }}>Error Loading Category</h2>
      <p>{error}</p>
      <Link to="/products">
        <Button variant="primary">Browse All Products</Button>
      </Link>
    </div>
  );
  if (!category) return <div>Category not found</div>;

  return (
    <div>
      {/* Category Header */}
      <div style={{
        backgroundColor: '#f8f9fa',
        padding: '2rem',
        borderRadius: '4px',
        marginBottom: '2rem',
        textAlign: 'center'
      }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>{category.name}</h1>
        {category.description && (
          <p style={{ fontSize: '1.1rem', color: '#666', maxWidth: '800px', margin: '0 auto' }}>
            {category.description}
          </p>
        )}
        {category.imageUrl && (
          <img 
            src={category.imageUrl} 
            alt={category.name}
            style={{ 
              maxWidth: '100%', 
              maxHeight: '300px', 
              objectFit: 'cover',
              borderRadius: '4px',
              marginTop: '1rem'
            }}
          />
        )}
        <p style={{ marginTop: '1rem', fontWeight: 'bold' }}>
          {totalProducts} products in this category
        </p>
      </div>

      {/* Products Section */}
      <div style={{ display: 'grid', gridTemplateColumns: '250px 1fr', gap: '2rem' }}>
        <FilterSidebar onFilterChange={handleFilterChange} />
        
        <div>
          <h2 style={{ marginBottom: '1rem' }}>Products in {category.name}</h2>
          
          {loading ? (
            <div>Loading products...</div>
          ) : products.length > 0 ? (
            <>
              <ProductGrid products={products} onAddToCart={addToCart} />
              
              {/* Pagination */}
              {totalPages > 1 && (
                <div style={{
                  marginTop: '2rem',
                  display: 'flex',
                  gap: '0.5rem',
                  justifyContent: 'center',
                  alignItems: 'center'
                }}>
                  <Button
                    variant="secondary"
                    onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                    disabled={currentPage === 0}
                  >
                    Previous
                  </Button>
                  
                  <span style={{ margin: '0 1rem' }}>
                    Page {currentPage + 1} of {totalPages}
                  </span>
                  
                  <Button
                    variant="secondary"
                    onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                    disabled={currentPage === totalPages - 1}
                  >
                    Next
                  </Button>
                </div>
              )}
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: '3rem' }}>
              <p>No products found in this category.</p>
              <Link to="/products">
                <Button variant="primary" style={{ marginTop: '1rem' }}>
                  Browse All Products
                </Button>
              </Link>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CategoryPage;