// src/admin/AdminBrandsPage/AdminBrandsPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getBrands, deleteBrand, updateBrand } from '../../services/brandService';
import { Button } from '../../components/ui';

const AdminBrandsPage = () => {
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [activeTab, setActiveTab] = useState('active'); // 'active' or 'inactive'

  useEffect(() => {
    loadBrands();
  }, [currentPage, activeTab]);

  const loadBrands = async () => {
    try {
      const data = await getBrands(currentPage, 10);
      // Filter based on active tab
      const allBrands = data.content || [];
      setBrands(allBrands);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Error loading brands:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (globalThis.confirm('Are you sure you want to delete this brand?')) {
      try {
        await deleteBrand(id);
        loadBrands();
      } catch (error) {
        console.error('Error deleting brand:', error);
        alert('Failed to delete brand');
      }
    }
  };

  const handleToggleActive = async (brand) => {
    try {
      const updatedBrand = {
        ...brand,
        isActive: !brand.isActive
      };
      await updateBrand(brand.id, updatedBrand);
      loadBrands();
    } catch (error) {
      console.error('Error toggling brand status:', error);
      alert('Failed to update brand status');
    }
  };

  // Filter brands based on active tab
  const displayBrands = brands.filter(brand => 
    activeTab === 'active' ? brand.isActive !== false : brand.isActive === false
  );

  const activeCount = brands.filter(b => b.isActive !== false).length;
  const inactiveCount = brands.filter(b => b.isActive === false).length;

  if (loading) return <div>Loading brands...</div>;

  return (
    <div className="admin-brands-page">
      <div className="page-header">
        <h1>Brand Management</h1>
        <Link to="/admin/brands/new">
          <Button variant="success">Create New Brand</Button>
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
          Active Brands 
        <span className="tab-count">{activeCount}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'inactive' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('inactive');
            setCurrentPage(0);
          }}
        >
          Inactive Brands 
        <span className="tab-count">{inactiveCount}</span>
        </button>
      </div>

      {/* Results info */}
      <div className="results-info">
        Showing {displayBrands.length} of {activeTab === 'active' ? activeCount : inactiveCount} brands
      </div>

      <div className="brand-table-wrapper">
        <table className="brand-table">
          <thead>
            <tr>
              <th className="col-id">ID</th>
              <th className="col-logo">Logo</th>
              <th className="col-name">Name</th>
              <th className="col-description">Description</th>
              <th className="col-website">Website</th>
              <th className="col-products">Products</th>
              <th className="col-status">Status</th>
              <th className="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            {displayBrands.map(brand => (
              <tr key={brand.id}>
                <td className="col-id">{brand.id}</td>
                <td className="col-logo">
                  {brand.logoUrl ? (
                    <img 
                      src={brand.logoUrl} 
                      alt={brand.name} 
                      className="brand-logo"
                    />
                  ) : (
                    <div className="no-logo">
                      {brand.name.charAt(0)}
                    </div>
                  )}
                </td>
                <td className="col-name">
                  <div className="brand-name-cell" title={brand.name}>
                    {brand.name}
                  </div>
                </td>
                <td className="col-description">
                  <div className="brand-description-cell" title={brand.description}>
                    {brand.description?.length > 50 
                      ? `${brand.description.substring(0, 50)}...` 
                      : brand.description || '-'}
                  </div>
                </td>
                <td className="col-website">
                  {brand.website ? (
                    <a href={brand.website} target="_blank" rel="noopener noreferrer" className="brand-website">
                      Visit
                    </a>
                  ) : '-'}
                </td>
                <td className="col-products">
                  <span className="product-count-badge">{brand.productCount || 0}</span>
                </td>
                <td className="col-status">
                  <div className="admin-toggle-container">
                    <label className="admin-toggle-switch">
                      <input
                        type="checkbox"
                        checked={brand.isActive !== false}
                        onChange={() => handleToggleActive(brand)}
                        className="admin-toggle-input"
                      />
                      <span className="admin-toggle-slider"></span>
                    </label>
                    <span className="admin-toggle-label">
                      {brand.isActive !== false ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                </td>
                <td className="col-actions">
                  <div className="action-buttons">
                    <Link to={`/admin/brands/edit/${brand.id}`}>
                      <Button variant="primary" className="action-btn">Edit</Button>
                    </Link>
                    <Button 
                      variant="danger" 
                      onClick={() => handleDelete(brand.id)}
                      className="action-btn"
                    >
                      Delete
                    </Button>
                  </div>
                </td>
              </tr>
            ))}
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

export default AdminBrandsPage;