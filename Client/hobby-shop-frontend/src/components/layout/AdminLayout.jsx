import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';

const AdminLayout = ({ children }) => {
  const location = useLocation();
  const { user, logout } = useAuth();

  const isActive = (path) => {
    return location.pathname.startsWith(`/admin${path}`);
  };

  const menuItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/products', label: 'Products', icon: '📦' },
    { path: '/brands', label: 'Brands', icon: '🏷️' },
    { path: '/categories', label: 'Categories', icon: '📂' },
    { path: '/orders', label: 'Orders', icon: '🛒' },
    { path: '/customers', label: 'Customers', icon: '👥' },
  ];

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      {/* Sidebar */}
      <div style={{
        width: '250px',
        backgroundColor: '#343a40',
        color: 'white',
        padding: '1rem 0'
      }}>
        <div style={{ padding: '1rem', borderBottom: '1px solid #495057' }}>
          <h3 style={{ margin: 0 }}>Admin Panel</h3>
          <p style={{ margin: '0.5rem 0 0', fontSize: '0.875rem', color: '#adb5bd' }}>
            {user?.firstName} {user?.lastName}
          </p>
        </div>
        
        <nav style={{ marginTop: '1rem' }}>
          {menuItems.map(item => (
            <Link
              key={item.path}
              to={`/admin${item.path}`}
              style={{
                display: 'block',
                padding: '0.75rem 1rem',
                color: 'white',
                textDecoration: 'none',
                backgroundColor: isActive(item.path) ? '#007bff' : 'transparent',
                transition: 'background-color 0.2s'
              }}
              onMouseEnter={(e) => {
                if (!isActive(item.path)) {
                  e.currentTarget.style.backgroundColor = '#495057';
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive(item.path)) {
                  e.currentTarget.style.backgroundColor = 'transparent';
                }
              }}
            >
              <span style={{ marginRight: '0.5rem' }}>{item.icon}</span>
              {item.label}
            </Link>
          ))}
        </nav>
        
        <div style={{ padding: '1rem', borderTop: '1px solid #495057', marginTop: 'auto' }}>
          <button
            onClick={logout}
            style={{
              width: '100%',
              padding: '0.75rem',
              backgroundColor: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div style={{ flex: 1, backgroundColor: '#f8f9fa' }}>
        {/* Top Bar */}
        <div style={{
          backgroundColor: 'white',
          padding: '1rem 2rem',
          borderBottom: '1px solid #dee2e6',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <h2 style={{ margin: 0, fontSize: '1.25rem' }}>
            {menuItems.find(item => isActive(item.path))?.label || 'Admin'}
          </h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
            <span>Welcome, {user?.firstName}!</span>
          </div>
        </div>

        {/* Page Content */}
        <div style={{ padding: '2rem' }}>
          {children}
        </div>
      </div>
    </div>
  );
};

export default AdminLayout;