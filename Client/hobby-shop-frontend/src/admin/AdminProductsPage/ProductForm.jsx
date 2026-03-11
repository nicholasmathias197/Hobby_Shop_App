import React, { useState, useEffect } from 'react';
import { Input, Button } from '../../components/ui';
import { getCategoriesArray } from '../../services/categoryService';  // Changed from productService
import { getBrandsArray } from '../../services/brandService';        // Changed from productService

const ProductForm = ({ product, onSubmit, onCancel }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    sku: '',
    stockQuantity: '',
    brandId: '',
    categoryId: '',
    scale: '',
    isActive: true,
    isFeatured: false,
    imageUrl: ''
  });
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadFormData();
    if (product) {
      setFormData({
        name: product.name || '',
        description: product.description || '',
        price: product.price || '',
        sku: product.sku || '',
        stockQuantity: product.stockQuantity || '',
        brandId: product.brandId || '',
        categoryId: product.categoryId || '',
        scale: product.scale || '',
        isActive: product.isActive !== undefined ? product.isActive : true,
        isFeatured: product.isFeatured || false,
        imageUrl: product.imageUrl || ''
      });
    }
  }, [product]);

  const loadFormData = async () => {
    try {
      const [categoriesData, brandsData] = await Promise.all([
        getCategoriesArray(),  // Now using correct service
        getBrandsArray()       // Now using correct service
      ]);
      setCategories(categoriesData);
      setBrands(brandsData);
    } catch (error) {
      console.error('Error loading form data:', error);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onSubmit(formData);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ padding: '1rem' }}>
      <h3>{product ? 'Edit Product' : 'Create New Product'}</h3>
      
      <Input
        label="Product Name"
        name="name"
        value={formData.name}
        onChange={handleChange}
        required
      />
      
      <div style={{ marginBottom: '1rem' }}>
        <label style={{ display: 'block', marginBottom: '0.5rem' }}>Description</label>
        <textarea
          name="description"
          value={formData.description}
          onChange={handleChange}
          rows="4"
          style={{
            width: '100%',
            padding: '0.5rem',
            border: '1px solid #ddd',
            borderRadius: '4px'
          }}
          required
        />
      </div>

      <Input
        label="Price"
        type="number"
        step="0.01"
        name="price"
        value={formData.price}
        onChange={handleChange}
        required
      />

      <Input
        label="SKU"
        name="sku"
        value={formData.sku}
        onChange={handleChange}
        required
      />

      <Input
        label="Stock Quantity"
        type="number"
        name="stockQuantity"
        value={formData.stockQuantity}
        onChange={handleChange}
        required
      />

      <div style={{ marginBottom: '1rem' }}>
        <label style={{ display: 'block', marginBottom: '0.5rem' }}>Brand</label>
        <select
          name="brandId"
          value={formData.brandId}
          onChange={handleChange}
          style={{
            width: '100%',
            padding: '0.5rem',
            border: '1px solid #ddd',
            borderRadius: '4px'
          }}
          required
        >
          <option value="">Select Brand</option>
          {brands.map(brand => (
            <option key={brand.id} value={brand.id}>{brand.name}</option>
          ))}
        </select>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label style={{ display: 'block', marginBottom: '0.5rem' }}>Category</label>
        <select
          name="categoryId"
          value={formData.categoryId}
          onChange={handleChange}
          style={{
            width: '100%',
            padding: '0.5rem',
            border: '1px solid #ddd',
            borderRadius: '4px'
          }}
          required
        >
          <option value="">Select Category</option>
          {categories.map(category => (
            <option key={category.id} value={category.id}>{category.name}</option>
          ))}
        </select>
      </div>

      <Input
        label="Scale"
        name="scale"
        value={formData.scale}
        onChange={handleChange}
      />

      <Input
        label="Image URL"
        name="imageUrl"
        value={formData.imageUrl}
        onChange={handleChange}
      />

      <div style={{ marginBottom: '1rem' }}>
        <label style={{ marginRight: '1rem' }}>
          <input
            type="checkbox"
            name="isActive"
            checked={formData.isActive}
            onChange={handleChange}
          />
          {' '}Active
        </label>
        <label>
          <input
            type="checkbox"
            name="isFeatured"
            checked={formData.isFeatured}
            onChange={handleChange}
          />
          {' '}Featured
        </label>
      </div>

      <div style={{ display: 'flex', gap: '1rem' }}>
        <Button type="submit" variant="primary" disabled={loading}>
          {loading ? 'Saving...' : (product ? 'Update' : 'Create')}
        </Button>
        <Button type="button" variant="secondary" onClick={onCancel}>
          Cancel
        </Button>
      </div>
    </form>
  );
};

export default ProductForm;