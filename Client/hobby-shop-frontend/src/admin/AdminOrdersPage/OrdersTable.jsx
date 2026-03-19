// src/admin/AdminOrdersPage/OrdersTable.jsx
import React from 'react';
import { Button } from '../../components/ui';

const OrdersTable = ({ orders, onStatusChange, onViewDetails }) => {
  const ordersArray = Array.isArray(orders) ? orders : [];
  
  if (ordersArray.length === 0) {
    return (
      <div className="empty-state">
        <p>No orders found.</p>
      </div>
    );
  }

  const getStatusBadgeClass = (status) => {
    switch(status) {
      case 'PENDING': return 'status-pending';
      case 'PROCESSING': return 'status-processing';
      case 'SHIPPED': return 'status-shipped';
      case 'DELIVERED': return 'status-delivered';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  };

  const getPaymentBadgeClass = (status) => {
    switch(status) {
      case 'PAID': return 'payment-paid';
      case 'PENDING': return 'payment-pending';
      case 'FAILED': return 'payment-failed';
      case 'REFUNDED': return 'payment-refunded';
      default: return '';
    }
  };

  return (
    <div className="orders-table-wrapper">
      <table className="orders-table">
        <thead>
          <tr>
            <th className="col-order">Order #</th>
            <th className="col-customer">Customer</th>
            <th className="col-date">Date</th>
            <th className="col-total">Total</th>
            <th className="col-status">Status</th>
            <th className="col-payment">Payment</th>
            <th className="col-actions">Actions</th>
          </tr>
        </thead>
        <tbody>
          {ordersArray.map(order => (
            <tr key={order.id}>
              <td className="col-order">
                <span className="order-number">{order.orderNumber}</span>
              </td>
              <td className="col-customer">
                <div className="customer-name" title={order.customerName || `${order.shippingAddress?.firstName} ${order.shippingAddress?.lastName}`}>
                  {order.customerName || `${order.shippingAddress?.firstName} ${order.shippingAddress?.lastName}`}
                </div>
                {/* Display email or phone for better identification */}
                <div className="customer-contact" style={{ fontSize: '0.8rem', color: '#888' }}>
                  {order.customerEmail || order.shippingAddress?.email}
                </div>
              </td>
              <td className="col-date">
                {order.orderDate ? new Date(order.orderDate).toLocaleDateString() : 'N/A'}
              </td>
              <td className="col-total">
                <span className="order-total">${order.totalAmount?.toFixed(2)}</span>
              </td>
              <td className="col-status">
                <select
                  value={order.status}
                  onChange={(e) => onStatusChange(order.id, e.target.value)}
                  className={`status-select ${getStatusBadgeClass(order.status)}`}
                >
                  <option value="PENDING">Pending</option>
                  <option value="PROCESSING">Processing</option>
                  <option value="SHIPPED">Shipped</option>
                  <option value="DELIVERED">Delivered</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </td>
              <td className="col-payment">
                <span className={`payment-badge ${getPaymentBadgeClass(order.paymentStatus)}`}>
                  {order.paymentStatus}
                </span>
              </td>
              <td className="col-actions">
                <Button
                  variant="primary"
                  onClick={() => {
                    console.log('Order data being passed:', order); // Debug log
                    onViewDetails(order);
                  }}
                  className="action-btn"
                >
                  View Details
                </Button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default OrdersTable;