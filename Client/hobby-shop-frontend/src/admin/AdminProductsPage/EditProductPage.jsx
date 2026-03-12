// src/admin/AdminProductsPage/EditProductPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import ProductForm from './ProductForm';
import { getProductById, updateProduct } from '../../services/productService';

const EditProductPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isNewProduct, setIsNewProduct] = useState(false); // Add state for new product flag

  useEffect(() => {
    loadProduct();
  }, [id]);

  const loadProduct = async () => {
    try {
      const data = await getProductById(id);
      setProduct(data);
      // Set the isNewProduct state based on the product's isNew property
      setIsNewProduct(data.isNew || false);
    } catch (error) {
      console.error('Error loading product:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (formData) => {
    try {
      // Add the isNew flag to the form data
      const productData = {
        ...formData,
        isNew: isNewProduct
      };
      await updateProduct(id, productData);
      navigate('/admin/products');
    } catch (error) {
      console.error('Error updating product:', error);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="create-product-page">
      <div className="page-header">
        <h1>Edit Product</h1>
        <div className="new-product-badge">✏️ Editing Product</div>
      </div>

      {/* New Product Toggle - Add this to edit page too */}
      <div className="new-product-toggle">
        <label className="toggle-label">
          <span className="toggle-text">Mark as New Product:</span>
          <div className="toggle-container">
            <input
              type="checkbox"
              checked={isNewProduct}
              onChange={(e) => setIsNewProduct(e.target.checked)}
              className="toggle-checkbox"
              id="edit-product-toggle"
            />
            <label htmlFor="edit-product-toggle" className="toggle-switch-label">
              <span className="toggle-button"></span>
            </label>
          </div>
        </label>
        <p className="toggle-help-text">
          New products will display a "NEW" badge on the storefront for 30 days
        </p>
      </div>

      <ProductForm product={product} onSubmit={handleSubmit} />
    </div>
  );
};

export default EditProductPage;