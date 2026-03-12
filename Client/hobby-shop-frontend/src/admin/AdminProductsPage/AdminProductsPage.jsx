// src/admin/AdminProductsPage/AdminProductsPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  getAllProducts, 
  getAllProductsIncludingInactive,
  getActiveProductsCount,
  getInactiveProductsCount,
  getFeaturedProductsCount,
  deleteProduct,
  restoreProduct,
  updateProduct 
} from '../../services/productService';
import ProductTable from './ProductTable';
import { Button } from '../../components/ui';

const AdminProductsPage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  // Real counts from database
  const [activeCount, setActiveCount] = useState(0);
  const [inactiveCount, setInactiveCount] = useState(0);
  const [featuredCount, setFeaturedCount] = useState(0);
  
  const [activeTab, setActiveTab] = useState('active'); // 'active' or 'inactive'

  // Load real counts on component mount and after operations
  useEffect(() => {
    loadCounts();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [currentPage, activeTab]);

  const loadCounts = async () => {
    try {
      const [active, inactive, featured] = await Promise.all([
        getActiveProductsCount(),
        getInactiveProductsCount(),
        getFeaturedProductsCount()
      ]);
      
      setActiveCount(active);
      setInactiveCount(inactive);
      setFeaturedCount(featured);
      
      console.log('📊 Real counts from DB:', { active, inactive, featured });
    } catch (error) {
      console.error('Error loading counts:', error);
    }
  };

  const loadProducts = async () => {
    setLoading(true);
    try {
      let response;
      if (activeTab === 'active') {
        response = await getAllProducts(currentPage, 10);
        setProducts(response?.content || []);
      } else {
        // For inactive tab, we need to get inactive products
        response = await getAllProductsIncludingInactive(currentPage, 10);
        const allProducts = response?.content || [];
        const inactiveProducts = allProducts.filter(p => !p.isActive);
        setProducts(inactiveProducts);
      }
      
      setTotalPages(response?.totalPages || 0);
    } catch (error) {
      console.error('Error loading products:', error);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this product?')) {
      try {
        await deleteProduct(id);
        await loadCounts(); // Reload counts first
        await loadProducts(); // Then reload products
      } catch (error) {
        console.error('Error deleting product:', error);
        alert('Failed to delete product');
      }
    }
  };

  const handleRestore = async (id) => {
    if (window.confirm('Are you sure you want to restore this product?')) {
      try {
        await restoreProduct(id);
        await loadCounts(); // Reload counts first
        await loadProducts(); // Then reload products
      } catch (error) {
        console.error('Error restoring product:', error);
        alert('Failed to restore product');
      }
    }
  };

  const handleToggleFeatured = async (product) => {
    try {
      const updatedProduct = {
        ...product,
        isFeatured: !product.isFeatured
      };
      await updateProduct(product.id, updatedProduct);
      await loadCounts(); // Reload counts
      await loadProducts(); // Reload products
    } catch (error) {
      console.error('Error toggling featured status:', error);
      alert('Failed to update featured status');
    }
  };

  const handleToggleActive = async (product) => {
    if (product.isActive) {
      // If active, deactivate it (soft delete)
      handleDelete(product.id);
    } else {
      // If inactive, restore it
      handleRestore(product.id);
    }
  };

  if (loading) return <div>Loading products...</div>;

  // Determine which products to display based on tab
  const displayProducts = activeTab === 'active' 
    ? products.filter(p => p.isActive) 
    : products.filter(p => !p.isActive);

  return (
    <div className="admin-products-page">
      <div className="page-header">
        <h1>Product Management</h1>
        <Link to="/admin/products/new">
          <Button variant="success">Add New Product</Button>
        </Link>
      </div>

      {/* Real Counts Summary Cards */}
      <div className="status-summary">
        <div className="summary-card">
          <span className="summary-label">Total Active Products</span>
          <span className="summary-value">{activeCount}</span>
        </div>
        <div className="summary-card">
          <span className="summary-label">Total Inactive Products</span>
          <span className="summary-value">{inactiveCount}</span>
        </div>
        <div className="summary-card">
          <span className="summary-label">Featured Products</span>
          <span className="summary-value">{featuredCount}</span>
        </div>
      </div>

      {/* Tabs with real counts */}
      <div className="tabs">
        <button 
          className={`tab ${activeTab === 'active' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('active');
            setCurrentPage(0);
          }}
        >
          Active Products 
          <span className="tab-count">{activeCount}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'inactive' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('inactive');
            setCurrentPage(0);
          }}
        >
          Inactive/Deleted Products 
          <span className="tab-count">{inactiveCount}</span>
        </button>
      </div>

      {/* Results info with real counts */}
      <div className="results-info">
        Showing {displayProducts.length} of {activeTab === 'active' ? activeCount : inactiveCount} {activeTab === 'active' ? 'active' : 'inactive'} products
      </div>

      {/* Products Table */}
      <ProductTable 
        products={displayProducts}
        onEdit={(product) => window.location.href = `/admin/products/edit/${product.id}`}
        onDelete={handleDelete}
        onRestore={handleRestore}
        onToggleFeatured={handleToggleFeatured}
        onToggleActive={handleToggleActive}
        showInactive={activeTab === 'inactive'}
      />

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <Button
            variant="secondary"
            onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
            disabled={currentPage === 0}
          >
            Previous
          </Button>
          <div className="page-info">
            <span>Page {currentPage + 1} of {totalPages}</span>
          </div>
          <Button
            variant="secondary"
            onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={currentPage === totalPages - 1}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
};

export default AdminProductsPage;