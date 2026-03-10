import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getUserOrders } from '../services/orderService';

const OrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      const data = await getUserOrders();
      setOrders(data.content || data);
    } catch (error) {
      console.error('Error loading orders:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading orders...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>My Orders</h1>
      
      {orders.length === 0 ? (
        <p>You haven't placed any orders yet.</p>
      ) : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {orders.map(order => (
            <Link 
              key={order.id} 
              to={`/order/${order.orderNumber}`}
              style={{ textDecoration: 'none', color: 'inherit' }}
            >
              <div style={{
                padding: '1rem',
                border: '1px solid #ddd',
                borderRadius: '4px',
                display: 'grid',
                gridTemplateColumns: 'auto 1fr auto auto auto',
                gap: '1rem',
                alignItems: 'center'
              }}>
                <span style={{ fontWeight: 'bold' }}>#{order.orderNumber}</span>
                <span>{new Date(order.orderDate).toLocaleDateString()}</span>
                <span style={{
                  padding: '0.25rem 0.5rem',
                  backgroundColor: 
                    order.status === 'DELIVERED' ? '#28a745' :
                    order.status === 'CANCELLED' ? '#dc3545' :
                    order.status === 'SHIPPED' ? '#17a2b8' : '#ffc107',
                  color: 'white',
                  borderRadius: '4px',
                  fontSize: '0.875rem'
                }}>
                  {order.status}
                </span>
                <span style={{ fontWeight: 'bold' }}>${order.totalAmount}</span>
                <span>{order.items?.length || 0} items</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
};

export default OrdersPage;