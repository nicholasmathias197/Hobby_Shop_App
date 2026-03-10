import React from 'react';

const CartItemRow = ({ item, onUpdateQuantity, onRemove }) => {
  return (
    <tr>
      <td>{item.productName}</td>
      <td>${item.price}</td>
      <td>
        <input
          type="number"
          min="1"
          value={item.quantity}
          onChange={(e) => onUpdateQuantity(item.id, parseInt(e.target.value))}
          style={{ width: '60px' }}
        />
      </td>
      <td>${(item.price * item.quantity).toFixed(2)}</td>
      <td>
        <button 
          onClick={() => onRemove(item.id)}
          style={{
            backgroundColor: '#dc3545',
            color: 'white',
            border: 'none',
            padding: '0.25rem 0.5rem',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Remove
        </button>
      </td>
    </tr>
  );
};

export default CartItemRow;