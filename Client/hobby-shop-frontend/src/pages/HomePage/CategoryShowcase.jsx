import React from 'react';
import { Link } from 'react-router-dom';

const CategoryShowcase = ({ categories }) => {
  if (!categories || categories.length === 0) {
    return null;
  }

  return (
    <div style={{
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
      gap: '1rem'
    }}>
      {categories.map(category => (
        <Link 
          key={category.id} 
          to={`/category/${category.id}`}
          style={{ textDecoration: 'none', color: 'inherit' }}
        >
          <div style={{
            border: '1px solid #ddd',
            borderRadius: '4px',
            padding: '1rem',
            textAlign: 'center',
            transition: 'transform 0.2s, box-shadow 0.2s',
            cursor: 'pointer',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center'
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.transform = 'translateY(-4px)';
            e.currentTarget.style.boxShadow = '0 4px 8px rgba(0,0,0,0.1)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.transform = 'translateY(0)';
            e.currentTarget.style.boxShadow = 'none';
          }}
          >
            {category.imageUrl ? (
              <img 
                src={category.imageUrl} 
                alt={category.name}
                style={{ 
                  width: '80px', 
                  height: '80px', 
                  objectFit: 'cover',
                  borderRadius: '50%',
                  marginBottom: '0.5rem'
                }}
              />
            ) : (
              <div style={{
                width: '80px',
                height: '80px',
                backgroundColor: '#e9ecef',
                borderRadius: '50%',
                marginBottom: '0.5rem',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '2rem',
                color: '#adb5bd'
              }}>
                {category.name.charAt(0)}
              </div>
            )}
            <h3 style={{ fontSize: '1rem', margin: 0 }}>{category.name}</h3>
          </div>
        </Link>
      ))}
    </div>
  );
};

export default CategoryShowcase;