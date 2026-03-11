import React from 'react';
import { Button } from '../../components/ui';

const OrdersTable = ({ orders, onStatusChange, onViewDetails }) => {
  // Ensure orders is an array
  const ordersArray = Array.isArray(orders) ? orders : [];
  
  if (ordersArray.length === 0) {
    return (
      <div style={{ 
        textAlign: 'center', 
        padding: '2rem',
        backgroundColor: '#f8f9fa',
        borderRadius: '4px'
      }}>
        <p>No orders found.</p>
      </div>
    );
  }

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr style={{ backgroundColor: '#f8f9fa' }}>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Order #</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Customer</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Date</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Total</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Status</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Payment</th>
          <th style={{ padding: '1rem', textAlign: 'left' }}>Actions</th>
        </tr>
      </thead>
      <tbody>
        {ordersArray.map(order => (
          <tr key={order.id} style={{ borderBottom: '1px solid #ddd' }}>
            <td style={{ padding: '1rem' }}>{order.orderNumber}</td>
            <td style={{ padding: '1rem' }}>
              {order.customerName || `${order.shippingAddress?.firstName} ${order.shippingAddress?.lastName}`}
            </td>
            <td style={{ padding: '1rem' }}>
              {new Date(order.orderDate).toLocaleDateString()}
            </td>
            <td style={{ padding: '1rem' }}>${order.totalAmount?.toFixed(2)}</td>
            <td style={{ padding: '1rem' }}>
              <select
                value={order.status}
                onChange={(e) => onStatusChange(order.id, e.target.value)}
                style={{
                  padding: '0.25rem',
                  borderRadius: '4px',
                  border: '1px solid #ddd'
                }}
              >
                <option value="PENDING">Pending</option>
                <option value="PROCESSING">Processing</option>
                <option value="SHIPPED">Shipped</option>
                <option value="DELIVERED">Delivered</option>
                <option value="CANCELLED">Cancelled</option>
              </select>
            </td>
            <td style={{ padding: '1rem' }}>
              <span style={{
                padding: '0.25rem 0.5rem',
                backgroundColor: 
                  order.paymentStatus === 'PAID' ? '#28a745' :
                  order.paymentStatus === 'FAILED' ? '#dc3545' :
                  order.paymentStatus === 'REFUNDED' ? '#ffc107' : '#6c757d',
                color: 'white',
                borderRadius: '4px',
                fontSize: '0.875rem'
              }}>
                {order.paymentStatus}
              </span>
            </td>
            <td style={{ padding: '1rem' }}>
              <Button
                variant="primary"
                onClick={() => onViewDetails(order)}
              >
                View Details
              </Button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
};

export default OrdersTable;