// src/pages/CartPage.jsx
import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../hooks/useCart';
import { useAuth } from '../hooks/useAuth';
import { CartItemRow } from '../components/common';
import { Button } from '../components/ui';

const CartPage = () => {
  const navigate = useNavigate();
  const { cart, updateQuantity, removeFromCart, cartTotal, cartItemsCount } = useCart();
  const { isAuthenticated } = useAuth();

  // If cart is empty, show empty cart message
  if (!cart || cart.items.length === 0) {
    return (
      <div className="empty-cart">
        <div className="empty-cart-content">
          <span className="empty-cart-icon">🛒</span>
          <h2>Your cart is empty</h2>
          <p>Looks like you haven't added anything to your cart yet.</p>
          <Link to="/products">
            <Button variant="primary" className="continue-shopping-btn">
              Continue Shopping
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  const handleCheckout = () => {
    if (isAuthenticated()) {
      navigate('/checkout');
    } else {
      // Redirect to login with return URL - use full path with leading slash
      navigate('/login?redirect=/checkout');
    }
  };

  const handleContinueShopping = () => {
    navigate('/products');
  };

  return (
    <div className="cart-page">
      <h1>Shopping Cart</h1>
      
      <div className="cart-header">
        <p className="cart-item-count" aria-live="polite" aria-atomic="true">
          {cartItemsCount} {cartItemsCount === 1 ? 'item' : 'items'} in your cart
        </p>
        {!isAuthenticated() && (
          <div className="guest-warning">
            <span className="warning-icon">⚠️</span>
            <div className="warning-content">
              <strong>You're shopping as a guest</strong>
              <p>Your cart will be saved to your account when you log in or register.</p>
            </div>
          </div>
        )}
      </div>

      <div className="cart-table-container">
        <table className="cart-table" aria-label="Shopping cart items">
          <thead>
            <tr>
              <th>Product</th>
              <th>Price</th>
              <th>Quantity</th>
              <th>Total</th>
              <th>Action</th>
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
      </div>

      <div className="cart-summary">
        <div className="cart-totals">
          <h3>Order Summary</h3>
          
          <div className="total-row">
            <span>Subtotal:</span>
            <span>${cartTotal.toFixed(2)}</span>
          </div>
          
          <div className="total-row">
            <span>Shipping:</span>
            <span>Calculated at checkout</span>
          </div>
          
          <div className="total-row">
            <span>Tax:</span>
            <span>Calculated at checkout</span>
          </div>
          
          <div className="total-row grand-total">
            <span>Estimated Total:</span>
            <span>${cartTotal.toFixed(2)}</span>
          </div>
        </div>

        <div className="cart-actions">
          <Button 
            variant="secondary" 
            onClick={handleContinueShopping}
            className="continue-shopping-btn"
          >
            ← Continue Shopping
          </Button>
          
          <Button 
            variant="success" 
            onClick={handleCheckout}
            className="checkout-btn"
            disabled={cartItemsCount === 0}
          >
            {isAuthenticated() ? 'Proceed to Checkout' : 'Login to Checkout'}
          </Button>
        </div>
      </div>

      {/* Trust badges / guarantees */}
      <div className="cart-guarantees">
        <div className="guarantee-item">
          <span className="guarantee-icon">🔒</span>
          <div className="guarantee-text">
            <strong>Secure Checkout</strong>
            <p>Your information is protected</p>
          </div>
        </div>
        <div className="guarantee-item">
          <span className="guarantee-icon">🚚</span>
          <div className="guarantee-text">
            <strong>Free Shipping Over $50</strong>
            <p>On all domestic orders</p>
          </div>
        </div>
        <div className="guarantee-item">
          <span className="guarantee-icon">🔄</span>
          <div className="guarantee-text">
            <strong>30-Day Returns</strong>
            <p>Hassle-free returns</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CartPage;