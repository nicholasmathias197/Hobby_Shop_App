// src/admin/AdminCategoriesPage/AdminCategoriesPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getCategories, deleteCategory, updateCategory } from '../../services/categoryService';
import { Button } from '../../components/ui';

const AdminCategoriesPage = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [activeTab, setActiveTab] = useState('active'); // 'active' or 'inactive'

  useEffect(() => {
    loadCategories();
  }, [currentPage, activeTab]);

  const loadCategories = async () => {
    try {
      const data = await getCategories(currentPage, 10);
      console.log('Categories data:', data.content); // Debug log
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

  const handleToggleActive = async (category) => {
    try {
      // Check which property name the backend uses
      const currentActiveStatus = category.active !== undefined ? category.active : 
                                  category.isActive !== undefined ? category.isActive : true;
      
      const updatedCategory = {
        ...category,
        active: !currentActiveStatus,
        isActive: !currentActiveStatus // Set both to be safe
      };
      
      console.log('Toggling category:', category.id, 'from', currentActiveStatus, 'to', !currentActiveStatus);
      
      await updateCategory(category.id, updatedCategory);
      loadCategories();
    } catch (error) {
      console.error('Error toggling category status:', error);
      alert('Failed to update category status');
    }
  };

  // Helper function to check if category is active
  const isCategoryActive = (category) => {
    return category.active !== undefined ? category.active : 
           category.isActive !== undefined ? category.isActive : true;
  };

  // Filter categories based on active tab
  const displayCategories = categories.filter(category => 
    activeTab === 'active' ? isCategoryActive(category) : !isCategoryActive(category)
  );

  const activeCount = categories.filter(c => isCategoryActive(c)).length;
  const inactiveCount = categories.filter(c => !isCategoryActive(c)).length;

  if (loading) return <div>Loading categories...</div>;

  return (
    <div className="admin-categories-page">
      <div className="page-header">
        <h1>Category Management</h1>
        <Link to="/admin/categories/new">
          <Button variant="success">Create New Category</Button>
        </Link>
      </div>

      {/* Tabs with counts */}
      <div className="tabs">
        <button 
          className={`tab ${activeTab === 'active' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('active');
            setCurrentPage(0);
          }}
        >
          Active Categories 
          <span className="tab-count">{activeCount}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'inactive' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('inactive');
            setCurrentPage(0);
          }}
        >
          Inactive Categories 
          <span className="tab-count">{inactiveCount}</span>
        </button>
      </div>

      {/* Results info */}
      <div className="results-info">
        Showing {displayCategories.length} of {activeTab === 'active' ? activeCount : inactiveCount} categories
      </div>

      <div className="category-table-wrapper">
        <table className="category-table">
          <thead>
            <tr>
              <th className="col-id">ID</th>
              <th className="col-image">Image</th>
              <th className="col-name">Name</th>
              <th className="col-description">Description</th>
              <th className="col-status">Status</th>
              <th className="col-products">Products</th>
              <th className="col-created">Created</th>
              <th className="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            {displayCategories.map(category => {
              const isActive = isCategoryActive(category);
              return (
                <tr key={category.id}>
                  <td className="col-id">{category.id}</td>
                  <td className="col-image">
                    {category.imageUrl ? (
                      <img 
                        src={category.imageUrl} 
                        alt={category.name} 
                        className="category-image-thumb"
                      />
                    ) : (
                      <div className="no-image-thumb">
                        {category.name.charAt(0)}
                      </div>
                    )}
                  </td>
                  <td className="col-name">
                    <div className="category-name-cell" title={category.name}>
                      {category.name}
                    </div>
                  </td>
                  <td className="col-description">
                    <div className="category-description-cell" title={category.description}>
                      {category.description?.length > 50 
                        ? `${category.description.substring(0, 50)}...` 
                        : category.description || '-'}
                    </div>
                  </td>
                  <td className="col-status">
                    <div className="admin-toggle-container">
                      <label className="admin-toggle-switch">
                        <input
                          type="checkbox"
                          checked={isActive}
                          onChange={() => handleToggleActive(category)}
                          className="admin-toggle-input"
                        />
                        <span className="admin-toggle-slider"></span>
                      </label>
                      <span className="admin-toggle-label">
                        {isActive ? 'Active' : 'Inactive'}
                      </span>
                    </div>
                  </td>
                  <td className="col-products">
                    <span className="product-count-badge">{category.productCount || 0}</span>
                  </td>
                  <td className="col-created">
                    {new Date(category.createdAt).toLocaleDateString()}
                  </td>
                  <td className="col-actions">
                    <div className="action-buttons">
                      <Link to={`/admin/categories/edit/${category.id}`}>
                        <Button variant="primary" className="action-btn">Edit</Button>
                      </Link>
                      <Button 
                        variant="danger" 
                        onClick={() => handleDelete(category.id)}
                        className="action-btn"
                      >
                        Delete
                      </Button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

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
          <span>Page {currentPage + 1} of {totalPages}</span>
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