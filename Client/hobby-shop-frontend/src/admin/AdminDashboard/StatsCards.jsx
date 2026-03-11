// src/admin/AdminDashboard/StatsCards.jsx
import React from 'react';

const StatsCards = ({ stats }) => {
  const cards = [
    { 
      title: 'Total Customers', 
      value: stats?.totalCustomers || 0, 
      color: '#007bff',
      icon: '👥',
      gradient: 'linear-gradient(135deg, #0066cc, #0099ff)'
    },
    { 
      title: 'Active Customers', 
      value: stats?.activeCustomers || 0, 
      color: '#28a745',
      icon: '✅',
      gradient: 'linear-gradient(135deg, #28a745, #34ce57)'
    },
    { 
      title: 'Inactive Customers', 
      value: stats?.inactiveCustomers || 0, 
      color: '#ffc107',
      icon: '⏸️',
      gradient: 'linear-gradient(135deg, #ffc107, #ffdb58)'
    },
    { 
      title: 'Total Products', 
      value: stats?.totalProducts || 0, 
      color: '#17a2b8',
      icon: '📦',
      gradient: 'linear-gradient(135deg, #17a2b8, #00d9ff)'
    },
    { 
      title: 'Total Orders', 
      value: stats?.totalOrders || 0, 
      color: '#dc3545',
      icon: '🛒',
      gradient: 'linear-gradient(135deg, #dc3545, #ff4d5e)'
    },
    { 
      title: 'Total Revenue', 
      value: `$${(stats?.totalRevenue || 0).toFixed(2)}`, 
      color: '#20c997',
      icon: '💰',
      gradient: 'linear-gradient(135deg, #20c997, #00d9ff)'
    }
  ];

  return (
    <div className="stats-grid">
      {cards.map((card, index) => (
        <div
          key={index}
          className="stat-card"
          style={{
            background: card.gradient,
            border: '2px solid rgba(0, 217, 255, 0.4)'
          }}
        >
          <div className="stat-icon">{card.icon}</div>
          <h3 className="stat-title">{card.title}</h3>
          <p className="stat-number">{card.value}</p>
          
          {/* Glowing effect overlay */}
          <div className="stat-glow"></div>
        </div>
      ))}
    </div>
  );
};

export default StatsCards;