// src/admin/AdminProductsPage/ProductTable.jsx
import React from 'react';
import { Button } from '../../components/ui';

const ProductTable = ({ 
  products, 
  onEdit, 
  onDelete, 
  onRestore, 
  onToggleFeatured,
  onToggleActive,
  showInactive 
}) => {
  const productsArray = Array.isArray(products) ? products : [];
  
  if (productsArray.length === 0) {
    return (
      <div className="empty-state">
        <p>No {showInactive ? 'inactive' : 'active'} products found.</p>
      </div>
    );
  }

  return (
    <div className="product-table-wrapper">
      <table className="product-table">
        <thead>
          <tr>
            <th className="col-id">ID</th>
            <th className="col-image">Image</th>
            <th className="col-name">Name</th>
            <th className="col-brand">Brand</th>
            <th className="col-category hide-mobile">Category</th>
            <th className="col-price">Price</th>
            <th className="col-stock">Stock</th>
            <th className="col-status">Status</th>
            <th className="col-featured">Featured</th>
            <th className="col-actions">Actions</th>
          </tr>
        </thead>
        <tbody>
          {productsArray.map(product => (
            <tr key={product.id}>
              <td className="col-id">{product.id}</td>
              <td className="col-image">
                {product.imageUrl ? (
                  <img 
                    src={product.imageUrl} 
                    alt={product.name}
                    className="product-thumbnail"
                  />
                ) : (
                  <div className="no-image">No Image</div>
                )}
              </td>
              <td className="col-name">
                <div className="product-name-cell" title={product.name}>
                  {product.name}
                </div>
              </td>
              <td className="col-brand">{product.brandName || '-'}</td>
              <td className="col-category hide-mobile">{product.categoryName || '-'}</td>
              <td className="col-price">${product.price?.toFixed(2)}</td>
              <td className="col-stock">
                <span className={`stock-badge ${product.stockQuantity > 0 ? 'in-stock' : 'out-of-stock'}`}>
                  {product.stockQuantity}
                </span>
              </td>
              <td className="col-status">
                <button
                  onClick={() => onToggleActive(product)}
                  disabled={showInactive}
                  style={{
                    padding: '6px 12px',
                    border: 'none',
                    borderRadius: '4px',
                    backgroundColor: product.isActive ? '#28a745' : '#6c757d',
                    color: 'white',
                    cursor: showInactive ? 'not-allowed' : 'pointer',
                    fontWeight: 'bold',
                    fontSize: '0.85rem',
                    minWidth: '70px',
                    opacity: showInactive ? 0.5 : 1,
                    boxShadow: product.isActive ? '0 0 10px rgba(40, 167, 69, 0.5)' : 'none'
                  }}
                >
                  {product.isActive ? 'ACTIVE' : 'INACTIVE'}
                </button>
              </td>
              <td className="col-featured">
                <button
                  onClick={() => onToggleFeatured(product)}
                  disabled={!product.isActive}
                  style={{
                    padding: '6px 12px',
                    border: 'none',
                    borderRadius: '4px',
                    backgroundColor: product.isFeatured ? '#ffc107' : '#6c757d',
                    color: product.isFeatured ? '#000' : 'white',
                    cursor: !product.isActive ? 'not-allowed' : 'pointer',
                    fontWeight: 'bold',
                    fontSize: '0.85rem',
                    minWidth: '70px',
                    opacity: !product.isActive ? 0.5 : 1,
                    boxShadow: product.isFeatured ? '0 0 10px rgba(255, 193, 7, 0.5)' : 'none'
                  }}
                >
                  {product.isFeatured ? 'FEATURED' : 'REGULAR'}
                </button>
              </td>
              <td className="col-actions">
                <div className="action-buttons">
                  {product.isActive ? (
                    <>
                      <Button 
                        variant="primary" 
                        onClick={() => onEdit(product)} 
                        className="action-btn"
                        title="Edit product"
                      >
                        Edit
                      </Button>
                      <Button 
                        variant="danger" 
                        onClick={() => onDelete(product.id)}
                        className="action-btn"
                        title="Delete product"
                      >
                        Delete
                      </Button>
                    </>
                  ) : (
                    <Button 
                      variant="success" 
                      onClick={() => onRestore(product.id)}
                      className="action-btn"
                      title="Restore product"
                    >
                      Restore
                    </Button>
                  )}
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ProductTable;