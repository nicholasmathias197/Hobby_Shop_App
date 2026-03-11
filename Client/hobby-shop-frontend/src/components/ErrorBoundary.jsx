// src/components/ErrorBoundary.jsx
import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    // Update state so the next render will show the fallback UI
    return { hasError: true, error };
  }

  componentDidCatch(error, info) {
    // Log the error to an error reporting service
    console.error('Error caught by boundary:', error, info);
    // You can also log to a service like Sentry here
  }

  render() {
    if (this.state.hasError) {
      // You can render any custom fallback UI
      return (
        <div style={{ padding: '2rem', textAlign: 'center' }}>
          <h1 style={{ color: '#dc3545' }}>Something went wrong</h1>
          <p>We're sorry, but there was an error loading this page.</p>
          <pre style={{ 
            background: '#f8f9fa', 
            padding: '1rem', 
            borderRadius: '4px',
            marginTop: '1rem',
            color: '#721c24'
          }}>
            {this.state.error?.toString()}
          </pre>
          <button
            onClick={() => window.location.reload()}
            style={{
              marginTop: '1rem',
              padding: '0.5rem 1rem',
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '4px',
              cursor: 'pointer'
            }}
          >
            Reload Page
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;