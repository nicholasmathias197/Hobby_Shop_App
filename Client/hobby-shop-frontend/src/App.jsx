// src/App.jsx
import React from 'react';
import { BrowserRouter as Router } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthProvider';
import { CartProvider } from './contexts/CartProvider';
import AppRoutes from './routes/AppRoutes';
import ErrorBoundary from './components/ErrorBoundary'; // Import the error boundary
import './App.css';

function App() {
  return (
    <Router
      future={{
        v7_startTransition: true,
        v7_relativeSplatPath: true
      }}
    >
      <AuthProvider>
        <CartProvider>
          <ErrorBoundary> {/* Wrap your app with error boundary */}
            <AppRoutes />
          </ErrorBoundary>
        </CartProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;