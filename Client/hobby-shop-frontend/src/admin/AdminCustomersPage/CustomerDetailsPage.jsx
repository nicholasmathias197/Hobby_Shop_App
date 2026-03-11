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

  useEffect(() => {
    loadCustomerDetails();
  }, [customerId]);

  const loadCustomerDetails = async () => {
    try {
      const [customerData, ordersData] = await Promise.all([
        getCustomerById(customerId),
        getOrdersByCustomer(customerId, 0, 10)
      ]);
      setCustomer(customerData);
      setOrders(ordersData.content || []);
    } catch (error) {
      console.error('Error loading customer details:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async () => {
    try {
      await toggleCustomerStatus(customerId);
      loadCustomerDetails();
    } catch (error) {
      console.error('Error toggling customer status:', error);
      alert('Failed to update customer status');
    }
  };

  if (loading) return <div>Loading customer details...</div>;
  if (!customer) return <div>Customer not found</div>;

  return (
    <div>
      <div style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Button variant="secondary" onClick={() => navigate('/admin/customers')}>
          ← Back to Customers
        </Button>
        <h1 style={{ margin: 0 }}>Customer Details</h1>
        <div style={{ width: '100px' }}></div>
      </div>

      {/* Customer Info Card */}
      <div style={{
        backgroundColor: '#f8f9fa',
        padding: '2rem',
        borderRadius: '8px',
        marginBottom: '2rem'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
          <div>
            <h2>{customer.firstName} {customer.lastName}</h2>
            <p><strong>Email:</strong> {customer.email}</p>
            <p><strong>Phone:</strong> {customer.phone || 'Not provided'}</p>
            <p><strong>Role:</strong> 
              <span style={{
                marginLeft: '0.5rem',
                padding: '0.25rem 0.5rem',
                backgroundColor: customer.role === 'ADMIN' ? '#dc3545' : '#28a745',
                color: 'white',
                borderRadius: '4px',
                fontSize: '0.875rem'
              }}>
                {customer.role}
              </span>
            </p>
            <p><strong>Status:</strong>
              <span style={{
                marginLeft: '0.5rem',
                padding: '0.25rem 0.5rem',
                backgroundColor: customer.enabled ? '#28a745' : '#dc3545',
                color: 'white',
                borderRadius: '4px',
                fontSize: '0.875rem'
              }}>
                {customer.enabled ? 'Active' : 'Inactive'}
              </span>
            </p>
          </div>
          <Button 
            variant={customer.enabled ? 'warning' : 'success'}
            onClick={handleToggleStatus}
          >
            {customer.enabled ? 'Disable Account' : 'Enable Account'}
          </Button>
        </div>

        {customer.address && (
          <div style={{ marginTop: '1rem' }}>
            <h3>Address</h3>
            <p>{customer.address}</p>
            <p>{customer.city}, {customer.state} {customer.postalCode}</p>
            <p>{customer.country}</p>
          </div>
        )}

        <div style={{ marginTop: '1rem', color: '#666' }}>
          <small>Customer since: {new Date(customer.createdAt).toLocaleDateString()}</small>
          {customer.lastLogin && (
            <small style={{ marginLeft: '1rem' }}>
              Last login: {new Date(customer.lastLogin).toLocaleString()}
            </small>
          )}
        </div>
      </div>

      {/* Orders Section */}
      <h2 style={{ marginBottom: '1rem' }}>Order History</h2>
      {orders.length > 0 ? (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f8f9fa' }}>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Order #</th>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Date</th>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Status</th>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Total</th>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {orders.map(order => (
              <tr key={order.id} style={{ borderBottom: '1px solid #ddd' }}>
                <td style={{ padding: '0.75rem' }}>{order.orderNumber}</td>
                <td style={{ padding: '0.75rem' }}>
                  {new Date(order.orderDate).toLocaleDateString()}
                </td>
                <td style={{ padding: '0.75rem' }}>
                  <span style={{
                    padding: '0.25rem 0.5rem',
                    backgroundColor: 
                      order.status === 'DELIVERED' ? '#28a745' :
                      order.status === 'CANCELLED' ? '#dc3545' :
                      order.status === 'SHIPPED' ? '#007bff' : '#ffc107',
                    color: 'white',
                    borderRadius: '4px',
                    fontSize: '0.875rem'
                  }}>
                    {order.status}
                  </span>
                </td>
                <td style={{ padding: '0.75rem', fontWeight: 'bold' }}>
                  ${order.totalAmount?.toFixed(2)}
                </td>
                <td style={{ padding: '0.75rem' }}>
                  <Link to={`/admin/orders/${order.id}`}>
                    <Button variant="primary" size="small">View</Button>
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p style={{ textAlign: 'center', padding: '2rem', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
          No orders found for this customer.
        </p>
      )}
    </div>
  );
};

export default CustomerDetailsPage;