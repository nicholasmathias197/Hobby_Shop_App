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
    if (!newStatus) return;
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
    if (!paymentStatus) return;
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
    if (!trackingInput.trim()) return;
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

  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '400px',
        background: 'rgba(0, 0, 0, 0.8)'
      }}>
        <div className="spinner"></div>
        <p style={{ color: '#00d9ff', marginLeft: '1rem' }}>Loading order details...</p>
      </div>
    );
  }
  
  if (!order) {
    return (
      <div style={{
        padding: '2rem',
        background: 'rgba(0, 0, 0, 0.8)',
        borderRadius: '12px',
        border: '2px solid rgba(0, 217, 255, 0.3)',
        textAlign: 'center',
        color: '#e0e1dd'
      }}>
        <h2>Order not found</h2>
        <Button variant="primary" onClick={() => navigate('/admin/orders')}>
          Back to Orders
        </Button>
      </div>
    );
  }

  return (
    <div style={{
      background: 'rgba(0, 0, 0, 0.8)',
      minHeight: '100vh',
      padding: '2rem',
      color: '#e0e1dd'
    }}>
      <div style={{
        maxWidth: '1400px',
        margin: '0 auto'
      }}>
        {/* Header */}
        <div style={{
          marginBottom: '2rem',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          borderBottom: '2px solid rgba(0, 217, 255, 0.3)',
          paddingBottom: '1rem'
        }}>
          <Button 
            variant="secondary" 
            onClick={() => navigate('/admin/orders')}
            style={{
              background: 'transparent',
              border: '1px solid #00d9ff',
              color: '#00d9ff',
              padding: '0.5rem 1rem',
              borderRadius: '4px',
              cursor: 'pointer',
              transition: 'all 0.3s ease'
            }}
            onMouseEnter={(e) => {
              e.target.style.background = 'rgba(0, 217, 255, 0.1)';
            }}
            onMouseLeave={(e) => {
              e.target.style.background = 'transparent';
            }}
          >
            ← Back to Orders
          </Button>
          <h1 style={{
            margin: 0,
            color: '#00d9ff',
            textShadow: '0 0 10px rgba(0, 217, 255, 0.5)',
            fontSize: '2rem'
          }}>
            Order #{order.orderNumber}
          </h1>
          <div style={{ width: '100px' }}></div>
        </div>

        {/* Order Status Cards */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: '1rem',
          marginBottom: '2rem'
        }}>
          {/* Order Status Card */}
          <div style={{
            padding: '1.5rem',
            background: 'rgba(0, 0, 0, 0.6)',
            border: '2px solid rgba(0, 217, 255, 0.3)',
            borderRadius: '12px',
            textAlign: 'center'
          }}>
            <h3 style={{
              marginBottom: '1rem',
              color: '#00d9ff',
              fontSize: '1rem',
              textTransform: 'uppercase',
              letterSpacing: '1px'
            }}>
              Order Status
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
              <span style={{
                padding: '0.5rem 1rem',
                backgroundColor: getStatusColor(order.status),
                color: 'white',
                borderRadius: '20px',
                fontWeight: 'bold',
                minWidth: '100px'
              }}>
                {order.status}
              </span>
              <select
                onChange={(e) => handleStatusUpdate(e.target.value)}
                disabled={updating}
                value=""
                style={{
                  padding: '0.5rem',
                  borderRadius: '4px',
                  background: 'rgba(0, 0, 0, 0.5)',
                  border: '1px solid #00d9ff',
                  color: '#e0e1dd',
                  cursor: 'pointer',
                  minWidth: '140px'
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

          {/* Payment Status Card */}
          <div style={{
            padding: '1.5rem',
            background: 'rgba(0, 0, 0, 0.6)',
            border: '2px solid rgba(0, 217, 255, 0.3)',
            borderRadius: '12px',
            textAlign: 'center'
          }}>
            <h3 style={{
              marginBottom: '1rem',
              color: '#00d9ff',
              fontSize: '1rem',
              textTransform: 'uppercase',
              letterSpacing: '1px'
            }}>
              Payment Status
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '1rem', flexWrap: 'wrap' }}>
              <span style={{
                padding: '0.5rem 1rem',
                backgroundColor: getPaymentStatusColor(order.paymentStatus),
                color: 'white',
                borderRadius: '20px',
                fontWeight: 'bold',
                minWidth: '100px'
              }}>
                {order.paymentStatus}
              </span>
              <select
                onChange={(e) => handlePaymentUpdate(e.target.value)}
                disabled={updating}
                value=""
                style={{
                  padding: '0.5rem',
                  borderRadius: '4px',
                  background: 'rgba(0, 0, 0, 0.5)',
                  border: '1px solid #00d9ff',
                  color: '#e0e1dd',
                  cursor: 'pointer',
                  minWidth: '140px'
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

          {/* Tracking Number Card */}
          <div style={{
            padding: '1.5rem',
            background: 'rgba(0, 0, 0, 0.6)',
            border: '2px solid rgba(0, 217, 255, 0.3)',
            borderRadius: '12px',
            textAlign: 'center'
          }}>
            <h3 style={{
              marginBottom: '1rem',
              color: '#00d9ff',
              fontSize: '1rem',
              textTransform: 'uppercase',
              letterSpacing: '1px'
            }}>
              Tracking Number
            </h3>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
              <input
                type="text"
                value={trackingInput}
                onChange={(e) => setTrackingInput(e.target.value)}
                placeholder="Enter tracking number"
                style={{
                  flex: 1,
                  minWidth: '200px',
                  padding: '0.5rem',
                  borderRadius: '4px',
                  background: 'rgba(0, 0, 0, 0.5)',
                  border: '1px solid #00d9ff',
                  color: '#e0e1dd'
                }}
              />
              <Button 
                variant="primary" 
                onClick={handleTrackingUpdate}
                disabled={updating || !trackingInput.trim()}
                style={{
                  background: trackingInput.trim() ? '#00d9ff' : 'rgba(0, 217, 255, 0.3)',
                  border: 'none',
                  color: '#000',
                  padding: '0.5rem 1rem',
                  borderRadius: '4px',
                  cursor: trackingInput.trim() ? 'pointer' : 'not-allowed',
                  fontWeight: 'bold',
                  transition: 'all 0.3s ease'
                }}
              >
                Update
              </Button>
            </div>
            {order.trackingNumber && (
              <p style={{
                marginTop: '0.5rem',
                color: '#00d9ff',
                fontSize: '0.9rem'
              }}>
                📦 Current: {order.trackingNumber}
              </p>
            )}
          </div>
        </div>

        {/* Customer Information & Shipping Address */}
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '2rem',
          marginBottom: '2rem'
        }}>
          {/* Customer Information */}
          <div style={{
            padding: '1.5rem',
            background: 'rgba(0, 0, 0, 0.6)',
            border: '2px solid rgba(0, 217, 255, 0.3)',
            borderRadius: '12px'
          }}>
            <h3 style={{
              marginBottom: '1rem',
              color: '#00d9ff',
              borderBottom: '1px solid rgba(0, 217, 255, 0.3)',
              paddingBottom: '0.5rem'
            }}>
              Customer Information
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <p style={{ margin: 0 }}>
                <strong style={{ color: '#888', minWidth: '80px', display: 'inline-block' }}>Name:</strong> 
                <span style={{ color: '#e0e1dd' }}>{order.customerName || `${order.shippingAddress?.firstName} ${order.shippingAddress?.lastName}`}</span>
              </p>
              <p style={{ margin: 0 }}>
                <strong style={{ color: '#888', minWidth: '80px', display: 'inline-block' }}>Email:</strong> 
                <span style={{ color: '#e0e1dd' }}>{order.customerEmail || order.shippingAddress?.email}</span>
              </p>
              <p style={{ margin: 0 }}>
                <strong style={{ color: '#888', minWidth: '80px', display: 'inline-block' }}>Phone:</strong> 
                <span style={{ color: '#e0e1dd' }}>{order.shippingAddress?.phone || 'N/A'}</span>
              </p>
            </div>
          </div>

          {/* Shipping Address */}
          <div style={{
            padding: '1.5rem',
            background: 'rgba(0, 0, 0, 0.6)',
            border: '2px solid rgba(0, 217, 255, 0.3)',
            borderRadius: '12px'
          }}>
            <h3 style={{
              marginBottom: '1rem',
              color: '#00d9ff',
              borderBottom: '1px solid rgba(0, 217, 255, 0.3)',
              paddingBottom: '0.5rem'
            }}>
              Shipping Address
            </h3>
            <div style={{
              background: 'rgba(0, 0, 0, 0.3)',
              padding: '1rem',
              borderRadius: '8px',
              border: '1px solid rgba(0, 217, 255, 0.2)'
            }}>
              <p style={{ margin: '0 0 0.5rem 0', fontWeight: '500', color: '#00d9ff' }}>
                {order.shippingAddress?.firstName} {order.shippingAddress?.lastName}
              </p>
              <p style={{ margin: '0 0 0.25rem 0', color: '#e0e1dd' }}>
                {order.shippingAddress?.address}
              </p>
              <p style={{ margin: '0 0 0.25rem 0', color: '#e0e1dd' }}>
                {order.shippingAddress?.city}, {order.shippingAddress?.state} {order.shippingAddress?.postalCode}
              </p>
              <p style={{ margin: '0', color: '#e0e1dd' }}>
                {order.shippingAddress?.country}
              </p>
            </div>
          </div>
        </div>

        {/* Order Items */}
        <div style={{
          padding: '1.5rem',
          background: 'rgba(0, 0, 0, 0.6)',
          border: '2px solid rgba(0, 217, 255, 0.3)',
          borderRadius: '12px',
          marginBottom: '2rem'
        }}>
          <h3 style={{
            marginBottom: '1rem',
            color: '#00d9ff',
            borderBottom: '1px solid rgba(0, 217, 255, 0.3)',
            paddingBottom: '0.5rem'
          }}>
            Order Items
          </h3>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: 'rgba(0, 217, 255, 0.1)' }}>
                  <th style={{ padding: '0.75rem', textAlign: 'left', color: '#00d9ff' }}>Product</th>
                  <th style={{ padding: '0.75rem', textAlign: 'right', color: '#00d9ff' }}>Price</th>
                  <th style={{ padding: '0.75rem', textAlign: 'center', color: '#00d9ff' }}>Quantity</th>
                  <th style={{ padding: '0.75rem', textAlign: 'right', color: '#00d9ff' }}>Total</th>
                </tr>
              </thead>
              <tbody>
                {order.items?.map((item, index) => (
                  <tr key={index} style={{ borderBottom: '1px solid rgba(0, 217, 255, 0.2)' }}>
                    <td style={{ padding: '0.75rem' }}>
                      <Link 
                        to={`/admin/products/edit/${item.productId}`}
                        style={{ color: '#00d9ff', textDecoration: 'none' }}
                      >
                        {item.productName}
                      </Link>
                    </td>
                    <td style={{ padding: '0.75rem', textAlign: 'right', color: '#e0e1dd' }}>
                      ${item.price?.toFixed(2)}
                    </td>
                    <td style={{ padding: '0.75rem', textAlign: 'center', color: '#e0e1dd' }}>
                      {item.quantity}
                    </td>
                    <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold', color: '#00d9ff' }}>
                      ${(item.price * item.quantity).toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr>
                  <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', color: '#888' }}>
                    Subtotal:
                  </td>
                  <td style={{ padding: '0.75rem', textAlign: 'right', color: '#e0e1dd' }}>
                    ${order.subtotal?.toFixed(2)}
                  </td>
                </tr>
                <tr>
                  <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', color: '#888' }}>
                    Shipping:
                  </td>
                  <td style={{ padding: '0.75rem', textAlign: 'right', color: '#e0e1dd' }}>
                    ${order.shippingCost?.toFixed(2) || '0.00'}
                  </td>
                </tr>
                <tr>
                  <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold', color: '#888', fontSize: '1.1rem' }}>
                    Total:
                  </td>
                  <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold', fontSize: '1.1rem', color: '#00d9ff' }}>
                    ${order.totalAmount?.toFixed(2)}
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
        </div>

        {/* Order Timeline */}
        {order.statusHistory && order.statusHistory.length > 0 && (
          <div style={{
            padding: '1.5rem',
            background: 'rgba(0, 0, 0, 0.6)',
            border: '2px solid rgba(0, 217, 255, 0.3)',
            borderRadius: '12px'
          }}>
            <h3 style={{
              marginBottom: '1rem',
              color: '#00d9ff',
              borderBottom: '1px solid rgba(0, 217, 255, 0.3)',
              paddingBottom: '0.5rem'
            }}>
              Order Timeline
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {order.statusHistory.map((history, index) => (
                <div key={index} style={{
                  display: 'flex',
                  gap: '1rem',
                  padding: '0.75rem',
                  background: index === 0 ? 'rgba(255, 193, 7, 0.1)' : 'rgba(0, 0, 0, 0.3)',
                  borderRadius: '4px',
                  border: index === 0 ? '1px solid rgba(255, 193, 7, 0.3)' : '1px solid rgba(0, 217, 255, 0.1)',
                  flexWrap: 'wrap'
                }}>
                  <span style={{ minWidth: '150px', color: '#888' }}>
                    {new Date(history.timestamp).toLocaleString()}
                  </span>
                  <span style={{
                    padding: '0.25rem 0.5rem',
                    backgroundColor: getStatusColor(history.status),
                    color: 'white',
                    borderRadius: '4px',
                    fontSize: '0.875rem',
                    fontWeight: 'bold'
                  }}>
                    {history.status}
                  </span>
                  {history.comment && (
                    <span style={{ color: '#e0e1dd' }}>- {history.comment}</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderDetailsPage;