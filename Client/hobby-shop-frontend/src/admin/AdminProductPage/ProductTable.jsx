import React from 'react';

const ProductTable = ({ products, onEdit, onDelete }) => {
  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ backgroundColor: '#f8f9fa' }}>
          <th style={{ padding: '1rem', textAlign: 'left' }}>ID</th>
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
        {products.map(product => (
          <tr key={product.id} style={{ borderBottom: '1px solid #ddd' }}>
            <td style={{ padding: '1rem' }}>{product.id}</td>
            <td style={{ padding: '1rem' }}>{product.name}</td>
            <td style={{ padding: '1rem' }}>{product.brandName}</td>
            <td style={{ padding: '1rem' }}>{product.categoryName}</td>
            <td style={{ padding: '1rem' }}>${product.price}</td>
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
              <button
                onClick={() => onEdit(product)}
                style={{
                  padding: '0.25rem 0.5rem',
                  backgroundColor: '#ffc107',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  marginRight: '0.5rem'
                }}
              >
                Edit
              </button>
              <button
                onClick={() => onDelete(product.id)}
                style={{
                  padding: '0.25rem 0.5rem',
                  backgroundColor: '#dc3545',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Delete
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default ProductTable;