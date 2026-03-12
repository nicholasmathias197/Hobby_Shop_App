import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ProductForm from './ProductForm';
import { createProduct } from '../../services/productService';

const CreateProductPage = () => {
  const navigate = useNavigate();
  const [isNewProduct, setIsNewProduct] = useState(true); // Default to true for new products

  const handleSubmit = async (formData) => {
    try {
      // Add the isNew flag to the form data
      const productData = {
        ...formData,
        isNew: isNewProduct // This will set the "NEW" tag on the product
      };
      
      await createProduct(productData);
      navigate('/admin/products');
    } catch (error) {
      console.error('Error creating product:', error);
    }
  };

  return (
    <div className="create-product-page">
      <div className="page-header">
        <h1>Create New Product</h1>
        <div className="new-product-badge">✨ New Product</div>
      </div>
      
      {/* New Product Toggle */}
      <div className="new-product-toggle">
        <label className="toggle-label">
          <span className="toggle-text">Mark as New Product:</span>
          <div className="toggle-container">
            <input
              type="checkbox"
              checked={isNewProduct}
              onChange={(e) => setIsNewProduct(e.target.checked)}
              className="toggle-checkbox"
              id="new-product-toggle"
            />
            <label htmlFor="new-product-toggle" className="toggle-switch-label">
              <span className="toggle-button"></span>
            </label>
          </div>
        </label>
        <p className="toggle-help-text">
          New products will display a "NEW" badge on the storefront for 30 days
        </p>
      </div>

      <ProductForm onSubmit={handleSubmit} />
    </div>
  );
};

export default CreateProductPage;