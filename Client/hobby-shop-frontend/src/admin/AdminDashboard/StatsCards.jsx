// src/admin/AdminDashboard/StatsCards.jsx
import React from 'react';

const StatsCards = ({ stats }) => {
  const cards = [
    { 
      title: 'Total Customers', 
      value: stats?.totalCustomers || 0, 
      icon: '👥',
      accent: '#00d9ff'
    },
    { 
      title: 'Active Customers', 
      value: stats?.activeCustomers || 0, 
      icon: '✅',
      accent: '#00d9ff'
    },
    { 
      title: 'Inactive Customers', 
      value: stats?.inactiveCustomers || 0, 
      icon: '⏸️',
      accent: 'rgba(0, 217, 255, 0.5)'
    },
    { 
      title: 'Total Products', 
      value: stats?.totalProducts || 0, 
      icon: '📦',
      accent: '#00d9ff'
    },
    { 
      title: 'Total Orders', 
      value: stats?.totalOrders || 0, 
      icon: '🛒',
      accent: '#00d9ff'
    },
    { 
      title: 'Total Revenue', 
      value: `$${(stats?.totalRevenue || 0).toFixed(2)}`, 
      icon: '💰',
      accent: '#00d9ff'
    }
  ];

  return (
    <div className="stats-grid">
      {cards.map((card, index) => (
        <div
          key={index}
          className="stat-card"
          style={{
            background: 'rgba(0, 0, 0, 0.8)',
            border: `2px solid rgba(0, 217, 255, 0.3)`,
            textAlign: 'center',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center'
          }}
        >
          <div className="stat-icon">{card.icon}</div>
          <h3 className="stat-title" style={{ color: card.accent }}>{card.title}</h3>
          <p className="stat-number" style={{ color: card.accent }}>{card.value}</p>
          <div className="stat-glow"></div>
        </div>
      ))}
    </div>
  );
};

export default StatsCards;