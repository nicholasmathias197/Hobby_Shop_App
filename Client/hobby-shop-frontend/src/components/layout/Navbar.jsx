// src/components/layout/Navbar.jsx (Dropdown Version)
import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { useCart } from '../../hooks/useCart';
import companyLogo from '../../assets/images/logo.png';
import PillNav from './PillNav';
import '../../assets/styles/layout/navigation.css';

const NAV_ITEMS = [
  { href: '/', label: 'Home' },
  { href: '/products', label: 'Products' },
  { href: '/category/1', label: 'Gundam Models' },
  { href: '/category/3', label: 'Tools' },
  { href: '/category/2', label: 'Paints' }
];

const Navbar = () => {
  const { user, logout, isAuthenticated, isAdmin } = useAuth();
  const { cart } = useCart();
  const { pathname } = useLocation();
  const [showAdminMenu, setShowAdminMenu] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const cartItemsCount = cart?.items?.length || 0;

  return (
    <nav className="navbar" aria-label="Main navigation" style={{
      backgroundColor: '#343a40',
      padding: '0.75rem 2rem',
      color: 'white',
      position: 'sticky',
      top: 0,
      zIndex: 1000,
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
      width: '100%'
    }}>
      <div style={{
        maxWidth: '1400px',
        margin: '0 auto',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        width: '100%',
        flexWrap: 'wrap'
      }}>
        {/* Logo and Title - Left Side */}
        <Link to="/" style={{ 
          color: 'white', 
          textDecoration: 'none', 
          display: 'flex',
          alignItems: 'center',
          gap: '1rem',
          flexShrink: 0
        }}>
          <img 
            src={companyLogo} 
            alt="U197 Hobbies" 
            style={{ 
              height: '50px',
              width: '50px',
              borderRadius: '50%',
              objectFit: 'cover',
              border: '2px solid #00d9ff',
              boxShadow: '0 0 20px rgba(0, 217, 255, 0.6)'
            }} 
          />
          <span style={{ 
            fontSize: '2.2rem', 
            fontWeight: '800',
            background: 'linear-gradient(to right, #00d9ff, #0099ff)',
            WebkitBackgroundClip: 'text',
            backgroundClip: 'text',
            color: 'transparent',
            letterSpacing: '1px'
          }}>U197 Hobbies</span>
        </Link>
        
        {/* Hamburger button - mobile only */}
        <button
          className="nav-hamburger"
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label={mobileOpen ? 'Close menu' : 'Open menu'}
          aria-expanded={mobileOpen}
          aria-controls="nav-links"
          style={{
            display: 'none',
            background: 'none',
            border: 'none',
            color: 'white',
            fontSize: '1.8rem',
            cursor: 'pointer',
            padding: '0.25rem 0.5rem'
          }}
        >
          {mobileOpen ? '✕' : '☰'}
        </button>

        {/* Navigation Links - Center/Right */}
        <div id="nav-links" className={`nav-links${mobileOpen ? ' nav-links--open' : ''}`} style={{ 
          gap: '2rem', 
          alignItems: 'center',
          flexWrap: 'wrap',
          justifyContent: 'flex-end',
          flex: 1
        }}>
          {/* Main Navigation Links */}
          <PillNav items={NAV_ITEMS} activeHref={pathname} />

          {/* Mobile-only plain nav links (hidden on desktop via CSS) */}
          {NAV_ITEMS.map(item => (
            <Link
              key={item.href}
              to={item.href}
              className="nav-mobile-link"
              onClick={() => setMobileOpen(false)}
              style={{ color: 'white', textDecoration: 'none', fontSize: '1rem', padding: '0.75rem 1rem', width: '100%', borderBottom: '1px solid rgba(0,217,255,0.1)' }}
            >
              {item.label}
            </Link>
          ))}
          
          {/* Search Icon */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setShowSearch(!showSearch)}
              aria-label={showSearch ? 'Close search' : 'Open search'}
              aria-expanded={showSearch}
              style={{
                background: 'none',
                border: 'none',
                color: 'white',
                fontSize: '1.3rem',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                padding: '0.5rem',
                transition: 'transform 0.2s ease'
              }}
              onMouseEnter={(e) => e.target.style.transform = 'scale(1.1)'}
              onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
            >
              🔍
            </button>
            
            {/* Search Dropdown */}
            {showSearch && (
              <div style={{
                position: 'absolute',
                top: '100%',
                right: 0,
                marginTop: '0.5rem',
                backgroundColor: 'white',
                borderRadius: '8px',
                boxShadow: '0 4px 15px rgba(0,0,0,0.2)',
                minWidth: '300px',
                zIndex: 1001,
                padding: '0.75rem'
              }}>
                <input
                  type="text"
                  placeholder="Search products..."
                  aria-label="Search products"
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '2px solid #00d9ff',
                    borderRadius: '6px',
                    fontSize: '1rem',
                    outline: 'none'
                  }}
                  autoFocus
                />
              </div>
            )}
          </div>
          
          {/* Cart Icon */}
          <Link to="/cart" aria-label={`Shopping cart, ${cartItemsCount} ${cartItemsCount === 1 ? 'item' : 'items'}`} style={{ 
            color: 'white', 
            textDecoration: 'none',
            display: 'flex',
            alignItems: 'center',
            gap: '0.25rem',
            fontSize: '1.3rem',
            padding: '0.5rem',
            position: 'relative',
            transition: 'transform 0.2s ease'
          }}
          onMouseEnter={(e) => e.target.style.transform = 'scale(1.1)'}
          onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
          >
            🛒
            {cartItemsCount > 0 && (
              <span style={{
                position: 'absolute',
                top: '-5px',
                right: '-5px',
                backgroundColor: '#dc3545',
                color: 'white',
                borderRadius: '50%',
                padding: '0.15rem 0.4rem',
                fontSize: '0.7rem',
                fontWeight: 'bold',
                minWidth: '18px',
                textAlign: 'center'
              }}>
                {cartItemsCount}
              </span>
            )}
          </Link>
          
          {/* Profile/Login Section */}
          {isAuthenticated() ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <Link to="/profile" style={{ 
                color: 'white', 
                textDecoration: 'none',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                fontSize: '1.1rem',
                padding: '0.5rem',
                borderRadius: '4px',
                transition: 'background-color 0.3s ease'
              }}
              onMouseEnter={(e) => e.target.style.backgroundColor = 'rgba(255,255,255,0.1)'}
              onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
              >
                <span style={{ fontSize: '1.3rem' }}>👤</span>
                {user?.firstName || 'Profile'}
              </Link>
              <Link to="/orders" style={{ 
                color: 'white', 
                textDecoration: 'none', 
                fontSize: '1.1rem',
                padding: '0.5rem',
                borderRadius: '4px',
                transition: 'background-color 0.3s ease'
              }}
              onMouseEnter={(e) => e.target.style.backgroundColor = 'rgba(255,255,255,0.1)'}
              onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
              >
                Orders
              </Link>
              <button 
                onClick={logout}
                style={{
                  background: 'none',
                  border: '2px solid #dc3545',
                  color: '#dc3545',
                  padding: '0.4rem 1rem',
                  borderRadius: '4px',
                  cursor: 'pointer',
                  fontSize: '1rem',
                  fontWeight: '600',
                  transition: 'all 0.3s ease'
                }}
                onMouseEnter={(e) => {
                  e.target.style.backgroundColor = '#dc3545';
                  e.target.style.color = 'white';
                }}
                onMouseLeave={(e) => {
                  e.target.style.backgroundColor = 'transparent';
                  e.target.style.color = '#dc3545';
                }}
              >
                Logout
              </button>
            </div>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <Link to="/login" style={{ 
                backgroundColor: '#007bff',
                color: 'white',
                padding: '0.5rem 1.5rem',
                borderRadius: '4px',
                textDecoration: 'none',
                fontSize: '1.1rem',
                fontWeight: '600',
                transition: 'all 0.3s ease'
              }}
              onMouseEnter={(e) => {
                e.target.style.backgroundColor = '#0056b3';
                e.target.style.transform = 'translateY(-2px)';
                e.target.style.boxShadow = '0 4px 8px rgba(0,0,0,0.2)';
              }}
              onMouseLeave={(e) => {
                e.target.style.backgroundColor = '#007bff';
                e.target.style.transform = 'translateY(0)';
                e.target.style.boxShadow = 'none';
              }}
              >
                Login
              </Link>
              <Link to="/register" style={{ 
                color: 'white', 
                textDecoration: 'none', 
                fontSize: '1.1rem',
                padding: '0.5rem',
                borderRadius: '4px',
                transition: 'background-color 0.3s ease'
              }}
              onMouseEnter={(e) => e.target.style.backgroundColor = 'rgba(255,255,255,0.1)'}
              onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
              >
                Register
              </Link>
            </div>
          )}
          
          {/* Admin Dropdown */}
          {isAdmin() && (
            <div style={{ position: 'relative' }}>
              <button
                onClick={() => setShowAdminMenu(!showAdminMenu)}
                aria-label="Admin menu"
                aria-expanded={showAdminMenu}
                aria-haspopup="true"
                style={{
                  backgroundColor: '#28a745',
                  color: 'white',
                  padding: '0.5rem 1rem',
                  borderRadius: '4px',
                  border: 'none',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                  fontWeight: '600',
                  fontSize: '1rem',
                  transition: 'all 0.3s ease'
                }}
                onMouseEnter={(e) => {
                  e.target.style.backgroundColor = '#218838';
                  e.target.style.transform = 'translateY(-2px)';
                  e.target.style.boxShadow = '0 4px 8px rgba(0,0,0,0.2)';
                }}
                onMouseLeave={(e) => {
                  e.target.style.backgroundColor = '#28a745';
                  e.target.style.transform = 'translateY(0)';
                  e.target.style.boxShadow = 'none';
                }}
              >
                <span>👑</span>
                Admin {showAdminMenu ? '▲' : '▼'}
              </button>
              
              {showAdminMenu && (
                <div style={{
                  position: 'absolute',
                  top: '100%',
                  right: 0,
                  marginTop: '0.5rem',
                  backgroundColor: 'white',
                  borderRadius: '8px',
                  boxShadow: '0 4px 15px rgba(0,0,0,0.2)',
                  minWidth: '220px',
                  zIndex: 1001
                }}>
                  {[
                    { to: '/admin/dashboard', icon: '📊', label: 'Dashboard' },
                    { to: '/admin/products', icon: '📦', label: 'Products' },
                    { to: '/admin/brands', icon: '🏷️', label: 'Brands' },
                    { to: '/admin/categories', icon: '📂', label: 'Categories' },
                    { to: '/admin/orders', icon: '🛒', label: 'Orders' },
                    { to: '/admin/customers', icon: '👥', label: 'Customers' }
                  ].map((item, index) => (
                    <Link
                      key={index}
                      to={item.to}
                      style={{
                        display: 'block',
                        padding: '0.75rem 1rem',
                        color: '#333',
                        textDecoration: 'none',
                        borderBottom: index < 5 ? '1px solid #eee' : 'none',
                        fontSize: '0.95rem',
                        transition: 'background-color 0.3s ease'
                      }}
                      onMouseEnter={(e) => e.target.style.backgroundColor = '#f8f9fa'}
                      onMouseLeave={(e) => e.target.style.backgroundColor = 'white'}
                      onClick={() => setShowAdminMenu(false)}
                    >
                      <span style={{ marginRight: '0.5rem' }}>{item.icon}</span>
                      {item.label}
                    </Link>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;