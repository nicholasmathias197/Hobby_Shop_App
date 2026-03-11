import React from 'react';
import { Button } from '../ui';

const CartItemRow = ({ item, onUpdateQuantity, onRemove }) => {
  const handleQuantityChange = (e) => {
    const newQuantity = parseInt(e.target.value);
    if (newQuantity > 0) {
      onUpdateQuantity(item.id, newQuantity);
    }
  };

  return (
    <tr style={{ borderBottom: '1px solid #ddd' }}>
      <td style={{ padding: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          {item.imageUrl ? (
            <img 
              src={item.imageUrl} 
              alt={item.productName}
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
          <span>{item.productName}</span>
        </div>
      </td>
      <td style={{ padding: '1rem' }}>${item.price?.toFixed(2)}</td>
      <td style={{ padding: '1rem' }}>
        <input
          type="number"
          min="1"
          value={item.quantity}
          onChange={handleQuantityChange}
          style={{
            width: '70px',
            padding: '0.5rem',
            borderRadius: '4px',
            border: '1px solid #ddd'
          }}
        />
      </td>
      <td style={{ padding: '1rem', fontWeight: 'bold' }}>
        ${(item.price * item.quantity).toFixed(2)}
      </td>
      <td style={{ padding: '1rem' }}>
        <Button 
          variant="danger" 
          onClick={() => onRemove(item.id)}
        >
          Remove
        </Button>
      </td>
    </tr>
  );
};

export default CartItemRow;