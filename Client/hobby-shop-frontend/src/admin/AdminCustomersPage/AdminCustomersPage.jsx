// src/admin/AdminCustomersPage/AdminCustomersPage.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllCustomers, toggleCustomerStatus } from '../../services/userService';
import { Button } from '../../components/ui';

const AdminCustomersPage = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [activeTab, setActiveTab] = useState('active'); // 'active' or 'inactive'

  useEffect(() => {
    loadCustomers();
  }, [currentPage]);

  const loadCustomers = async () => {
    try {
      const data = await getAllCustomers(currentPage, 10);
      console.log('Customers data:', data); // Debug log
      setCustomers(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (error) {
      console.error('Error loading customers:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (customer) => {
    try {
      const newStatus = !customer.enabled;
      await toggleCustomerStatus(customer.id, newStatus);
      loadCustomers();
    } catch (error) {
      console.error('Error toggling customer status:', error);
      alert('Failed to update customer status');
    }
  };

  // Filter customers based on active tab
  const displayCustomers = customers.filter(customer => 
    activeTab === 'active' ? customer.enabled : !customer.enabled
  );

  const activeCount = customers.filter(c => c.enabled).length;
  const inactiveCount = customers.filter(c => !c.enabled).length;

  if (loading) return <div>Loading customers...</div>;

  return (
    <div className="admin-customers-page">
      <div className="page-header">
        <h1>Customer Management</h1>
      </div>

      {/* Tabs with counts */}
      <div className="tabs">
        <button 
          className={`tab ${activeTab === 'active' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('active');
            setCurrentPage(0);
          }}
        >
          Active Customers 
          <span className="tab-count">{activeCount}</span>
        </button>
        <button 
          className={`tab ${activeTab === 'inactive' ? 'active' : ''}`}
          onClick={() => {
            setActiveTab('inactive');
            setCurrentPage(0);
          }}
        >
          Inactive Customers 
          <span className="tab-count">{inactiveCount}</span>
        </button>
      </div>

      {/* Results info */}
      <div className="results-info">
        Showing {displayCustomers.length} of {activeTab === 'active' ? activeCount : inactiveCount} customers
      </div>

      <div className="customer-table-wrapper">
        <table className="customer-table">
          <thead>
            <tr>
              <th className="col-id">ID</th>
              <th className="col-name">Name</th>
              <th className="col-email">Email</th>
              <th className="col-phone">Phone</th>
              <th className="col-role">Role</th>
              <th className="col-status">Status</th>
              <th className="col-joined">Joined</th>
              <th className="col-actions">Actions</th>
            </tr>
          </thead>
          <tbody>
            {displayCustomers.map(customer => (
              <tr key={customer.id}>
                <td className="col-id">{customer.id}</td>
                <td className="col-name">
                  <div className="customer-name-cell" title={`${customer.firstName} ${customer.lastName}`}>
                    {customer.firstName} {customer.lastName}
                  </div>
                </td>
                <td className="col-email">
                  <div className="customer-email-cell" title={customer.email}>
                    {customer.email}
                  </div>
                </td>
                <td className="col-phone">{customer.phone || '-'}</td>
                <td className="col-role">
                  <span className={`role-badge ${customer.role === 'ADMIN' ? 'admin' : 'user'}`}>
                    {customer.role || 'USER'}
                  </span>
                </td>
                <td className="col-status">
                  <div className="admin-toggle-container">
                    <label className="admin-toggle-switch">
                      <input
                        type="checkbox"
                        checked={customer.enabled}
                        onChange={() => handleToggleStatus(customer)}
                        className="admin-toggle-input"
                      />
                      <span className="admin-toggle-slider"></span>
                    </label>
                    <span className="admin-toggle-label">
                      {customer.enabled ? 'Active' : 'Inactive'}
                    </span>
                  </div>
                </td>
                <td className="col-joined">
                  {new Date(customer.createdAt).toLocaleDateString()}
                </td>
                <td className="col-actions">
                  <div className="action-buttons">
                    <Link to={`/admin/customers/${customer.id}`}>
                      <Button variant="primary" className="action-btn">View</Button>
                    </Link>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

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
          <span>Page {currentPage + 1} of {totalPages}</span>
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

export default AdminCustomersPage;