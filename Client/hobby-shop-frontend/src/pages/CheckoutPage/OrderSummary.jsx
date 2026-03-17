import React from 'react';

const OrderSummary = ({ cartItems, cartTotal }) => {
  const tax = cartTotal * 0.08;
  const total = cartTotal + tax;

  return (
    <div style={{
      background: 'rgba(0, 0, 0, 0.6)',
      border: '2px solid rgba(0, 217, 255, 0.3)',
      borderRadius: '12px',
      padding: '2rem',
      position: 'sticky',
      top: '2rem'
    }}>
      <h3 style={{ color: '#00d9ff', marginBottom: '1.5rem', fontSize: '1.2rem' }}>
        Order Summary
      </h3>

      {/* Items */}
      <div style={{ marginBottom: '1.5rem' }}>
        {cartItems.map(item => (
          <div key={item.id} style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '0.75rem 0',
            borderBottom: '1px solid rgba(0, 217, 255, 0.1)',
            gap: '1rem'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flex: 1, minWidth: 0 }}>
              {item.productImage && (
                <img
                  src={item.productImage}
                  alt={item.productName}
                  style={{ width: '48px', height: '48px', objectFit: 'cover', borderRadius: '4px', flexShrink: 0 }}
                />
              )}
              <div style={{ minWidth: 0 }}>
                <div style={{ color: '#e0e1dd', fontSize: '0.9rem', fontWeight: '500', wordBreak: 'break-word' }}>
                  {item.productName}
                </div>
                <div style={{ color: '#888', fontSize: '0.8rem' }}>Qty: {item.quantity}</div>
              </div>
            </div>
            <span style={{ color: '#e0e1dd', fontWeight: '600', whiteSpace: 'nowrap', flexShrink: 0 }}>
              ${(item.price * item.quantity).toFixed(2)}
            </span>
          </div>
        ))}
      </div>

      {/* Totals */}
      <div style={{ borderTop: '1px solid rgba(0, 217, 255, 0.3)', paddingTop: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: '#e0e1dd' }}>
          <span>Subtotal</span>
          <span>${cartTotal.toFixed(2)}</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: '#e0e1dd' }}>
          <span>Shipping</span>
          <span>Free</span>
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', color: '#e0e1dd' }}>
          <span>Tax (8%)</span>
          <span>${tax.toFixed(2)}</span>
        </div>
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          marginTop: '1rem',
          paddingTop: '1rem',
          borderTop: '2px solid rgba(0, 217, 255, 0.5)',
          color: '#00d9ff',
          fontWeight: '700',
          fontSize: '1.1rem'
        }}>
          <span>Total</span>
          <span>${total.toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
};

export default OrderSummary;
