// src/pages/HomePage/CategoryShowcase.jsx
import React from 'react';
import { Link } from 'react-router-dom';

// Import all category images directly
import gundamModelsImg from '../../assets/images/gunpla.png';
import toolsImg from '../../assets/images/tools.jpg';
import paintsImg from '../../assets/images/paints.jpg';
import accessoriesImg from '../../assets/images/accessories.jpg';
import airbrushesImg from '../../assets/images/airbrushes.jpg';

// Create a mapping of category names to imported images
const categoryImages = {
  'Gundam Models': gundamModelsImg,
  'Tools': toolsImg,
  'Paints': paintsImg,
  'Accessories': accessoriesImg,
  'Airbrush Supplies': airbrushesImg,
  // Add more mappings as needed
};

// Default fallback image
import defaultCategoryImg from '../../assets/images/logo.png';

const CategoryShowcase = ({ categories }) => {
  if (!categories || categories.length === 0) {
    return null;
  }

  const getCategoryImage = (category) => {
    // First check if the category has an imageUrl from the database
    if (category.imageUrl) {
      return category.imageUrl;
    }
    
    // Otherwise, use the local image based on category name
    return categoryImages[category.name] || defaultCategoryImg;
  };

  return (
    <div className="category-showcase">
      <div className="category-grid">
        {categories.map(category => (
          <Link 
            key={category.id} 
            to={`/category/${category.id}`}
            className="category-card-link"
          >
            <div className="category-card">
              <div className="category-image-container">
                <img 
                  src={getCategoryImage(category)} 
                  alt={category.name}
                  className="category-image"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = defaultCategoryImg;
                  }}
                />
              </div>
              <div className="category-info">
                <h3 className="category-name">{category.name}</h3>
                {category.productCount > 0 && (
                  <p className="category-count">{category.productCount} products</p>
                )}
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default CategoryShowcase;