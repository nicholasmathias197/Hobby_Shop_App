// src/admin/AdminBrandsPage/EditBrandPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getBrandById, updateBrand } from '../../services/brandService';
import { Button } from '../../components/ui';

const EditBrandPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isActive, setIsActive] = useState(true);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    logoUrl: '',
    website: ''
  });

  useEffect(() => {
    loadBrand();
  }, [id]);

  const loadBrand = async () => {
    try {
      const data = await getBrandById(id);
      setFormData({
        name: data.name || '',
        description: data.description || '',
        logoUrl: data.logoUrl || '',
        website: data.website || ''
      });
      setIsActive(data.isActive !== false);
    } catch (error) {
      console.error('Error loading brand:', error);
      alert('Failed to load brand');
      navigate('/admin/brands');
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
      const brandData = {
        ...formData,
        isActive
      };
      await updateBrand(id, brandData);
      navigate('/admin/brands');
    } catch (error) {
      console.error('Error updating brand:', error);
      alert('Failed to update brand');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div>Loading brand...</div>;

  return (
    <div className="edit-brand-page">
      <div className="page-header">
        <h1>Edit Brand</h1>
        <div className="edit-brand-badge">✏️ Editing Brand</div>
      </div>
      
      <form onSubmit={handleSubmit} className="brand-form">
        <div className="form-group">
          <label>Brand Name *</label>
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
          <label>Logo URL</label>
          <input
            type="url"
            name="logoUrl"
            value={formData.logoUrl}
            onChange={handleChange}
          />
          {formData.logoUrl && (
            <div className="logo-preview">
              <img 
                src={formData.logoUrl} 
                alt="Logo preview"
              />
            </div>
          )}
        </div>

        <div className="form-group">
          <label>Website</label>
          <input
            type="url"
            name="website"
            value={formData.website}
            onChange={handleChange}
          />
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
            onClick={() => navigate('/admin/brands')}
          >
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
};

export default EditBrandPage;