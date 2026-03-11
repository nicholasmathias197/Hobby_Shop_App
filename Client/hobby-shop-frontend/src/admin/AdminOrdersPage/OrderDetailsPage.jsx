import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getOrderByNumber, updateOrderStatus, updatePaymentStatus, updateTrackingNumber } from '../../services/orderService';
import { Button } from '../../components/ui';

const OrderDetailsPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [trackingInput, setTrackingInput] = useState('');

  useEffect(() => {
    loadOrder();
  }, [orderId]);

  const loadOrder = async () => {
    try {
      const data = await getOrderByNumber(orderId);
      setOrder(data);
      setTrackingInput(data.trackingNumber || '');
    } catch (error) {
      console.error('Error loading order:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus) => {
    setUpdating(true);
    try {
      await updateOrderStatus(order.id, { status: newStatus, comment: 'Status updated by admin' });
      await loadOrder();
    } catch (error) {
      console.error('Error updating status:', error);
      alert('Failed to update order status');
    } finally {
      setUpdating(false);
    }
  };

  const handlePaymentUpdate = async (paymentStatus) => {
    setUpdating(true);
    try {
      await updatePaymentStatus(order.id, { paymentStatus });
      await loadOrder();
    } catch (error) {
      console.error('Error updating payment:', error);
      alert('Failed to update payment status');
    } finally {
      setUpdating(false);
    }
  };

  const handleTrackingUpdate = async () => {
    setUpdating(true);
    try {
      await updateTrackingNumber(order.id, { trackingNumber: trackingInput });
      await loadOrder();
      alert('Tracking number updated successfully');
    } catch (error) {
      console.error('Error updating tracking:', error);
      alert('Failed to update tracking number');
    } finally {
      setUpdating(false);
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

  const getPaymentStatusColor = (status) => {
    const colors = {
      'PENDING': '#ffc107',
      'PAID': '#28a745',
      'FAILED': '#dc3545',
      'REFUNDED': '#6c757d'
    };
    return colors[status] || '#6c757d';
  };

  if (loading) return <div>Loading order details...</div>;
  if (!order) return <div>Order not found</div>;

  return (
    <div>
      <div style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <Button variant="secondary" onClick={() => navigate('/admin/orders')}>
            ← Back to Orders
          </Button>
        </div>
        <h1 style={{ margin: 0 }}>Order #{order.orderNumber}</h1>
        <div style={{ width: '100px' }}></div>
      </div>

      {/* Order Status Cards */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '1rem',
        marginBottom: '2rem'
      }}>
        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <h3 style={{ marginBottom: '0.5rem', color: '#666' }}>Order Status</h3>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1rem' }}>
            <span style={{
              padding: '0.5rem 1rem',
              backgroundColor: getStatusColor(order.status),
              color: 'white',
              borderRadius: '20px',
              fontWeight: 'bold'
            }}>
              {order.status}
            </span>
            <select
              onChange={(e) => handleStatusUpdate(e.target.value)}
              disabled={updating}
              style={{
                padding: '0.5rem',
                borderRadius: '4px',
                border: '1px solid #ddd'
              }}
            >
              <option value="">Update Status</option>
              <option value="PENDING">Pending</option>
              <option value="PROCESSING">Processing</option>
              <option value="SHIPPED">Shipped</option>
              <option value="DELIVERED">Delivered</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
        </div>

        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <h3 style={{ marginBottom: '0.5rem', color: '#666' }}>Payment Status</h3>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1rem' }}>
            <span style={{
              padding: '0.5rem 1rem',
              backgroundColor: getPaymentStatusColor(order.paymentStatus),
              color: 'white',
              borderRadius: '20px',
              fontWeight: 'bold'
            }}>
              {order.paymentStatus}
            </span>
            <select
              onChange={(e) => handlePaymentUpdate(e.target.value)}
              disabled={updating}
              style={{
                padding: '0.5rem',
                borderRadius: '4px',
                border: '1px solid #ddd'
              }}
            >
              <option value="">Update Payment</option>
              <option value="PENDING">Pending</option>
              <option value="PAID">Paid</option>
              <option value="FAILED">Failed</option>
              <option value="REFUNDED">Refunded</option>
            </select>
          </div>
        </div>

        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <h3 style={{ marginBottom: '0.5rem', color: '#666' }}>Tracking Number</h3>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem' }}>
            <input
              type="text"
              value={trackingInput}
              onChange={(e) => setTrackingInput(e.target.value)}
              placeholder="Enter tracking number"
              style={{
                flex: 1,
                padding: '0.5rem',
                borderRadius: '4px',
                border: '1px solid #ddd'
              }}
            />
            <Button 
              variant="primary" 
              onClick={handleTrackingUpdate}
              disabled={updating || !trackingInput}
            >
              Update
            </Button>
          </div>
        </div>
      </div>

      {/* Customer Information */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: '1fr 1fr',
        gap: '2rem',
        marginBottom: '2rem'
      }}>
        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px'
        }}>
          <h3 style={{ marginBottom: '1rem' }}>Customer Information</h3>
          <p><strong>Name:</strong> {order.customerName || `${order.shippingAddress?.firstName} ${order.shippingAddress?.lastName}`}</p>
          <p><strong>Email:</strong> {order.customerEmail || order.shippingAddress?.email}</p>
          <p><strong>Phone:</strong> {order.shippingAddress?.phone}</p>
        </div>

        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px'
        }}>
          <h3 style={{ marginBottom: '1rem' }}>Shipping Address</h3>
          <p>{order.shippingAddress?.firstName} {order.shippingAddress?.lastName}</p>
          <p>{order.shippingAddress?.address}</p>
          <p>{order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.postalCode}</p>
          <p>{order.shippingAddress?.country}</p>
        </div>
      </div>

      {/* Order Items */}
      <div style={{
        padding: '1.5rem',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px',
        marginBottom: '2rem'
      }}>
        <h3 style={{ marginBottom: '1rem' }}>Order Items</h3>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#e9ecef' }}>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Product</th>
              <th style={{ padding: '0.75rem', textAlign: 'right' }}>Price</th>
              <th style={{ padding: '0.75rem', textAlign: 'center' }}>Quantity</th>
              <th style={{ padding: '0.75rem', textAlign: 'right' }}>Total</th>
            </tr>
          </thead>
          <tbody>
            {order.items?.map((item, index) => (
              <tr key={index} style={{ borderBottom: '1px solid #dee2e6' }}>
                <td style={{ padding: '0.75rem' }}>
                  <Link to={`/admin/products/edit/${item.productId}`}>
                    {item.productName}
                  </Link>
                </td>
                <td style={{ padding: '0.75rem', textAlign: 'right' }}>${item.price?.toFixed(2)}</td>
                <td style={{ padding: '0.75rem', textAlign: 'center' }}>{item.quantity}</td>
                <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold' }}>
                  ${(item.price * item.quantity).toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold' }}>
                Subtotal:
              </td>
              <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold' }}>
                ${order.subtotal?.toFixed(2)}
              </td>
            </tr>
            <tr>
              <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold' }}>
                Shipping:
              </td>
              <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold' }}>
                ${order.shippingCost?.toFixed(2) || '0.00'}
              </td>
            </tr>
            <tr>
              <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold', fontSize: '1.1rem' }}>
                Total:
              </td>
              <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold', fontSize: '1.1rem', color: '#007bff' }}>
                ${order.totalAmount?.toFixed(2)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      {/* Order Timeline */}
      {order.statusHistory && order.statusHistory.length > 0 && (
        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '8px'
        }}>
          <h3 style={{ marginBottom: '1rem' }}>Order Timeline</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            {order.statusHistory.map((history, index) => (
              <div key={index} style={{
                display: 'flex',
                gap: '1rem',
                padding: '0.5rem',
                backgroundColor: index === 0 ? '#fff3cd' : 'transparent',
                borderRadius: '4px'
              }}>
                <span style={{ minWidth: '120px', color: '#666' }}>
                  {new Date(history.timestamp).toLocaleString()}
                </span>
                <span style={{
                  padding: '0.25rem 0.5rem',
                  backgroundColor: getStatusColor(history.status),
                  color: 'white',
                  borderRadius: '4px',
                  fontSize: '0.875rem'
                }}>
                  {history.status}
                </span>
                {history.comment && (
                  <span style={{ color: '#666' }}>- {history.comment}</span>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderDetailsPage;