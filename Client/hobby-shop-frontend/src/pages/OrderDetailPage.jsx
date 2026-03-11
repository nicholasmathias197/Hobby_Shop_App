import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getOrderByNumber } from '../services/orderService';

const OrderDetailPage = () => {
  const { orderNumber } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrder();
  }, [orderNumber]);

  const loadOrder = async () => {
    try {
      const data = await getOrderByNumber(orderNumber);
      setOrder(data);
    } catch (error) {
      console.error('Error loading order:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading order details...</div>;
  if (!order) return <div>Order not found</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Order #{order.orderNumber}</h1>
      
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(2, 1fr)',
        gap: '2rem',
        marginBottom: '2rem'
      }}>
        <div style={{
          padding: '1rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <h3>Order Information</h3>
          <p><strong>Date:</strong> {new Date(order.orderDate).toLocaleString()}</p>
          <p><strong>Status:</strong> {order.status}</p>
          <p><strong>Payment Status:</strong> {order.paymentStatus}</p>
          {order.trackingNumber && (
            <p><strong>Tracking Number:</strong> {order.trackingNumber}</p>
          )}
        </div>

        <div style={{
          padding: '1rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <h3>Shipping Address</h3>
          <p>{order.shippingAddress?.firstName} {order.shippingAddress?.lastName}</p>
          <p>{order.shippingAddress?.address}</p>
          <p>{order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.postalCode}</p>
          <p>{order.shippingAddress?.country}</p>
          <p>{order.shippingAddress?.email}</p>
          <p>{order.shippingAddress?.phone}</p>
        </div>
      </div>

      <h3>Order Items</h3>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ backgroundColor: '#f8f9fa' }}>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Product</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Price</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Quantity</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Total</th>
          </tr>
        </thead>
        <tbody>
          {order.items?.map(item => (
            <tr key={item.id} style={{ borderBottom: '1px solid #ddd' }}>
              <td style={{ padding: '1rem' }}>{item.productName}</td>
              <td style={{ padding: '1rem' }}>${item.price}</td>
              <td style={{ padding: '1rem' }}>{item.quantity}</td>
              <td style={{ padding: '1rem' }}>${(item.price * item.quantity).toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan="3" style={{ padding: '1rem', textAlign: 'right', fontWeight: 'bold' }}>
              Total:
            </td>
            <td style={{ padding: '1rem', fontWeight: 'bold' }}>
              ${order.totalAmount}
            </td>
          </tr>
        </tfoot>
      </table>

      <div style={{ marginTop: '2rem' }}>
        <Link to="/orders">
          <button style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#6c757d',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}>
            Back to Orders
          </button>
        </Link>
      </div>
    </div>
  );
};

export default OrderDetailPage;