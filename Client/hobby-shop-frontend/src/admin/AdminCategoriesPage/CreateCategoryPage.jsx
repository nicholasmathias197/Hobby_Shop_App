// src/admin/AdminCategoriesPage/CreateCategoryPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createCategory } from '../../services/categoryService';
import { Button } from '../../components/ui';

const CreateCategoryPage = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [isActive, setIsActive] = useState(true);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    imageUrl: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      // Send both property names to be safe
      const categoryData = {
        ...formData,
        active: isActive,
        isActive: isActive
      };
      console.log('Creating category:', categoryData);
      await createCategory(categoryData);
      navigate('/admin/categories');
    } catch (error) {
      console.error('Error creating category:', error);
      alert('Failed to create category');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-category-page">
      <div className="page-header">
        <h1>Create New Category</h1>
        <div className="new-category-badge">✨ New Category</div>
      </div>
      
      <form onSubmit={handleSubmit} className="category-form">
        <div className="form-group">
          <label>Category Name *</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
            placeholder="e.g., Action Figures, Gundam Models, Paints"
          />
        </div>

        <div className="form-group">
          <label>Description</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows="4"
            placeholder="Describe what products belong in this category..."
          />
        </div>

        <div className="form-group">
          <label>Image URL</label>
          <input
            type="url"
            name="imageUrl"
            value={formData.imageUrl}
            onChange={handleChange}
            placeholder="https://example.com/category-image.jpg"
          />
          {formData.imageUrl && (
            <div className="image-preview">
              <img 
                src={formData.imageUrl} 
                alt="Category preview"
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.src = 'https://via.placeholder.com/200?text=Invalid+Image';
                }}
              />
            </div>
          )}
        </div>

        {/* Active Status Toggle */}
        <div className="form-toggle-group">
          <label className="toggle-label">
            <span className="toggle-text">Active Status:</span>
            <div className="toggle-container">
              <input
                type="checkbox"
                checked={isActive}
                onChange={(e) => setIsActive(e.target.checked)}
                className="toggle-checkbox"
              />
              <span className="toggle-switch-label">
                <span className="toggle-button"></span>
              </span>
            </div>
          </label>
          <span className="toggle-status-text">{isActive ? 'Active' : 'Inactive'}</span>
        </div>

        <div className="form-actions">
          <Button type="submit" variant="primary" disabled={loading}>
            {loading ? 'Creating...' : 'Create Category'}
          </Button>
          <Button 
            type="button" 
            variant="secondary" 
            onClick={() => navigate('/admin/categories')}
          >
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
};

export default CreateCategoryPage;