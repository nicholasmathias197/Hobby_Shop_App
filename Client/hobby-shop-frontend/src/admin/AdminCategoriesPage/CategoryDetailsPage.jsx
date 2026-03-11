// src/admin/AdminCategoriesPage/CategoryDetailsPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { getCategoryById } from '../../services/categoryService';
import { getProductsByCategory } from '../../services/productService';
import { Button } from '../../components/ui';

const CategoryDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [category, setCategory] = useState(null);
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadCategoryDetails();
  }, [id]);

  const loadCategoryDetails = async () => {
    try {
      const [categoryData, productsData] = await Promise.all([
        getCategoryById(id),
        getProductsByCategory(id, 0, 5)
      ]);
      setCategory(categoryData);
      setProducts(productsData.content || []);
    } catch (error) {
      console.error('Error loading category details:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (!category) return <div>Category not found</div>;

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <Button variant="secondary" onClick={() => navigate('/admin/categories')}>
          ← Back to Categories
        </Button>
      </div>

      <div style={{
        backgroundColor: '#f8f9fa',
        padding: '2rem',
        borderRadius: '4px',
        marginBottom: '2rem'
      }}>
        <div style={{ display: 'flex', gap: '2rem', alignItems: 'center' }}>
          {category.imageUrl ? (
            <img 
              src={category.imageUrl} 
              alt={category.name}
              style={{ width: '150px', height: '150px', objectFit: 'cover', borderRadius: '4px' }}
            />
          ) : (
            <div style={{
              width: '150px',
              height: '150px',
              backgroundColor: '#e9ecef',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: '4px',
              fontSize: '3rem',
              color: '#adb5bd'
            }}>
              {category.name.charAt(0)}
            </div>
          )}
          <div>
            <h1>{category.name}</h1>
            <p style={{ color: '#666', marginBottom: '0.5rem' }}>{category.description}</p>
            <p><strong>Status:</strong> {category.active ? 'Active' : 'Inactive'}</p>
            <p><strong>Total Products:</strong> {category.productCount || 0}</p>
            <p><strong>Created:</strong> {new Date(category.createdAt).toLocaleString()}</p>
          </div>
        </div>
      </div>

      <h2 style={{ marginBottom: '1rem' }}>Recent Products in this Category</h2>
      {products.length > 0 ? (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f8f9fa' }}>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>ID</th>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Product Name</th>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Price</th>
              <th style={{ padding: '0.75rem', textAlign: 'left' }}>Stock</th>
            </tr>
          </thead>
          <tbody>
            {products.map(product => (
              <tr key={product.id} style={{ borderBottom: '1px solid #ddd' }}>
                <td style={{ padding: '0.75rem' }}>{product.id}</td>
                <td style={{ padding: '0.75rem' }}>
                  <Link to={`/admin/products/edit/${product.id}`}>
                    {product.name}
                  </Link>
                </td>
                <td style={{ padding: '0.75rem' }}>${product.price}</td>
                <td style={{ padding: '0.75rem' }}>{product.stockQuantity}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>No products in this category yet.</p>
      )}

      <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem' }}>
        <Link to={`/admin/categories/edit/${category.id}`}>
          <Button variant="primary">Edit Category</Button>
        </Link>
      </div>
    </div>
  );
};

export default CategoryDetailsPage;