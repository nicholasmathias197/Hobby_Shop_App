// src/admin/AdminDashboard/AdminDashboard.jsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getCustomerStats, getActiveCustomers, getInactiveCustomers } from '../../services/userService';
import { getAllProducts } from '../../services/productService';
import { getAllOrders } from '../../services/orderService';
import StatsCards from './StatsCards';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    totalCustomers: 0,
    activeCustomers: 0,
    inactiveCustomers: 0,
    totalProducts: 0,
    activeProducts: 0,
    inactiveProducts: 0,
    totalOrders: 0,
    pendingOrders: 0,
    processingOrders: 0,
    shippedOrders: 0,
    deliveredOrders: 0,
    cancelledOrders: 0,
    totalRevenue: 0,
    averageOrderValue: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDashboardStats();
  }, []);

  const loadDashboardStats = async () => {
    try {
      setLoading(true);
      
      // Fetch all data in parallel
      const [customerStats, activeCustomers, inactiveCustomers, productsData, ordersData] = await Promise.all([
        getCustomerStats().catch(err => {
          console.error('Error fetching customer stats:', err);
          return { totalCustomers: 0 };
        }),
        getActiveCustomers().catch(err => {
          console.error('Error fetching active customers:', err);
          return [];
        }),
        getInactiveCustomers().catch(err => {
          console.error('Error fetching inactive customers:', err);
          return [];
        }),
        getAllProducts(0, 1).catch(err => {
          console.error('Error fetching products:', err);
          return { totalElements: 0, content: [] };
        }),
        getAllOrders(0, 1).catch(err => {
          console.error('Error fetching orders:', err);
          return { totalElements: 0, content: [] };
        })
      ]);

      // Fetch all orders for detailed stats
      const allOrders = await getAllOrders(0, 100).catch(err => {
        console.error('Error fetching all orders:', err);
        return { content: [] };
      });

      // Calculate order statistics
      const orders = allOrders.content || [];
      const totalRevenue = orders.reduce((sum, order) => sum + (order.totalAmount || 0), 0);
      const averageOrderValue = orders.length > 0 ? totalRevenue / orders.length : 0;

      // Count orders by status
      const pendingOrders = orders.filter(o => o.status === 'PENDING').length;
      const processingOrders = orders.filter(o => o.status === 'PROCESSING').length;
      const shippedOrders = orders.filter(o => o.status === 'SHIPPED').length;
      const deliveredOrders = orders.filter(o => o.status === 'DELIVERED').length;
      const cancelledOrders = orders.filter(o => o.status === 'CANCELLED').length;

      setStats({
        // Customer stats
        totalCustomers: customerStats?.totalCustomers || 0,
        activeCustomers: Array.isArray(activeCustomers) ? activeCustomers.length : 0,
        inactiveCustomers: Array.isArray(inactiveCustomers) ? inactiveCustomers.length : 0,
        
        // Product stats
        totalProducts: productsData?.totalElements || 0,
        activeProducts: productsData?.content?.filter(p => p.isActive).length || 0,
        inactiveProducts: productsData?.content?.filter(p => !p.isActive).length || 0,
        
        // Order stats
        totalOrders: ordersData?.totalElements || 0,
        pendingOrders,
        processingOrders,
        shippedOrders,
        deliveredOrders,
        cancelledOrders,
        totalRevenue,
        averageOrderValue
      });
      
      setError(null);
    } catch (error) {
      console.error('Error loading dashboard stats:', error);
      setError('Failed to load dashboard statistics');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <div className="spinner"></div>
        <p>Loading dashboard statistics...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <h2 style={{ color: '#dc3545', marginBottom: '1rem' }}>Error</h2>
        <p style={{ marginBottom: '2rem' }}>{error}</p>
        <button 
          onClick={loadDashboardStats}
          className="btn btn-primary"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div>
      <h1>Admin Dashboard</h1>
      
      {/* Stats Cards */}
      <StatsCards stats={stats} />

      {/* Order Status Breakdown */}
      <div className="grid grid-5" style={{ marginBottom: '2rem' }}>
        <div className="dashboard-card" style={{ backgroundColor: 'rgba(0, 217, 255, 0.3)' }}>
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#f3dd9f' }}>Pending</h4>
          <p className="stat-value">{stats.pendingOrders}</p>
        </div>
        <div className="dashboard-card" style={{ backgroundColor: 'rgba(0, 217, 255, 0.3)' }}>
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#69aaf0' }}>Processing</h4>
          <p className="stat-value">{stats.processingOrders}</p>
        </div>
        <div className="dashboard-card" style={{ backgroundColor: 'rgba(0, 217, 255, 0.3)' }}>
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#75e68f' }}>Shipped</h4>
          <p className="stat-value">{stats.shippedOrders}</p>
        </div>
        <div className="dashboard-card" style={{ backgroundColor: 'rgba(0, 217, 255, 0.3)' }}>
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#47cbe2' }}>Delivered</h4>
          <p className="stat-value">{stats.deliveredOrders}</p>
        </div>
        <div className="dashboard-card" style={{ backgroundColor: 'rgba(0, 217, 255, 0.3)' }}>
          <h4 style={{ margin: '0 0 0.5rem 0', color: '#e64e5d' }}>Cancelled</h4>
          <p className="stat-value">{stats.cancelledOrders}</p>
        </div>
      </div>

      {/* Revenue Stats */}
      <div className="grid grid-2" style={{ marginBottom: '2rem' }}>
        <div className="dashboard-card" style={{ 
          background: 'linear-gradient(135deg, #28a745, #20c997)',
          color: 'white',
          border: '2px solid rgba(0, 217, 255, 0.4)'
        }}>
          <h3>Total Revenue</h3>
          <p className="stat-value">${stats.totalRevenue.toFixed(2)}</p>
        </div>
        <div className="dashboard-card" style={{ 
          background: 'linear-gradient(135deg, #17a2b8, #00d9ff)',
          color: 'white',
          border: '2px solid rgba(0, 217, 255, 0.4)'
        }}>
          <h3>Average Order Value</h3>
          <p className="stat-value">${stats.averageOrderValue.toFixed(2)}</p>
        </div>
      </div>

      {/* Quick Action Cards */}
      <div className="grid grid-4" style={{ marginTop: '2rem' }}>
        <div className="dashboard-card">
          <h3>Products</h3>
          <p className="stat-value">{stats.totalProducts}</p>
          <Link to="/admin/products">
            <button className="btn btn-primary" style={{ width: '100%' }}>
              Manage Products
            </button>
          </Link>
        </div>

        <div className="dashboard-card">
          <h3>Orders</h3>
          <p className="stat-value">{stats.totalOrders}</p>
          <Link to="/admin/orders">
            <button className="btn btn-success" style={{ width: '100%' }}>
              Manage Orders
            </button>
          </Link>
        </div>

        <div className="dashboard-card">
          <h3>Customers</h3>
          <p className="stat-value">{stats.totalCustomers}</p>
          <Link to="/admin/customers">
            <button className="btn btn-primary" style={{ 
              width: '100%',
              background: 'linear-gradient(135deg, #17a2b8, #00d9ff)'
            }}>
              Manage Customers
            </button>
          </Link>
        </div>

        <div className="dashboard-card">
          <h3>Refresh Data</h3>
          <button 
            onClick={loadDashboardStats}
            className="btn btn-secondary"
            style={{ width: '100%' }}
          >
            Refresh Stats
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;