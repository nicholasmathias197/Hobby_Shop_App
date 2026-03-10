import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getCustomerStats, getActiveCustomers, getInactiveCustomers } from '../../services/userService';
import StatsCards from './StatsCards';

const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadStats();
  }, []);

  const loadStats = async () => {
    try {
      const [statsData, activeData, inactiveData] = await Promise.all([
        getCustomerStats(),
        getActiveCustomers(),
        getInactiveCustomers()
      ]);

      setStats({
        ...statsData,
        activeCustomers: activeData.length,
        inactiveCustomers: inactiveData.length
      });
    } catch (error) {
      console.error('Error loading stats:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading dashboard...</div>;

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Admin Dashboard</h1>
      
      <StatsCards stats={stats} />

      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(3, 1fr)',
        gap: '2rem'
      }}>
        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <h3 style={{ marginBottom: '1rem' }}>Products</h3>
          <Link to="/admin/products">
            <button style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              width: '100%'
            }}>
              Manage Products
            </button>
          </Link>
        </div>

        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <h3 style={{ marginBottom: '1rem' }}>Orders</h3>
          <Link to="/admin/orders">
            <button style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#28a745',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              width: '100%'
            }}>
              Manage Orders
            </button>
          </Link>
        </div>

        <div style={{
          padding: '1.5rem',
          backgroundColor: '#f8f9fa',
          borderRadius: '4px'
        }}>
          <h3 style={{ marginBottom: '1rem' }}>Customers</h3>
          <Link to="/admin/customers">
            <button style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#17a2b8',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer',
              width: '100%'
            }}>
              Manage Customers
            </button>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;