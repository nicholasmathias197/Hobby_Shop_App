import React from 'react';
import { Link } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { CartItemRow } from '../components/common';
import { Button } from '../components/ui';

const CartPage = () => {
  const { cart, updateQuantity, removeFromCart, cartTotal } = useCart();

  if (!cart || cart.items.length === 0) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <h2>Your cart is empty</h2>
        <Link to="/products">
          <Button variant="primary" style={{ marginTop: '1rem' }}>
            Continue Shopping
          </Button>
        </Link>
      </div>
    );
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Shopping Cart</h1>
      
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ backgroundColor: '#f8f9fa' }}>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Product</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Price</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Quantity</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Total</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Action</th>
          </tr>
        </thead>
        <tbody>
          {cart.items.map(item => (
            <CartItemRow
              key={item.id}
              item={item}
              onUpdateQuantity={updateQuantity}
              onRemove={removeFromCart}
            />
          ))}
        </tbody>
      </table>

      <div style={{
        marginTop: '2rem',
        padding: '1rem',
        backgroundColor: '#f8f9fa',
        borderRadius: '4px',
        textAlign: 'right'
      }}>
        <h3>Total: ${cartTotal.toFixed(2)}</h3>
        <div style={{ marginTop: '1rem' }}>
          <Link to="/checkout">
            <Button variant="success" style={{ marginRight: '1rem' }}>
              Proceed to Checkout
            </Button>
          </Link>
          <Link to="/products">
            <Button variant="secondary">Continue Shopping</Button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default CartPage;