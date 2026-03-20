// src/admin/AdminCustomersPage/CustomerDetailsPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getCustomerById, toggleCustomerStatus } from '../../services/userService';
import { getAllOrders, getOrdersByCustomer } from '../../services/orderService';
import { Button } from '../../components/ui';

const CustomerDetailsPage = () => {
  const { customerId } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toggling, setToggling] = useState(false);

  const normalizeOrdersResponse = (payload) => {
    if (!payload) return [];
    if (Array.isArray(payload)) return payload;
    if (Array.isArray(payload.content)) return payload.content;
    if (payload.data && Array.isArray(payload.data.content)) return payload.data.content;
    if (payload.data && Array.isArray(payload.data)) return payload.data;
    return [];
  };

  const loadCustomerOrders = useCallback(async (id) => {
    try {
      console.log(`🔄 Starting order load for customer ID: ${id}`);
      const ordersData = await getOrdersByCustomer(id, 0, 50);
      console.log('🔄 Orders data received:', ordersData);
      const normalizedOrders = normalizeOrdersResponse(ordersData);
      console.log('🔄 Normalized orders:', normalizedOrders);
      setOrders(normalizedOrders);
      console.log(`✅ Successfully loaded ${normalizedOrders.length} orders`);
      return;
    } catch (orderErr) {
      console.warn('⚠️ Customer orders endpoint failed, trying fallback:', orderErr);
    }

    try {
      console.log('🔄 Using fallback: loading all orders');
      const allOrdersData = await getAllOrders(0, 500);
      console.log('🔄 All orders data:', allOrdersData);
      const allOrders = normalizeOrdersResponse(allOrdersData);
      console.log('🔄 Normalized all orders:', allOrders);
      const filteredOrders = allOrders.filter((order) => String(order.customerId) === String(id));
      console.log(`✅ Filtered to ${filteredOrders.length} orders for customer`);
      setOrders(filteredOrders);
    } catch (fallbackErr) {
      console.error('❌ Fallback order loading failed:', fallbackErr);
      setOrders([]);
    }
  }, []);

  const loadCustomerDetails = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      console.log('Loading customer details for ID:', customerId);
      
      // Load customer details
      const customerData = await getCustomerById(customerId);
      console.log('Customer data loaded:', customerData);
      setCustomer(customerData);

      await loadCustomerOrders(customerData?.id ?? customerId);
      
    } catch (error) {
      console.error('Error loading customer details:', error);
      setError('Failed to load customer details. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [customerId, loadCustomerOrders]);

  useEffect(() => {
    loadCustomerDetails();
  }, [loadCustomerDetails]);

  const handleToggleStatus = async () => {
    if (!customer) return;
    
    setToggling(true);
    try {
      const newStatus = !customer.enabled;
      console.log('Toggling customer status to:', newStatus);
      
      await toggleCustomerStatus(customerId, newStatus);
      
      // Update local state instead of reloading everything
      setCustomer({
        ...customer,
        enabled: newStatus
      });
      
    } catch (error) {
      console.error('Error toggling customer status:', error);
      alert('Failed to update customer status');
    } finally {
      setToggling(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
        <p>Loading customer details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <h2>Error</h2>
        <p>{error}</p>
        <Button variant="primary" onClick={() => navigate('/admin/customers')}>
          Back to Customers
        </Button>
      </div>
    );
  }

  if (!customer) {
    return (
      <div className="error-container">
        <h2>Customer Not Found</h2>
        <p>The customer you're looking for doesn't exist.</p>
        <Button variant="primary" onClick={() => navigate('/admin/customers')}>
          Back to Customers
        </Button>
      </div>
    );
  }

  return (
    <div className="customer-details-page">
      <div className="page-header">
        <Button variant="secondary" onClick={() => navigate('/admin/customers')}>
          ← Back to Customers
        </Button>
        <h1>Customer Details</h1>
        <div style={{ width: '100px' }}></div>
      </div>

      {/* Customer Info Card */}
      <div className="customer-info-card">
        <div className="customer-info-header">
          <h2>{customer.firstName} {customer.lastName}</h2>
          <div className="customer-status-toggle">
            <div className="admin-toggle-container">
              <label className="admin-toggle-switch">
                <span className="visually-hidden">Toggle customer active status</span>
                <input
                  type="checkbox"
                  checked={customer.enabled}
                  onChange={handleToggleStatus}
                  disabled={toggling}
                  className="admin-toggle-input"
                />
                <span className="admin-toggle-slider"></span>
              </label>
              <span className="admin-toggle-label">
                {customer.enabled ? (
                  <>
                    <span>Active</span>
                    <span className="active-checkmark" aria-hidden="true">✓</span>
                  </>
                ) : (
                  <>
                    <span>Inactive</span>
                    <span className="inactive-mark" aria-hidden="true">•</span>
                  </>
                )}
              </span>
            </div>
          </div>
        </div>

        <div className="customer-info-grid">
          <div className="info-group">
            <p className="info-label">Email</p>
            <p>{customer.email || 'Not provided'}</p>
          </div>
          <div className="info-group">
            <p className="info-label">Phone</p>
            <p>{customer.phone || 'Not provided'}</p>
          </div>
          <div className="info-group">
            <p className="info-label">Role</p>
            <p>
              <span className={`role-badge ${customer.role === 'ADMIN' ? 'admin' : 'user'}`}>
                {customer.role || 'USER'}
              </span>
            </p>
          </div>
          <div className="info-group">
            <p className="info-label">Customer ID</p>
            <p>{customer.id}</p>
          </div>
          <div className="info-group">
            <p className="info-label">Customer Since</p>
            <p>{customer.createdAt ? new Date(customer.createdAt).toLocaleDateString() : 'N/A'}</p>
          </div>
          {customer.lastLogin && (
            <div className="info-group">
              <p className="info-label">Last Login</p>
              <p>{new Date(customer.lastLogin).toLocaleString()}</p>
            </div>
          )}
        </div>

        {(customer.address || customer.city || customer.state || customer.postalCode || customer.country) && (
          <div className="address-section">
            <h3>Address</h3>
            <p>
              {customer.address && <>{customer.address}<br /></>}
              {customer.city && customer.state && customer.postalCode && 
                <>{customer.city}, {customer.state} {customer.postalCode}<br /></>
              }
              {customer.country && <>{customer.country}</>}
            </p>
          </div>
        )}
      </div>

      {/* Orders Section */}
      <h2 className="section-title">Order History</h2>
      {orders.length > 0 ? (
        <div className="orders-table-wrapper">
          <table className="orders-table">
            <thead>
              <tr>
                <th>Order #</th>
                <th>Date</th>
                <th>Status</th>
                <th>Total</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td>{order.orderNumber || 'N/A'}</td>
                  <td>{order.orderDate ? new Date(order.orderDate).toLocaleDateString() : 'N/A'}</td>
                  <td>
                    <span className={`status-badge ${(order.status || 'PENDING').toLowerCase()}`}>
                      {order.status || 'PENDING'}
                    </span>
                  </td>
                  <td>${Number(order.totalAmount || 0).toFixed(2)}</td>
                  <td>
                    <Link to={`/admin/orders/${order.id}`}>
                      <Button variant="primary" className="action-btn">View</Button>
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="no-orders-container">
          <p className="no-orders">No orders found for this customer.</p>
        </div>
      )}
    </div>
  );
};

export default CustomerDetailsPage;