// src/admin/AdminCategoriesPage/EditCategoryPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCategoryById, updateCategory } from '../../services/categoryService';
import { Button } from '../../components/ui';

const EditCategoryPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isActive, setIsActive] = useState(true);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    imageUrl: ''
  });

  useEffect(() => {
    loadCategory();
  }, [id]);

  const loadCategory = async () => {
    try {
      const data = await getCategoryById(id);
      console.log('Loaded category:', data); // Debug log
      setFormData({
        name: data.name || '',
        description: data.description || '',
        imageUrl: data.imageUrl || ''
      });
      // Check which property name the backend uses
      setIsActive(data.active !== undefined ? data.active : 
                  data.isActive !== undefined ? data.isActive : true);
    } catch (error) {
      console.error('Error loading category:', error);
      alert('Failed to load category');
      navigate('/admin/categories');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    
    try {
      // Send both property names to be safe
      const categoryData = {
        ...formData,
        active: isActive,
        isActive: isActive
      };
      console.log('Updating category:', categoryData);
      await updateCategory(id, categoryData);
      navigate('/admin/categories');
    } catch (error) {
      console.error('Error updating category:', error);
      alert('Failed to update category');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div>Loading category...</div>;

  return (
    <div className="edit-category-page">
      <div className="page-header">
        <h1>Edit Category</h1>
        <div className="edit-category-badge">✏️ Editing Category</div>
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
          />
        </div>

        <div className="form-group">
          <label>Description</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows="4"
          />
        </div>

        <div className="form-group">
          <label>Image URL</label>
          <input
            type="url"
            name="imageUrl"
            value={formData.imageUrl}
            onChange={handleChange}
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
          <Button type="submit" variant="primary" disabled={saving}>
            {saving ? 'Saving...' : 'Save Changes'}
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

export default EditCategoryPage;