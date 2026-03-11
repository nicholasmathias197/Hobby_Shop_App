// src/admin/AdminCategoriesPage/AdminCategoriesPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getCategories, deleteCategory } from '../../services/categoryService';
import { Button } from '../../components/ui';

const AdminCategoriesPage = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadCategories();
  }, [currentPage]);

  const loadCategories = async () => {
    try {
      const data = await getCategories(currentPage, 10);
      setCategories(data.content || []);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Error loading categories:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this category?')) {
      try {
        await deleteCategory(id);
        loadCategories();
      } catch (error) {
        console.error('Error deleting category:', error);
        alert('Failed to delete category');
      }
    }
  };

  if (loading) return <div>Loading categories...</div>;

  return (
    <div>
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '2rem' 
      }}>
        <h1>Category Management</h1>
        <Link to="/admin/categories/new">
          <Button variant="success">Create New Category</Button>
        </Link>
      </div>

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ backgroundColor: '#f8f9fa' }}>
            <th style={{ padding: '1rem', textAlign: 'left' }}>ID</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Image</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Name</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Description</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Status</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Products</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Created</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {categories.map(category => (
            <tr key={category.id} style={{ borderBottom: '1px solid #ddd' }}>
              <td style={{ padding: '1rem' }}>{category.id}</td>
              <td style={{ padding: '1rem' }}>
                {category.imageUrl ? (
                  <img 
                    src={category.imageUrl} 
                    alt={category.name} 
                    style={{ 
                      width: '50px', 
                      height: '50px', 
                      objectFit: 'cover',
                      borderRadius: '4px'
                    }} 
                  />
                ) : (
                  <div style={{
                    width: '50px',
                    height: '50px',
                    backgroundColor: '#e9ecef',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: '4px',
                    fontSize: '1.5rem',
                    color: '#adb5bd'
                  }}>
                    {category.name.charAt(0)}
                  </div>
                )}
              </td>
              <td style={{ padding: '1rem', fontWeight: 'bold' }}>{category.name}</td>
              <td style={{ padding: '1rem' }}>
                {category.description?.length > 50 
                  ? `${category.description.substring(0, 50)}...` 
                  : category.description || '-'}
              </td>
              <td style={{ padding: '1rem' }}>
                <span style={{
                  padding: '0.25rem 0.5rem',
                  backgroundColor: category.active ? '#28a745' : '#dc3545',
                  color: 'white',
                  borderRadius: '4px',
                  fontSize: '0.875rem'
                }}>
                  {category.active ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td style={{ padding: '1rem' }}>{category.productCount || 0}</td>
              <td style={{ padding: '1rem' }}>
                {new Date(category.createdAt).toLocaleDateString()}
              </td>
              <td style={{ padding: '1rem' }}>
                <Link to={`/admin/categories/edit/${category.id}`}>
                  <Button variant="primary" style={{ marginRight: '0.5rem' }}>
                    Edit
                  </Button>
                </Link>
                <Button 
                  variant="danger" 
                  onClick={() => handleDelete(category.id)}
                >
                  Delete
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

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

export default AdminCategoriesPage;