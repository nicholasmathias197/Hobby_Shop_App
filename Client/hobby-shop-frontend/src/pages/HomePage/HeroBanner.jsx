import React from 'react';
import { Link } from 'react-router-dom';

const HeroBanner = () => {
  return (
    <div className="hero-banner">
      <h1 className="hero-title">Welcome to U197 Hobbies</h1>
      <h1 className="hero-title-gradient">
        Build Your Dreams with Premium Gundam Kits
      </h1>
      <p className="hero-text">
        Discover the finest selection of Gundam model kits, tools, and supplies. 
        From beginner to master grade, we have everything you need.
      </p>
      <Link to="/products" className="btn-shop-now">
        SHOP NOW
      </Link>
    </div>
  );
};

export default HeroBanner;