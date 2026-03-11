import React from 'react';
import { useCart } from '../../hooks/useCart';

const OrderSummary = () => {
  const { cart, cartTotal } = useCart();

  return (
    <div style={{
      padding: '1rem',
      backgroundColor: 'rgba(0,217,255,0.1)',
      borderRadius: '4px'
    }}>
      <h3>Order Summary</h3>
      {cart.items.map(item => (
        <div key={item.id} style={{
          display: 'flex',
          justifyContent: 'space-between',
          marginBottom: '0.5rem',
          paddingBottom: '0.5rem',
          borderBottom: '1px solid #ddd'
        }}>
          <span>
            {item.productName} x {item.quantity}
          </span>
          <span>${(item.price * item.quantity).toFixed(2)}</span>
        </div>
      ))}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        marginTop: '1rem',
        fontWeight: 'bold'
      }}>
        <span>Total:</span>
        <span>${cartTotal.toFixed(2)}</span>
      </div>
    </div>
  );
};

export default OrderSummary;