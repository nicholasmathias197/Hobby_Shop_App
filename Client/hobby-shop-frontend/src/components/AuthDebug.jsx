import React from 'react';
import { useAuth } from '../hooks/useAuth';

const AuthDebug = () => {
  const { user, isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) return <div>Loading auth...</div>;

  return (
    <div style={{
      position: 'fixed',
      bottom: '10px',
      right: '10px',
      backgroundColor: 'white',
      padding: '1rem',
      border: '2px solid #333',
      borderRadius: '8px',
      zIndex: 9999,
      maxWidth: '400px',
      boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
    }}>
      <h4 style={{ margin: '0 0 0.5rem 0', color: '#333' }}>🔐 Auth Debug</h4>
      <div style={{ marginBottom: '0.5rem' }}>
        <strong>Token:</strong> {localStorage.getItem('token') ? '✅ Present' : '❌ Missing'}
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <strong>Authenticated:</strong> {isAuthenticated() ? '✅ Yes' : '❌ No'}
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <strong>Is Admin:</strong> {isAdmin() ? '✅ Yes' : '❌ No'}
      </div>
      <div style={{ marginBottom: '0.5rem' }}>
        <strong>User Object:</strong>
      </div>
      <pre style={{
        backgroundColor: '#f5f5f5',
        padding: '0.5rem',
        borderRadius: '4px',
        fontSize: '0.8rem',
        overflow: 'auto',
        maxHeight: '200px'
      }}>
        {JSON.stringify(user, null, 2)}
      </pre>
    </div>
  );
};

export default AuthDebug;