// src/admin/AdminCustomersPage/CustomerDetailsPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { getCustomerById, toggleCustomerStatus } from '../../services/userService';
import { getOrdersByCustomer } from '../../services/orderService';
import { Button } from '../../components/ui';

const CustomerDetailsPage = () => {
  const { customerId } = useParams();
  const navigate = useNavigate();
  const [customer, setCustomer] = useState(null);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [toggling, setToggling] = useState(false);

  useEffect(() => {
    loadCustomerDetails();
  }, [customerId]);

  const loadCustomerDetails = async () => {
    setLoading(true);
    setError(null);
    try {
      console.log('Loading customer details for ID:', customerId);
      
      // Load customer details
      const customerData = await getCustomerById(customerId);
      console.log('Customer data loaded:', customerData);
      setCustomer(customerData);
      
      // Try to load orders, but don't fail if orders endpoint doesn't exist
      try {
        const ordersData = await getOrdersByCustomer(customerId, 0, 10);
        console.log('Orders data loaded:', ordersData);
        setOrders(ordersData.content || []);
      } catch (orderErr) {
        console.log('Orders endpoint not available or no orders found:', orderErr);
        setOrders([]);
      }
      
    } catch (error) {
      console.error('Error loading customer details:', error);
      setError('Failed to load customer details. Please try again.');
    } finally {
      setLoading(false);
    }
  };

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
                {customer.enabled ? 'Active' : 'Inactive'}
              </span>
            </div>
          </div>
        </div>

        <div className="customer-info-grid">
          <div className="info-group">
            <label>Email</label>
            <p>{customer.email || 'Not provided'}</p>
          </div>
          <div className="info-group">
            <label>Phone</label>
            <p>{customer.phone || 'Not provided'}</p>
          </div>
          <div className="info-group">
            <label>Role</label>
            <p>
              <span className={`role-badge ${customer.role === 'ADMIN' ? 'admin' : 'user'}`}>
                {customer.role || 'USER'}
              </span>
            </p>
          </div>
          <div className="info-group">
            <label>Customer ID</label>
            <p>{customer.id}</p>
          </div>
          <div className="info-group">
            <label>Customer Since</label>
            <p>{customer.createdAt ? new Date(customer.createdAt).toLocaleDateString() : 'N/A'}</p>
          </div>
          {customer.lastLogin && (
            <div className="info-group">
              <label>Last Login</label>
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
                    <span className={`status-badge ${order.status?.toLowerCase() || 'pending'}`}>
                      {order.status || 'PENDING'}
                    </span>
                  </td>
                  <td>${order.totalAmount?.toFixed(2) || '0.00'}</td>
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