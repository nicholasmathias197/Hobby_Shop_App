// src/admin/AdminProductsPage/AdminProductsPage.jsx
import React, { useState, useEffect } from 'react';
import { getAllProducts, deleteProduct } from '../../services/productService';
import ProductTable from './ProductTable';
import { Button } from '../../components/ui';

const AdminProductsPage = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadProducts();
  }, [currentPage]);

  const loadProducts = async () => {
    try {
      const response = await getAllProducts(currentPage, 10);
      console.log('Products response:', response);
      
      // Ensure we're setting an array
      setProducts(response?.content || []);
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
        loadProducts(); // Refresh the list
      } catch (error) {
        console.error('Error deleting product:', error);
        alert('Failed to delete product');
      }
    }
  };

  const handleEdit = (product) => {
    // Navigate to edit page
    window.location.href = `/admin/products/edit/${product.id}`;
  };

  if (loading) return <div>Loading products...</div>;

  return (
    <div>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '2rem' 
      }}>
        <h1>Product Management</h1>
        <Button 
          variant="success" 
          onClick={() => window.location.href = '/admin/products/new'}
        >
          Add New Product
        </Button>
      </div>

      <ProductTable 
        products={products} 
        onEdit={handleEdit} 
        onDelete={handleDelete} 
      />

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
          <span>
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
    </div>
  );
};

export default AdminProductsPage;