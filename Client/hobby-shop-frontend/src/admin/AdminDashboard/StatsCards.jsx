import React from 'react';

const StatsCards = ({ stats }) => {
  const cards = [
    { title: 'Total Customers', value: stats?.totalCustomers || 0, color: '#007bff' },
    { title: 'Active Customers', value: stats?.activeCustomers || 0, color: '#28a745' },
    { title: 'Inactive Customers', value: stats?.inactiveCustomers || 0, color: '#ffc107' },
    { title: 'Total Orders', value: stats?.totalOrders || 0, color: '#17a2b8' }
  ];

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: '1rem',
      marginBottom: '2rem'
    }}>
      {cards.map((card, index) => (
        <div
          key={index}
          style={{
            padding: '1.5rem',
            backgroundColor: card.color,
            color: 'white',
            borderRadius: '4px',
            textAlign: 'center'
          }}
        >
          <h3 style={{ fontSize: '0.875rem', marginBottom: '0.5rem' }}>
            {card.title}
          </h3>
          <p style={{ fontSize: '2rem', fontWeight: 'bold' }}>
            {card.value}
          </p>
        </div>
      ))}
    </div>
  );
};

export default StatsCards;