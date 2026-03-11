import React from 'react';
import { Button } from '../../components/ui';

const ProductTable = ({ products, onEdit, onDelete }) => {
  // Ensure products is an array
  const productsArray = Array.isArray(products) ? products : [];
  
  if (productsArray.length === 0) {
    return (
      <div style={{ 
        textAlign: 'center', 
        padding: '2rem',
        backgroundColor: '#f8f9fa',
        borderRadius: '4px'
      }}>
        <p>No products found.</p>
      </div>
    );
  }

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ backgroundColor: '#f8f9fa' }}>
          <th style={{ padding: '1rem', textAlign: 'left' }}>ID</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Image</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Name</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Brand</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Category</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Price</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Stock</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Status</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Actions</th>
        </tr>
      </thead>
      <tbody>
        {productsArray.map(product => (
          <tr key={product.id} style={{ borderBottom: '1px solid #ddd' }}>
            <td style={{ padding: '1rem' }}>{product.id}</td>
            <td style={{ padding: '1rem' }}>
              {product.imageUrl ? (
                <img 
                  src={product.imageUrl} 
                  alt={product.name}
                  style={{ width: '50px', height: '50px', objectFit: 'cover', borderRadius: '4px' }}
                />
              ) : (
                <div style={{
                  width: '50px',
                  height: '50px',
                  backgroundColor: '#e9ecef',
                  borderRadius: '4px'
                }} />
              )}
            </td>
            <td style={{ padding: '1rem', fontWeight: 'bold' }}>{product.name}</td>
            <td style={{ padding: '1rem' }}>{product.brandName || '-'}</td>
            <td style={{ padding: '1rem' }}>{product.categoryName || '-'}</td>
            <td style={{ padding: '1rem' }}>${product.price?.toFixed(2)}</td>
            <td style={{ padding: '1rem' }}>{product.stockQuantity}</td>
            <td style={{ padding: '1rem' }}>
              <span style={{
                padding: '0.25rem 0.5rem',
                backgroundColor: product.isActive ? '#28a745' : '#dc3545',
                color: 'white',
                borderRadius: '4px',
                fontSize: '0.875rem'
              }}>
                {product.isActive ? 'Active' : 'Inactive'}
              </span>
            </td>
            <td style={{ padding: '1rem' }}>
              <Button
                variant="primary"
                onClick={() => onEdit(product)}
                style={{ marginRight: '0.5rem' }}
              >
                Edit
              </Button>
              <Button
                variant="danger"
                onClick={() => onDelete(product.id)}
              >
                Delete
              </Button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default ProductTable;