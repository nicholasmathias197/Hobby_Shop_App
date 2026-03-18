import React from 'react';
import { Link } from 'react-router-dom';
import { GridScan } from '../../components/common/GridScan';

const HeroBanner = () => {
  return (
    <div className="hero-banner">
      <GridScan
        linesColor="#00d9ff"
        scanColor="#00d9ff"
        scanOpacity={0.6}
        gridScale={0.1}
        lineThickness={1.5}
        scanGlow={0.8}
        scanSoftness={2}
        scanDuration={2.5}
        scanDelay={1.5}
        scanDirection="pingpong"
        bloomIntensity={0.4}
        bloomThreshold={0.1}
        bloomSmoothing={0.3}
        chromaticAberration={0.001}
        noiseIntensity={0.008}
        sensitivity={0.5}
      />
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