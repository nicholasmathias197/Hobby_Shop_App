import React, { useState, useEffect } from 'react';
import { getAllOrders, updateOrderStatus, updatePaymentStatus, updateTrackingNumber } from '../../services/orderService';
import OrdersTable from './OrdersTable';

const AdminOrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetails, setShowDetails] = useState(false);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      const data = await getAllOrders();
      setOrders(data);
    } catch (error) {
      console.error('Error loading orders:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      await updateOrderStatus(orderId, { status: newStatus, comment: 'Status updated by admin' });
      await loadOrders();
    } catch (error) {
      console.error('Error updating order status:', error);
      alert('Failed to update order status');
    }
  };

  const handleViewDetails = (order) => {
    setSelectedOrder(order);
    setShowDetails(true);
  };

  const handlePaymentUpdate = async (orderId, paymentStatus) => {
    try {
      await updatePaymentStatus(orderId, { paymentStatus });
      await loadOrders();
      setShowDetails(false);
    } catch (error) {
      console.error('Error updating payment status:', error);
      alert('Failed to update payment status');
    }
  };

  const handleTrackingUpdate = async (orderId, trackingNumber) => {
    try {
      await updateTrackingNumber(orderId, { trackingNumber });
      await loadOrders();
      setShowDetails(false);
    } catch (error) {
      console.error('Error updating tracking number:', error);
      alert('Failed to update tracking number');
    }
  };

  if (loading) return <div>Loading orders...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Manage Orders</h1>

      {showDetails && selectedOrder && (
        <div style={{
          marginBottom: '2rem',
          padding: '1rem',
          border: '1px solid #ddd',
          borderRadius: '4px'
        }}>
          <h2>Order #{selectedOrder.orderNumber} Details</h2>
          
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(2, 1fr)',
            gap: '2rem',
            marginTop: '1rem'
          }}>
            <div>
              <h3>Customer Information</h3>
              <p><strong>Name:</strong> {selectedOrder.shippingAddress?.firstName} {selectedOrder.shippingAddress?.lastName}</p>
              <p><strong>Email:</strong> {selectedOrder.shippingAddress?.email}</p>
              <p><strong>Phone:</strong> {selectedOrder.shippingAddress?.phone}</p>
              <p><strong>Address:</strong> {selectedOrder.shippingAddress?.address}</p>
              <p><strong>City:</strong> {selectedOrder.shippingAddress?.city}</p>
              <p><strong>Postal Code:</strong> {selectedOrder.shippingAddress?.postalCode}</p>
              <p><strong>Country:</strong> {selectedOrder.shippingAddress?.country}</p>
            </div>

            <div>
              <h3>Order Information</h3>
              <p><strong>Date:</strong> {new Date(selectedOrder.orderDate).toLocaleString()}</p>
              <p><strong>Status:</strong> {selectedOrder.status}</p>
              <p><strong>Payment Status:</strong> {selectedOrder.paymentStatus}</p>
              {selectedOrder.trackingNumber && (
                <p><strong>Tracking Number:</strong> {selectedOrder.trackingNumber}</p>
              )}

              <div style={{ marginTop: '1rem' }}>
                <h4>Update Tracking Number</h4>
                <input
                  type="text"
                  placeholder="Enter tracking number"
                  onBlur={(e) => handleTrackingUpdate(selectedOrder.id, e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    borderRadius: '4px',
                    border: '1px solid #ddd'
                  }}
                />
              </div>

              <div style={{ marginTop: '1rem' }}>
                <h4>Update Payment Status</h4>
                <select
                  onChange={(e) => handlePaymentUpdate(selectedOrder.id, e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    borderRadius: '4px',
                    border: '1px solid #ddd'
                  }}
                >
                  <option value="PENDING">Pending</option>
                  <option value="PAID">Paid</option>
                  <option value="FAILED">Failed</option>
                  <option value="REFUNDED">Refunded</option>
                </select>
              </div>
            </div>
          </div>

          <h3 style={{ marginTop: '1rem' }}>Order Items</h3>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: '#f8f9fa' }}>
                <th style={{ padding: '0.5rem', textAlign: 'left' }}>Product</th>
                <th style={{ padding: '0.5rem', textAlign: 'left' }}>Price</th>
                <th style={{ padding: '0.5rem', textAlign: 'left' }}>Quantity</th>
                <th style={{ padding: '0.5rem', textAlign: 'left' }}>Total</th>
              </tr>
            </thead>
            <tbody>
              {selectedOrder.items?.map(item => (
                <tr key={item.id} style={{ borderBottom: '1px solid #ddd' }}>
                  <td style={{ padding: '0.5rem' }}>{item.productName}</td>
                  <td style={{ padding: '0.5rem' }}>${item.price}</td>
                  <td style={{ padding: '0.5rem' }}>{item.quantity}</td>
                  <td style={{ padding: '0.5rem' }}>${(item.price * item.quantity).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ marginTop: '1rem' }}>
            <button
              onClick={() => setShowDetails(false)}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#6c757d',
                color: 'white',
                border: 'none',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Close Details
            </button>
          </div>
        </div>
      )}

      <OrdersTable
        orders={orders}
        onStatusChange={handleStatusChange}
        onViewDetails={handleViewDetails}
      />
    </div>
  );
};

export default AdminOrdersPage;