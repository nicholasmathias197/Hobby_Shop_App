import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getBrandById } from '../services/brandService';
import { getProductsByBrand } from '../services/productService';
import ProductGrid from '../components/common/ProductGrid';
import FilterSidebar from '../components/common/FilterSidebar';
import { useCart } from '../hooks/useCart';
import { Button } from '../components/ui';

const BrandPage = () => {
  const { brandId } = useParams();
  const [brand, setBrand] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalProducts, setTotalProducts] = useState(0);
  const { addToCart } = useCart();

  useEffect(() => {
    loadBrandAndProducts();
  }, [brandId, currentPage]);

  const loadBrandAndProducts = async () => {
    setLoading(true);
    try {
      const [brandData, productsData] = await Promise.all([
        getBrandById(brandId),
        getProductsByBrand(brandId, currentPage, 12)
      ]);
      
      setBrand(brandData);
      setProducts(productsData.content);
      setTotalPages(productsData.totalPages);
      setTotalProducts(productsData.totalElements);
      setError(null);
    } catch (err) {
      setError(err.message);
      console.error('Error loading brand page:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleFilterChange = async (filters) => {
    setLoading(true);
    try {
      const filteredData = await getProductsByBrand(brandId, 0, 12, filters);
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

  if (loading && !brand) return <div>Loading brand...</div>;
  if (error) return (
    <div style={{ textAlign: 'center', padding: '3rem' }}>
      <h2 style={{ color: '#dc3545' }}>Error Loading Brand</h2>
      <p>{error}</p>
      <Link to="/products">
        <Button variant="primary">Browse All Products</Button>
      </Link>
    </div>
  );
  if (!brand) return <div>Brand not found</div>;

  return (
    <div>
      {/* Brand Header */}
      <div style={{
        backgroundColor: '#f8f9fa',
        padding: '2rem',
        borderRadius: '4px',
        marginBottom: '2rem',
        textAlign: 'center'
      }}>
        {brand.logoUrl && (
          <img 
            src={brand.logoUrl} 
            alt={brand.name}
            style={{ 
              maxWidth: '200px', 
              maxHeight: '100px', 
              objectFit: 'contain',
              marginBottom: '1rem'
            }}
          />
        )}
        <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>{brand.name}</h1>
        {brand.description && (
          <p style={{ fontSize: '1.1rem', color: '#666', maxWidth: '800px', margin: '0 auto' }}>
            {brand.description}
          </p>
        )}
        {brand.website && (
          <p style={{ marginTop: '1rem' }}>
            <a href={brand.website} target="_blank" rel="noopener noreferrer" style={{ color: '#007bff' }}>
              Visit Website
            </a>
          </p>
        )}
        <p style={{ marginTop: '1rem', fontWeight: 'bold' }}>
          {totalProducts} products from {brand.name}
        </p>
      </div>

      {/* Products Section */}
      <div style={{ display: 'grid', gridTemplateColumns: '250px 1fr', gap: '2rem' }}>
        <FilterSidebar onFilterChange={handleFilterChange} />
        
        <div>
          <h2 style={{ marginBottom: '1rem' }}>Products by {brand.name}</h2>
          
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
              <p>No products found for this brand.</p>
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

export default BrandPage;