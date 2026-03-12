// src/pages/OrderDetailPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getOrderByNumber, cancelOrder } from '../services/orderService';
import { useAuth } from '../hooks/useAuth';
import { Button } from '../components/ui';

const OrderDetailPage = () => {
  const { orderNumber } = useParams();
  const { isAuthenticated } = useAuth();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [cancelling, setCancelling] = useState(false);

  const loadOrder = useCallback(async () => {
    try {
      const data = await getOrderByNumber(orderNumber);
      setOrder(data);
    } catch (error) {
      console.error('Error loading order:', error);
    } finally {
      setLoading(false);
    }
  }, [orderNumber]);

  useEffect(() => {
    loadOrder();
  }, [loadOrder]);

  const handleCancelOrder = async () => {
    if (!window.confirm('Are you sure you want to cancel this order?')) {
      return;
    }

    setCancelling(true);
    try {
      await cancelOrder(order.id, 'Cancelled by customer');
      await loadOrder();
    } catch (error) {
      console.error('Error cancelling order:', error);
      alert('Failed to cancel order');
    } finally {
      setCancelling(false);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      'PENDING': '#ffc107',
      'PROCESSING': '#17a2b8',
      'SHIPPED': '#007bff',
      'DELIVERED': '#28a745',
      'CANCELLED': '#dc3545'
    };
    return colors[status] || '#6c757d';
  };

  const getPaymentMethodDisplay = (method) => {
    const methods = {
      'credit_card': 'Credit Card',
      'paypal': 'PayPal',
      'cash_on_delivery': 'Cash on Delivery'
    };
    return methods[method] || method;
  };

  if (loading) return <div className="loading">Loading order details...</div>;
  if (!order) return <div className="error">Order not found</div>;

  return (
    <div className="order-detail-page">
      <div className="order-header">
        <h1>Order #{order.orderNumber}</h1>
        <span 
          className="status-badge" 
          style={{ backgroundColor: getStatusColor(order.status) }}
        >
          {order.status}
        </span>
      </div>

      <div className="order-grid">
        <div className="order-section">
          <h3>Shipping Address</h3>
          <p>
            {order.shippingAddress?.firstName} {order.shippingAddress?.lastName}<br />
            {order.shippingAddress?.address}<br />
            {order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.postalCode}<br />
            {order.shippingAddress?.country}<br />
            {order.shippingAddress?.email}<br />
            {order.shippingAddress?.phone}
          </p>
        </div>

        <div className="order-section">
          <h3>Payment Information</h3>
          <p>
            <strong>Method:</strong> {getPaymentMethodDisplay(order.paymentMethod)}<br />
            {order.paymentMethod === 'credit_card' && order.cardLastFour && (
              <>Card ending in {order.cardLastFour}</>
            )}
          </p>
          
          <h3 style={{ marginTop: '1rem' }}>Billing Address</h3>
          {order.billingAddress ? (
            <p>
              {order.billingAddress}<br />
              {order.billingCity}, {order.billingPostalCode}<br />
              {order.billingCountry}
            </p>
          ) : (
            <p>Same as shipping address</p>
          )}

          <p style={{ marginTop: '1rem' }}>
            <strong>Payment Status:</strong>{' '}
            <span className={`payment-status ${order.paymentStatus?.toLowerCase()}`}>
              {order.paymentStatus}
            </span>
          </p>
        </div>
      </div>

      <div className="order-items-section">
        <h3>Order Items</h3>
        <table className="order-items-table">
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

      {order.trackingNumber && (
        <div className="tracking-section">
          <h3>Tracking Information</h3>
          <p>Tracking Number: {order.trackingNumber}</p>
        </div>
      )}

      <div className="order-actions">
        {order.status === 'PENDING' && isAuthenticated() && (
          <Button 
            variant="danger" 
            onClick={handleCancelOrder}
            disabled={cancelling}
          >
            {cancelling ? 'Cancelling...' : 'Cancel Order'}
          </Button>
        )}
        <Link to="/orders">
          <Button variant="secondary">Back to Orders</Button>
        </Link>
      </div>
    </div>
  );
};

export default OrderDetailPage;