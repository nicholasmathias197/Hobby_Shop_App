import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllCustomers, toggleCustomerStatus } from '../../services/userService';
import { Button } from '../../components/ui';

const AdminCustomersPage = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCustomers();
  }, []);

  const loadCustomers = async () => {
    try {
      const data = await getAllCustomers();
      setCustomers(data.content || []);
    } catch (error) {
      console.error('Error loading customers:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (id) => {  // Removed unused currentStatus parameter
    try {
      await toggleCustomerStatus(id);
      loadCustomers();
    } catch (error) {
      console.error('Error toggling customer status:', error);
    }
  };

  if (loading) return <div>Loading customers...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Customer Management</h1>

      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr style={{ backgroundColor: '#f8f9fa' }}>
            <th style={{ padding: '1rem', textAlign: 'left' }}>ID</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Name</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Email</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Phone</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Role</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Status</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Joined</th>
            <th style={{ padding: '1rem', textAlign: 'left' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {customers.map(customer => (
            <tr key={customer.id} style={{ borderBottom: '1px solid #ddd' }}>
              <td style={{ padding: '1rem' }}>{customer.id}</td>
              <td style={{ padding: '1rem' }}>{customer.firstName} {customer.lastName}</td>
              <td style={{ padding: '1rem' }}>{customer.email}</td>
              <td style={{ padding: '1rem' }}>{customer.phone}</td>
              <td style={{ padding: '1rem' }}>
                <span style={{
                  backgroundColor: customer.role === 'ADMIN' ? '#dc3545' : '#28a745',
                  color: 'white',
                  padding: '0.25rem 0.5rem',
                  borderRadius: '4px',
                  fontSize: '0.875rem'
                }}>
                  {customer.role}
                </span>
              </td>
              <td style={{ padding: '1rem' }}>
                <span style={{
                  backgroundColor: customer.enabled ? '#28a745' : '#dc3545',
                  color: 'white',
                  padding: '0.25rem 0.5rem',
                  borderRadius: '4px',
                  fontSize: '0.875rem'
                }}>
                  {customer.enabled ? 'Active' : 'Inactive'}
                </span>
              </td>
              <td style={{ padding: '1rem' }}>
                {new Date(customer.createdAt).toLocaleDateString()}
              </td>
              <td style={{ padding: '1rem' }}>
                <Button 
                  variant={customer.enabled ? 'warning' : 'success'}
                  onClick={() => handleToggleStatus(customer.id)}  // Removed second argument
                  style={{ marginRight: '0.5rem' }}
                >
                  {customer.enabled ? 'Disable' : 'Enable'}
                </Button>
                <Link to={`/admin/customers/${customer.id}`}>
                  <Button variant="primary">View</Button>
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default AdminCustomersPage;