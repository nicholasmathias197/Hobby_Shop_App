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
  const [activeTab, setActiveTab] = useState('all'); // 'all', 'pending', 'processing', 'shipped', 'delivered', 'cancelled'

  useEffect(() => {
    loadOrders();
  }, [currentPage, activeTab]);

  const loadOrders = async () => {
    setLoading(true);
    try {
      let response;
      
      if (activeTab === 'all') {
        response = await getAllOrders(currentPage, 10);
      } else {
        // Get orders by status
        const status = activeTab.toUpperCase();
        response = await getOrdersByStatus(status, currentPage, 10);
      }
      
      console.log('Orders response:', response);
      
      setOrders(response?.content || []);
      setTotalPages(response?.totalPages || 0);
      setTotalElements(response?.totalElements || 0);
    } catch (error) {
      console.error('Error loading orders:', error);
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

  // Calculate counts for each status
  const statusCounts = {
    all: totalElements,
    pending: orders.filter(o => o.status === 'PENDING').length,
    processing: orders.filter(o => o.status === 'PROCESSING').length,
    shipped: orders.filter(o => o.status === 'SHIPPED').length,
    delivered: orders.filter(o => o.status === 'DELIVERED').length,
    cancelled: orders.filter(o => o.status === 'CANCELLED').length
  };

  if (loading && currentPage === 0) return <div className="loading-container"><div className="spinner"></div><p>Loading orders...</p></div>;

  return (
    <div className="admin-orders-page">
      <div className="page-header">
        <h1>Order Management</h1>
      </div>

      {/* Status Tabs */}
      <div className="order-tabs">
        <button 
          className={`tab ${activeTab === 'all' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('all');
            setCurrentPage(0);
          }}
        >
          All Orders 
          <span className="tab-count">{statusCounts.all}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'pending' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('pending');
            setCurrentPage(0);
          }}
        >
          Pending 
          <span className="tab-count">{statusCounts.pending}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'processing' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('processing');
            setCurrentPage(0);
          }}
        >
          Processing 
          <span className="tab-count">{statusCounts.processing}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'shipped' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('shipped');
            setCurrentPage(0);
          }}
        >
          Shipped 
          <span className="tab-count">{statusCounts.shipped}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'delivered' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('delivered');
            setCurrentPage(0);
          }}
        >
          Delivered 
          <span className="tab-count">{statusCounts.delivered}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'cancelled' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('cancelled');
            setCurrentPage(0);
          }}
        >
          Cancelled 
          <span className="tab-count">{statusCounts.cancelled}</span>
        </button>
      </div>

      {/* Results info */}
      <div className="results-info">
        Showing {orders.length} of {statusCounts[activeTab]} {activeTab === 'all' ? 'total' : activeTab} orders
      </div>

      {/* Order Details Modal */}
      {showDetails && selectedOrder && (
        <div className="order-details-modal">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Order #{selectedOrder.orderNumber}</h2>
              <button className="close-btn" onClick={() => setShowDetails(false)}>×</button>
            </div>
            
            <div className="order-details-grid">
              <div className="detail-section">
                <h3>Customer Information</h3>
                <p><strong>Name:</strong> {selectedOrder.customerName || `${selectedOrder.shippingAddress?.firstName} ${selectedOrder.shippingAddress?.lastName}`}</p>
                <p><strong>Email:</strong> {selectedOrder.customerEmail || selectedOrder.shippingAddress?.email}</p>
                <p><strong>Phone:</strong> {selectedOrder.shippingAddress?.phone || 'N/A'}</p>
              </div>

              <div className="detail-section">
                <h3>Shipping Address</h3>
                <p>{selectedOrder.shippingAddress?.firstName} {selectedOrder.shippingAddress?.lastName}</p>
                <p>{selectedOrder.shippingAddress?.address}</p>
                <p>{selectedOrder.shippingAddress?.city}, {selectedOrder.shippingAddress?.state} {selectedOrder.shippingAddress?.postalCode}</p>
                <p>{selectedOrder.shippingAddress?.country}</p>
              </div>

              <div className="detail-section">
                <h3>Order Information</h3>
                <p><strong>Date:</strong> {new Date(selectedOrder.orderDate).toLocaleString()}</p>
                <p><strong>Status:</strong> 
                  <span className={`status-badge ${selectedOrder.status?.toLowerCase()}`}>
                    {selectedOrder.status}
                  </span>
                </p>
                <p><strong>Payment Status:</strong> 
                  <span className={`payment-badge ${selectedOrder.paymentStatus?.toLowerCase()}`}>
                    {selectedOrder.paymentStatus}
                  </span>
                </p>
              </div>

              <div className="detail-section">
                <h3>Tracking Information</h3>
                {selectedOrder.trackingNumber ? (
                  <p><strong>Tracking #:</strong> {selectedOrder.trackingNumber}</p>
                ) : (
                  <p>No tracking number assigned</p>
                )}
                
                <div className="tracking-update">
                  <h4>Update Tracking Number</h4>
                  <div className="tracking-input-group">
                    <input
                      type="text"
                      placeholder="Enter tracking number"
                      id={`tracking-${selectedOrder.id}`}
                      className="tracking-input"
                    />
                    <Button 
                      variant="primary" 
                      onClick={() => {
                        const input = document.getElementById(`tracking-${selectedOrder.id}`);
                        handleTrackingUpdate(selectedOrder.id, input.value);
                      }}
                    >
                      Update
                    </Button>
                  </div>
                </div>
              </div>

              <div className="detail-section full-width">
                <h3>Update Payment Status</h3>
                <select
                  onChange={(e) => handlePaymentUpdate(selectedOrder.id, e.target.value)}
                  className="payment-select"
                  defaultValue={selectedOrder.paymentStatus}
                >
                  <option value="PENDING">Pending</option>
                  <option value="PAID">Paid</option>
                  <option value="FAILED">Failed</option>
                  <option value="REFUNDED">Refunded</option>
                </select>
              </div>
            </div>

            <h3 className="items-title">Order Items</h3>
            <div className="order-items-table-wrapper">
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
                  {selectedOrder.items?.map(item => (
                    <tr key={item.id}>
                      <td>
                        <Link to={`/admin/products/edit/${item.productId}`}>
                          {item.productName}
                        </Link>
                      </td>
                      <td>${item.price?.toFixed(2)}</td>
                      <td>{item.quantity}</td>
                      <td>${(item.price * item.quantity).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr>
                    <td colSpan="3" className="total-label">Subtotal:</td>
                    <td className="total-value">${selectedOrder.subtotal?.toFixed(2)}</td>
                  </tr>
                  <tr>
                    <td colSpan="3" className="total-label">Shipping:</td>
                    <td className="total-value">${selectedOrder.shippingCost?.toFixed(2) || '0.00'}</td>
                  </tr>
                  <tr>
                    <td colSpan="3" className="total-label">Tax:</td>
                    <td className="total-value">${selectedOrder.tax?.toFixed(2) || '0.00'}</td>
                  </tr>
                  <tr className="grand-total">
                    <td colSpan="3" className="total-label">Total:</td>
                    <td className="total-value">${selectedOrder.totalAmount?.toFixed(2)}</td>
                  </tr>
                </tfoot>
              </table>
            </div>

            <div className="modal-actions">
              <Button variant="secondary" onClick={() => setShowDetails(false)}>
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
        <div className="pagination">
          <Button
            variant="secondary"
            onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
            disabled={currentPage === 0}
          >
            Previous
          </Button>
          <div className="page-info">
            <span>Page {currentPage + 1} of {totalPages}</span>
            <span className="total-items">({statusCounts[activeTab]} total items)</span>
          </div>
          <Button
            variant="secondary"
            onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={currentPage === totalPages - 1}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  );
};

export default AdminOrdersPage;