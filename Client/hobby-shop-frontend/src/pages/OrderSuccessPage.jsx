// src/pages/OrderSuccessPage.jsx
import React, { useEffect, useState, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getOrderByNumber } from '../services/orderService';
import { useAuth } from '../hooks/useAuth';
import { Button } from '../components/ui';

const OrderSuccessPage = () => {
  const { orderNumber } = useParams();
  const { isAuthenticated } = useAuth();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadOrder = useCallback(async () => {
    try {
      const data = await getOrderByNumber(orderNumber);
      setOrder(data);
    } catch (err) {
      setError('Order not found');
      console.error('Error loading order:', err);
    } finally {
      setLoading(false);
    }
  }, [orderNumber]);

  useEffect(() => {
    loadOrder();
  }, [loadOrder]);

  const getPaymentMethodDisplay = (method) => {
    const methods = {
      'credit_card': 'Credit Card',
      'paypal': 'PayPal',
      'cash_on_delivery': 'Cash on Delivery'
    };
    return methods[method] || method;
  };

  const getCardLastFour = (order) => {
    if (order.paymentMethod === 'credit_card' && order.cardLastFour) {
      return `ending in ${order.cardLastFour}`;
    }
    return '';
  };

  if (loading) {
    return (
      <div className="order-loading">
        <div className="spinner"></div>
        <p>Loading order details...</p>
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="order-error">
        <h2>Order Not Found</h2>
        <p>{error || 'The order you\'re looking for doesn\'t exist.'}</p>
        <Link to="/">
          <Button variant="primary">Go Home</Button>
        </Link>
      </div>
    );
  }

  return (
    <div className="order-success-page">
      <div className="success-header">
        <div className="success-icon">✅</div>
        <h1>Order Confirmed!</h1>
        <p className="order-number">Order #{order.orderNumber}</p>
        <p className="thank-you">Thank you for your purchase!</p>
      </div>

      <div className="order-details-card">
        <h2>Order Details</h2>
        
        <div className="detail-grid">
          <div className="detail-section">
            <h3>Shipping Address</h3>
            <p>
              {order.customerName}<br />
              {order.shippingAddress}<br />
              {order.shippingCity}, {order.shippingPostalCode}<br />
              {order.shippingCountry}
            </p>
          </div>

          <div className="detail-section">
            <h3>Payment Method</h3>
            <p className="payment-method">
              {getPaymentMethodDisplay(order.paymentMethod)}
              {order.paymentMethod === 'credit_card' && (
                <span className="card-info"> {getCardLastFour(order)}</span>
              )}
            </p>
            
            <h3>Billing Address</h3>
            {order.billingAddress ? (
              <p>
                {order.billingAddress}<br />
                {order.billingCity}, {order.billingPostalCode}<br />
                {order.billingCountry}
              </p>
            ) : (
              <p>Same as shipping address</p>
            )}
          </div>
        </div>

        <div className="order-items">
          <h3>Order Items</h3>
          <table className="items-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              {order.items?.map(item => (
                <tr key={item.id}>
                  <td>
                    <Link to={`/product/${item.productId}`}>
                      {item.productName}
                    </Link>
                  </td>
                  <td>${item.pricePerUnit?.toFixed(2)}</td>
                  <td>{item.quantity}</td>
                  <td>${item.subtotal?.toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td colSpan="3" className="total-label">Subtotal:</td>
                <td className="total-value">${order.subtotal?.toFixed(2)}</td>
              </tr>
              <tr>
                <td colSpan="3" className="total-label">Shipping:</td>
                <td className="total-value">${order.shippingCost?.toFixed(2)}</td>
              </tr>
              <tr>
                <td colSpan="3" className="total-label">Tax:</td>
                <td className="total-value">${order.tax?.toFixed(2)}</td>
              </tr>
              <tr className="grand-total">
                <td colSpan="3" className="total-label">Total:</td>
                <td className="total-value">${order.totalAmount?.toFixed(2)}</td>
              </tr>
            </tfoot>
          </table>
        </div>

        <div className="order-status">
          <p>
            <strong>Status:</strong> 
            <span className={`status-badge ${order.status?.toLowerCase()}`}>
              {order.status}
            </span>
          </p>
          <p>
            <strong>Order Date:</strong> {new Date(order.createdAt).toLocaleString()}
          </p>
        </div>

        <div className="action-buttons">
          {isAuthenticated() ? (
            <Link to="/orders">
              <Button variant="primary">View All Orders</Button>
            </Link>
          ) : (
            <div className="guest-actions">
              <p>Create an account to track your orders!</p>
              <Link to="/register">
                <Button variant="success">Create Account</Button>
              </Link>
            </div>
          )}
          <Link to="/products">
            <Button variant="secondary">Continue Shopping</Button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default OrderSuccessPage;