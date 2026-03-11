import React from 'react';
import { useNavigate } from 'react-router-dom';
import ProductForm from './ProductForm';
import { createProduct } from '../../services/productService';

const CreateProductPage = () => {
  const navigate = useNavigate();

  const handleSubmit = async (formData) => {
    try {
      await createProduct(formData);
      navigate('/admin/products');
    } catch (error) {
      console.error('Error creating product:', error);
    }
  };

  return (
    <div>
      <h1>Create New Product</h1>
      <ProductForm onSubmit={handleSubmit} />
    </div>
  );
};

export default CreateProductPage;