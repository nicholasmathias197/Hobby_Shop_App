import React from 'react';

const Footer = () => {
  return (
    <footer style={{
      backgroundColor: '#343a40',
      color: 'white',
      padding: '2rem',
      marginTop: '3rem'
    }}>
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto',
        textAlign: 'center'
      }}>
        <p>&copy; {new Date().getFullYear()} U197 Designs. All rights reserved.</p>
      </div>
    </footer>
  );
};

export default Footer;