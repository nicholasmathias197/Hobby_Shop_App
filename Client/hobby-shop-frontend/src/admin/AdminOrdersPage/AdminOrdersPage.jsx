// src/admin/AdminOrdersPage/AdminOrdersPage.jsx
import React, { useState, useEffect } from 'react';
import { getAllOrders, getOrdersByStatus, updateOrderStatus, updatePaymentStatus, updateTrackingNumber } from '../../services/orderService';
import OrdersTable from './OrdersTable';
import { Button } from '../../components/ui';
import { Link } from 'react-router-dom';

const AdminOrdersPage = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [showDetails, setShowDetails] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [activeTab, setActiveTab] = useState('all');
  
  // State for status counts - these persist across tab changes
  const [statusCounts, setStatusCounts] = useState({
    all: 0,
    pending: 0,
    processing: 0,
    shipped: 0,
    delivered: 0,
    cancelled: 0
  });

  // Fetch counts for all statuses on component mount
  useEffect(() => {
    fetchStatusCounts();
  }, []);

  // Load orders when tab or page changes
  useEffect(() => {
    loadOrders();
  }, [currentPage, activeTab]);

  const fetchStatusCounts = async () => {
    try {
      // Fetch first page of each status to get total counts
      const [all, pending, processing, shipped, delivered, cancelled] = await Promise.all([
        getAllOrders(0, 1),
        getOrdersByStatus('PENDING', 0, 1),
        getOrdersByStatus('PROCESSING', 0, 1),
        getOrdersByStatus('SHIPPED', 0, 1),
        getOrdersByStatus('DELIVERED', 0, 1),
        getOrdersByStatus('CANCELLED', 0, 1)
      ]);
      
      setStatusCounts({
        all: all?.totalElements || 0,
        pending: pending?.totalElements || 0,
        processing: processing?.totalElements || 0,
        shipped: shipped?.totalElements || 0,
        delivered: delivered?.totalElements || 0,
        cancelled: cancelled?.totalElements || 0
      });
    } catch (error) {
      console.error('Error fetching status counts:', error);
    }
  };

  const loadOrders = async () => {
    setLoading(true);
    try {
      let response;
      
      if (activeTab === 'all') {
        response = await getAllOrders(currentPage, 10);
      } else {
        const status = activeTab.toUpperCase();
        response = await getOrdersByStatus(status, currentPage, 10);
      }
      
      console.log('📦 Orders response:', response);
      
      setOrders(response?.content || []);
      setTotalPages(response?.totalPages || 0);
      setTotalElements(response?.totalElements || 0);
      
      // Refresh counts after loading orders to keep them accurate
      fetchStatusCounts();
      
    } catch (error) {
      console.error('❌ Error loading orders:', error);
      setOrders([]);
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
    console.log('🔍 Selected order:', order);
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

  // Helper function to safely format currency
  const formatCurrency = (value) => {
    if (value === null || value === undefined || isNaN(value)) {
      return '0.00';
    }
    return Number(value).toFixed(2);
  };

  // Helper function to parse numeric values
  const parseNumber = (value) => {
    if (value === null || value === undefined || value === '') {
      return 0;
    }
    return Number(value);
  };

  if (loading && currentPage === 0) {
    return (
      <div className="loading-container" style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '400px',
        background: 'rgba(0, 0, 0, 0.8)'
      }}>
        <div className="spinner" style={{
          border: '4px solid rgba(0, 217, 255, 0.1)',
          borderTop: '4px solid #00d9ff',
          borderRadius: '50%',
          width: '40px',
          height: '40px',
          animation: 'spin 1s linear infinite'
        }}></div>
        <p style={{ color: '#00d9ff', marginTop: '1rem' }}>Loading orders...</p>
      </div>
    );
  }

  return (
    <div className="admin-orders-page" style={{
      background: 'rgba(0, 0, 0, 0.8)',
      minHeight: '100vh',
      padding: '2rem',
      color: '#e0e1dd'
    }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
        {/* Header */}
        <div className="page-header" style={{
          marginBottom: '2rem',
          borderBottom: '2px solid rgba(0, 217, 255, 0.3)',
          paddingBottom: '1rem'
        }}>
          <h1 style={{
            color: '#00d9ff',
            textShadow: '0 0 10px rgba(0, 217, 255, 0.5)',
            fontSize: '2rem',
            margin: 0
          }}>
            Order Management
          </h1>
        </div>

        {/* Status Tabs - Using persisted statusCounts from state */}
        <div className="order-tabs" style={{
          display: 'flex',
          gap: '0.5rem',
          marginBottom: '1.5rem',
          flexWrap: 'wrap'
        }}>
          {[
            { key: 'all', label: 'All Orders' },
            { key: 'pending', label: 'Pending' },
            { key: 'processing', label: 'Processing' },
            { key: 'shipped', label: 'Shipped' },
            { key: 'delivered', label: 'Delivered' },
            { key: 'cancelled', label: 'Cancelled' }
          ].map(tab => (
            <button
              key={tab.key}
              className={`tab ${activeTab === tab.key ? 'active' : ''}`}
              onClick={() => {
                setActiveTab(tab.key);
                setCurrentPage(0);
              }}
              style={{
                padding: '0.75rem 1.5rem',
                background: activeTab === tab.key ? '#00d9ff' : 'transparent',
                border: activeTab === tab.key ? 'none' : '2px solid rgba(0, 217, 255, 0.3)',
                borderRadius: '4px',
                color: activeTab === tab.key ? '#000' : '#00d9ff',
                cursor: 'pointer',
                fontWeight: '500',
                textTransform: 'capitalize',
                transition: 'all 0.3s ease'
              }}
            >
              {tab.label}
              <span style={{
                marginLeft: '0.5rem',
                padding: '0.2rem 0.5rem',
                background: activeTab === tab.key ? 'rgba(0,0,0,0.2)' : 'rgba(0, 217, 255, 0.2)',
                borderRadius: '12px',
                fontSize: '0.8rem'
              }}>
                {statusCounts[tab.key]}
              </span>
            </button>
          ))}
        </div>

        {/* Results info */}
        <div className="results-info" style={{
          marginBottom: '1rem',
          color: '#888',
          fontSize: '0.9rem'
        }}>
          Showing {orders.length} of {statusCounts[activeTab]} {activeTab === 'all' ? 'total' : activeTab} orders
        </div>

        {/* Order Details Modal */}
        {showDetails && selectedOrder && (
          <div className="order-details-modal" style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0, 0, 0, 0.9)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            padding: '2rem'
          }}>
            <div className="modal-content" style={{
              background: 'rgba(10, 10, 20, 0.95)',
              border: '2px solid #00d9ff',
              borderRadius: '12px',
              maxWidth: '1200px',
              width: '100%',
              maxHeight: '90vh',
              overflowY: 'auto',
              padding: '2rem',
              boxShadow: '0 0 30px rgba(0, 217, 255, 0.3)'
            }}>
              {/* Modal Header */}
              <div className="modal-header" style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: '2rem',
                borderBottom: '2px solid rgba(0, 217, 255, 0.3)',
                paddingBottom: '1rem'
              }}>
                <h2 style={{ color: '#00d9ff', margin: 0 }}>
                  Order #{selectedOrder.orderNumber || selectedOrder.id}
                </h2>
                <button 
                  className="close-btn" 
                  onClick={() => setShowDetails(false)}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#00d9ff',
                    fontSize: '2rem',
                    cursor: 'pointer',
                    padding: '0 0.5rem'
                  }}
                >
                  ×
                </button>
              </div>
              
              {/* Order Details Grid */}
              <div className="order-details-grid" style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(2, 1fr)',
                gap: '1.5rem',
                marginBottom: '2rem'
              }}>
                {/* Customer Information */}
                <div className="detail-section">
                  <h3 style={{ color: '#00d9ff', marginBottom: '1rem', fontSize: '1.1rem' }}>
                    Customer Information
                  </h3>
                  <div style={{
                    background: 'rgba(0, 0, 0, 0.6)',
                    border: '2px solid rgba(0, 217, 255, 0.3)',
                    borderRadius: '12px',
                    padding: '1.5rem'
                  }}>
                    <p style={{ margin: '0 0 0.5rem 0' }}>
                      <strong style={{ color: '#888', minWidth: '80px', display: 'inline-block' }}>Name:</strong> 
                      <span style={{ color: '#e0e1dd' }}>
                        {selectedOrder.customerName || 'Guest Customer'}
                      </span>
                    </p>
                    <p style={{ margin: '0 0 0.5rem 0' }}>
                      <strong style={{ color: '#888', minWidth: '80px', display: 'inline-block' }}>Email:</strong> 
                      <span style={{ color: '#e0e1dd' }}>
                        {selectedOrder.guestEmail || selectedOrder.customerEmail || 'N/A'}
                      </span>
                    </p>
                    <p style={{ margin: 0 }}>
                      <strong style={{ color: '#888', minWidth: '80px', display: 'inline-block' }}>Customer ID:</strong> 
                      <span style={{ color: '#e0e1dd' }}>
                        {selectedOrder.customerId || 'Guest'}
                      </span>
                    </p>
                  </div>
                </div>

                {/* Shipping Address - Flattened fields from backend */}
                <div className="detail-section">
                  <h3 style={{ color: '#00d9ff', marginBottom: '1rem', fontSize: '1.1rem' }}>
                    Shipping Address
                  </h3>
                  <div style={{
                    background: 'rgba(0, 0, 0, 0.6)',
                    border: '2px solid rgba(0, 217, 255, 0.3)',
                    borderRadius: '12px',
                    padding: '1.5rem'
                  }}>
                    {selectedOrder.shippingAddress || selectedOrder.shippingCity ? (
                      <>
                        <p style={{ margin: '0 0 0.5rem 0', fontWeight: '500', color: '#00d9ff' }}>
                          {selectedOrder.customerName || 'Shipping Address'}
                        </p>
                        <p style={{ margin: '0 0 0.25rem 0', color: '#e0e1dd' }}>
                          {selectedOrder.shippingAddress || 'No street address'}
                        </p>
                        <p style={{ margin: '0 0 0.25rem 0', color: '#e0e1dd' }}>
                          {[
                            selectedOrder.shippingCity,
                            selectedOrder.shippingPostalCode
                          ].filter(Boolean).join(' ') || 'No city/zip'}
                        </p>
                        <p style={{ margin: '0 0 0.25rem 0', color: '#e0e1dd' }}>
                          {selectedOrder.shippingCountry || 'No country'}
                        </p>
                        {selectedOrder.notes && (
                          <p style={{ margin: '0.5rem 0 0 0', color: '#888', fontSize: '0.9rem', fontStyle: 'italic' }}>
                            Notes: {selectedOrder.notes}
                          </p>
                        )}
                      </>
                    ) : (
                      <p style={{ color: '#888', fontStyle: 'italic', margin: 0 }}>
                        No shipping address available
                      </p>
                    )}
                  </div>
                </div>

                {/* Order Information */}
                <div className="detail-section">
                  <h3 style={{ color: '#00d9ff', marginBottom: '1rem', fontSize: '1.1rem' }}>
                    Order Information
                  </h3>
                  <div style={{
                    background: 'rgba(0, 0, 0, 0.6)',
                    border: '2px solid rgba(0, 217, 255, 0.3)',
                    borderRadius: '12px',
                    padding: '1.5rem'
                  }}>
                    <p style={{ margin: '0 0 0.5rem 0', display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#888' }}>Date:</span>
                      <span style={{ color: '#e0e1dd' }}>
                        {selectedOrder.orderDate ? new Date(selectedOrder.orderDate).toLocaleString() : 'N/A'}
                      </span>
                    </p>
                    <p style={{ margin: '0 0 0.5rem 0', display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#888' }}>Status:</span>
                      <span className={`status-badge ${selectedOrder.status?.toLowerCase()}`} style={{
                        padding: '0.25rem 0.75rem',
                        background: selectedOrder.status === 'DELIVERED' ? '#28a745' :
                                  selectedOrder.status === 'SHIPPED' ? '#007bff' :
                                  selectedOrder.status === 'PROCESSING' ? '#17a2b8' :
                                  selectedOrder.status === 'CANCELLED' ? '#dc3545' :
                                  '#ffc107',
                        color: 'white',
                        borderRadius: '20px',
                        fontSize: '0.9rem'
                      }}>
                        {selectedOrder.status}
                      </span>
                    </p>
                    <p style={{ margin: 0, display: 'flex', justifyContent: 'space-between' }}>
                      <span style={{ color: '#888' }}>Payment:</span>
                      <span className={`payment-badge ${selectedOrder.paymentStatus?.toLowerCase()}`} style={{
                        padding: '0.25rem 0.75rem',
                        background: selectedOrder.paymentStatus === 'PAID' ? '#28a745' :
                                  selectedOrder.paymentStatus === 'FAILED' ? '#dc3545' :
                                  selectedOrder.paymentStatus === 'REFUNDED' ? '#6c757d' :
                                  '#ffc107',
                        color: 'white',
                        borderRadius: '20px',
                        fontSize: '0.9rem'
                      }}>
                        {selectedOrder.paymentStatus}
                      </span>
                    </p>
                  </div>
                </div>

                {/* Tracking Information */}
                <div className="detail-section">
                  <h3 style={{ color: '#00d9ff', marginBottom: '1rem', fontSize: '1.1rem' }}>
                    Tracking Information
                  </h3>
                  <div style={{
                    background: 'rgba(0, 0, 0, 0.6)',
                    border: '2px solid rgba(0, 217, 255, 0.3)',
                    borderRadius: '12px',
                    padding: '1.5rem'
                  }}>
                    {selectedOrder.trackingNumber ? (
                      <p style={{ margin: '0 0 1rem 0', color: '#00d9ff', fontWeight: '500' }}>
                        📦 {selectedOrder.trackingNumber}
                      </p>
                    ) : (
                      <p style={{ margin: '0 0 1rem 0', color: '#888', fontStyle: 'italic' }}>
                        No tracking number assigned
                      </p>
                    )}
                    
                    <div className="tracking-update">
                      <h4 style={{ color: '#00d9ff', fontSize: '0.9rem', margin: '0 0 0.5rem 0' }}>
                        Update Tracking Number
                      </h4>
                      <div className="tracking-input-group" style={{ display: 'flex', gap: '0.5rem' }}>
                        <input
                          type="text"
                          placeholder="Enter tracking number"
                          id={`tracking-${selectedOrder.id}`}
                          className="tracking-input"
                          style={{
                            flex: 1,
                            padding: '0.5rem',
                            background: 'rgba(0, 0, 0, 0.5)',
                            border: '1px solid #00d9ff',
                            borderRadius: '4px',
                            color: '#e0e1dd'
                          }}
                        />
                        <Button 
                          variant="primary" 
                          onClick={() => {
                            const input = document.getElementById(`tracking-${selectedOrder.id}`);
                            if (input.value.trim()) {
                              handleTrackingUpdate(selectedOrder.id, input.value);
                            }
                          }}
                          style={{
                            background: '#00d9ff',
                            border: 'none',
                            color: '#000',
                            padding: '0.5rem 1rem',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontWeight: 'bold'
                          }}
                        >
                          Update
                        </Button>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Payment Status Update */}
                <div className="detail-section full-width" style={{ gridColumn: 'span 2' }}>
                  <h3 style={{ color: '#00d9ff', marginBottom: '1rem', fontSize: '1.1rem' }}>
                    Update Payment Status
                  </h3>
                  <div style={{
                    background: 'rgba(0, 0, 0, 0.6)',
                    border: '2px solid rgba(0, 217, 255, 0.3)',
                    borderRadius: '12px',
                    padding: '1.5rem'
                  }}>
                    <select
                      onChange={(e) => e.target.value && handlePaymentUpdate(selectedOrder.id, e.target.value)}
                      className="payment-select"
                      defaultValue=""
                      style={{
                        width: '100%',
                        padding: '0.5rem',
                        background: 'rgba(0, 0, 0, 0.5)',
                        border: '1px solid #00d9ff',
                        borderRadius: '4px',
                        color: '#e0e1dd',
                        cursor: 'pointer'
                      }}
                    >
                      <option value="" disabled>Select payment status</option>
                      <option value="PENDING">Pending</option>
                      <option value="PAID">Paid</option>
                      <option value="FAILED">Failed</option>
                      <option value="REFUNDED">Refunded</option>
                    </select>
                  </div>
                </div>
              </div>

              {/* Order Items */}
              <h3 className="items-title" style={{ color: '#00d9ff', marginBottom: '1rem', fontSize: '1.1rem' }}>
                Order Items
              </h3>
              <div className="order-items-table-wrapper" style={{
                background: 'rgba(0, 0, 0, 0.6)',
                border: '2px solid rgba(0, 217, 255, 0.3)',
                borderRadius: '12px',
                padding: '1.5rem',
                marginBottom: '2rem',
                overflowX: 'auto'
              }}>
                <table className="order-items-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid rgba(0, 217, 255, 0.3)' }}>
                      <th style={{ padding: '0.75rem', textAlign: 'left', color: '#00d9ff' }}>Product</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right', color: '#00d9ff' }}>Price</th>
                      <th style={{ padding: '0.75rem', textAlign: 'center', color: '#00d9ff' }}>Quantity</th>
                      <th style={{ padding: '0.75rem', textAlign: 'right', color: '#00d9ff' }}>Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedOrder.items?.map(item => {
                      const price = parseNumber(item.pricePerUnit);
                      const quantity = parseNumber(item.quantity);
                      const total = price * quantity;
                      
                      return (
                        <tr key={item.id} style={{ borderBottom: '1px solid rgba(0, 217, 255, 0.2)' }}>
                          <td style={{ padding: '0.75rem' }}>
                            <Link 
                              to={`/admin/products/edit/${item.productId}`}
                              style={{ color: '#00d9ff', textDecoration: 'none' }}
                            >
                              {item.productName || 'Unknown Product'}
                            </Link>
                            {item.productSku && (
                              <div style={{ fontSize: '0.8rem', color: '#888' }}>SKU: {item.productSku}</div>
                            )}
                          </td>
                          <td style={{ padding: '0.75rem', textAlign: 'right', color: '#e0e1dd' }}>
                            ${formatCurrency(price)}
                          </td>
                          <td style={{ padding: '0.75rem', textAlign: 'center', color: '#e0e1dd' }}>
                            {quantity}
                          </td>
                          <td style={{ padding: '0.75rem', textAlign: 'right', color: '#00d9ff', fontWeight: 'bold' }}>
                            ${formatCurrency(total)}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                  <tfoot>
                    <tr>
                      <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', color: '#888' }}>
                        Subtotal:
                      </td>
                      <td style={{ padding: '0.75rem', textAlign: 'right', color: '#e0e1dd' }}>
                        ${formatCurrency(selectedOrder.subtotal)}
                      </td>
                    </tr>
                    <tr>
                      <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', color: '#888' }}>
                        Shipping:
                      </td>
                      <td style={{ padding: '0.75rem', textAlign: 'right', color: '#e0e1dd' }}>
                        ${formatCurrency(selectedOrder.shippingCost)}
                      </td>
                    </tr>
                    <tr>
                      <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', color: '#888' }}>
                        Tax:
                      </td>
                      <td style={{ padding: '0.75rem', textAlign: 'right', color: '#e0e1dd' }}>
                        ${formatCurrency(selectedOrder.tax)}
                      </td>
                    </tr>
                    <tr className="grand-total" style={{ borderTop: '2px solid rgba(0, 217, 255, 0.3)' }}>
                      <td colSpan="3" style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold', color: '#888' }}>
                        Total:
                      </td>
                      <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: 'bold', color: '#00d9ff', fontSize: '1.2rem' }}>
                        ${formatCurrency(selectedOrder.totalAmount)}
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>

              {/* Modal Actions */}
              <div className="modal-actions" style={{ display: 'flex', justifyContent: 'flex-end' }}>
                <Button 
                  variant="secondary" 
                  onClick={() => setShowDetails(false)}
                  style={{
                    background: 'transparent',
                    border: '2px solid #00d9ff',
                    color: '#00d9ff',
                    padding: '0.75rem 2rem',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    fontWeight: 'bold',
                    transition: 'all 0.3s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.background = 'rgba(0, 217, 255, 0.1)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.background = 'transparent';
                  }}
                >
                  Close
                </Button>
              </div>
            </div>
          </div>
        )}

        {/* Orders Table */}
        <OrdersTable
          orders={orders}
          onStatusChange={handleStatusChange}
          onViewDetails={handleViewDetails}
        />

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="pagination" style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            gap: '1rem',
            marginTop: '2rem'
          }}>
            <Button
              variant="secondary"
              onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
              disabled={currentPage === 0}
              style={{
                background: currentPage === 0 ? 'rgba(0, 217, 255, 0.1)' : 'transparent',
                border: '2px solid #00d9ff',
                color: currentPage === 0 ? '#888' : '#00d9ff',
                padding: '0.5rem 1rem',
                borderRadius: '4px',
                cursor: currentPage === 0 ? 'not-allowed' : 'pointer'
              }}
            >
              Previous
            </Button>
            <div className="page-info" style={{ color: '#e0e1dd' }}>
              <span>Page {currentPage + 1} of {totalPages}</span>
              <span className="total-items" style={{ color: '#888', marginLeft: '0.5rem' }}>
                ({statusCounts[activeTab]} total items)
              </span>
            </div>
            <Button
              variant="secondary"
              onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={currentPage === totalPages - 1}
              style={{
                background: currentPage === totalPages - 1 ? 'rgba(0, 217, 255, 0.1)' : 'transparent',
                border: '2px solid #00d9ff',
                color: currentPage === totalPages - 1 ? '#888' : '#00d9ff',
                padding: '0.5rem 1rem',
                borderRadius: '4px',
                cursor: currentPage === totalPages - 1 ? 'not-allowed' : 'pointer'
              }}
            >
              Next
            </Button>
          </div>
        )}
      </div>

      <style>{`
        @keyframes spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default AdminOrdersPage;