import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useCart } from '../../hooks/useCart';

const Navbar = () => {
  const { user, logout, isAuthenticated, isAdmin } = useAuth();
  const { cart } = useCart();

  const cartItemsCount = cart?.items?.length || 0;

  return (
    <nav style={{
      backgroundColor: '#343a40',
      padding: '1rem',
      color: 'white'
    }}>
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <Link to="/" style={{ color: 'white', textDecoration: 'none', fontSize: '1.5rem' }}>
          Hobby Shop
        </Link>
        
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <Link to="/products" style={{ color: 'white', textDecoration: 'none' }}>
            Products
          </Link>
          
          {isAdmin() && (
            <div style={{ display: 'flex', gap: '1rem' }}>
              <Link to="/admin/dashboard" style={{ color: 'white', textDecoration: 'none' }}>
                Dashboard
              </Link>
              <Link to="/admin/products" style={{ color: 'white', textDecoration: 'none' }}>
                Manage Products
              </Link>
              <Link to="/admin/orders" style={{ color: 'white', textDecoration: 'none' }}>
                Manage Orders
              </Link>
            </div>
          )}
          
          <Link to="/cart" style={{ color: 'white', textDecoration: 'none' }}>
            Cart ({cartItemsCount})
          </Link>
          
          {isAuthenticated() ? (
            <>
              <Link to="/profile" style={{ color: 'white', textDecoration: 'none' }}>
                {user?.firstName || 'Profile'}
              </Link>
              <Link to="/orders" style={{ color: 'white', textDecoration: 'none' }}>
                Orders
              </Link>
              <button 
                onClick={logout}
                style={{
                  background: 'none',
                  border: '1px solid white',
                  color: 'white',
                  padding: '0.25rem 0.5rem',
                  cursor: 'pointer'
                }}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ color: 'white', textDecoration: 'none' }}>
                Login
              </Link>
              <Link to="/register" style={{ color: 'white', textDecoration: 'none' }}>
                Register
              </Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;