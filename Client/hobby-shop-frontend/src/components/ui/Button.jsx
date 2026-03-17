import React from 'react';

const Button = ({ 
  children, 
  variant = 'primary', 
  onClick, 
  disabled = false,
  type = 'button',
  fullWidth = false,
  className,
  'aria-label': ariaLabel
}) => {
  const variants = {
    primary: {
      backgroundColor: '#007bff',
      color: 'white'
    },
    secondary: {
      backgroundColor: '#6c757d',
      color: 'white'
    },
    success: {
      backgroundColor: '#28a745',
      color: 'white'
    },
    danger: {
      backgroundColor: '#dc3545',
      color: 'white'
    }
  };

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className={className}
      aria-label={ariaLabel}
      style={{
        ...variants[variant],
        border: 'none',
        padding: '0.5rem 1rem',
        borderRadius: '4px',
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.65 : 1,
        width: fullWidth ? '100%' : 'auto'
      }}
    >
      {children}
    </button>
  );
};

export default Button;