import React from 'react';

const Input = ({
  type = 'text',
  name,
  value,
  onChange,
  placeholder,
  label,
  error,
  required = false,
  ...props
}) => {
  return (
    <div style={{ marginBottom: '1rem' }}>
      {label && (
        <label style={{ display: 'block', marginBottom: '0.5rem' }}>
          {label} {required && <span style={{ color: 'red' }}>*</span>}
        </label>
      )}
      <input
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        style={{
          width: '100%',
          padding: '0.5rem',
          border: `1px solid ${error ? '#dc3545' : '#ddd'}`,
          borderRadius: '4px'
        }}
        {...props}
      />
      {error && <small style={{ color: '#dc3545' }}>{error}</small>}
    </div>
  );
};

export default Input;