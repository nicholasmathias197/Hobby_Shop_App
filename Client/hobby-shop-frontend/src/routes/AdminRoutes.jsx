// src/routes/AdminRoutes.jsx
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import AdminLayout from '../components/layout/AdminLayout';

// Admin pages
import AdminDashboard from '../admin/AdminDashboard/AdminDashboard';
import AdminProductsPage from '../admin/AdminProductsPage/AdminProductsPage';
import AdminOrdersPage from '../admin/AdminOrdersPage/AdminOrdersPage';
import AdminBrandsPage from '../admin/AdminBrandsPage/AdminBrandsPage';
import AdminCategoriesPage from '../admin/AdminCategoriesPage/AdminCategoriesPage';
import AdminCustomersPage from '../admin/AdminCustomersPage/AdminCustomersPage';
import CategoryDetailsPage from '../admin/AdminCategoriesPage/CategoryDetailsPage';
// Product sub-pages
import CreateProductPage from '../admin/AdminProductsPage/CreateProductPage';
import EditProductPage from '../admin/AdminProductsPage/EditProductPage';

// Brand sub-pages
import CreateBrandPage from '../admin/AdminBrandsPage/CreateBrandPage';
import EditBrandPage from '../admin/AdminBrandsPage/EditBrandPage';

// Category sub-pages
import CreateCategoryPage from '../admin/AdminCategoriesPage/CreateCategoryPage';
import EditCategoryPage from '../admin/AdminCategoriesPage/EditCategoryPage';

// Order sub-pages
import OrderDetailsPage from '../admin/AdminOrdersPage/OrderDetailsPage';

// Customer sub-pages
import CustomerDetailsPage from '../admin/AdminCustomersPage/CustomerDetailsPage';

// Route guard
import AdminRoute from './AdminRoutes';

const AdminRoutes = () => {
  return (
    <AdminRoute>
      <AdminLayout>
        <Routes>
          {/* ============= ADMIN DASHBOARD ============= */}
          <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />
          <Route path="/dashboard" element={<AdminDashboard />} />

          {/* ============= PRODUCT MANAGEMENT ============= */}
          <Route path="/products" element={<AdminProductsPage />} />
          <Route path="/products/new" element={<CreateProductPage />} />
          <Route path="/products/edit/:id" element={<EditProductPage />} />
          
          {/* ============= BRAND MANAGEMENT ============= */}
          <Route path="/brands" element={<AdminBrandsPage />} />
          <Route path="/brands/new" element={<CreateBrandPage />} />
          <Route path="/brands/edit/:id" element={<EditBrandPage />} />
          
          {/* ============= CATEGORY MANAGEMENT ============= */}
          <Route path="/categories" element={<AdminCategoriesPage />} />
          <Route path="/categories/new" element={<CreateCategoryPage />} />
          <Route path="/categories/edit/:id" element={<EditCategoryPage />} />
          <Route path="/categories/:categoryId" element={<CategoryDetailsPage />} />
          
          {/* ============= ORDER MANAGEMENT ============= */}
          <Route path="/orders" element={<AdminOrdersPage />} />
          <Route path="/orders/:orderId" element={<OrderDetailsPage />} />
          
          {/* ============= CUSTOMER MANAGEMENT ============= */}
          <Route path="/customers" element={<AdminCustomersPage />} />
          <Route path="/customers/:customerId" element={<CustomerDetailsPage />} />

          {/* ============= 404 WITHIN ADMIN ============= */}
          <Route path="*" element={
            <div style={{ 
              textAlign: 'center', 
              padding: '4rem 2rem',
              backgroundColor: '#fff',
              borderRadius: '8px',
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
            }}>
              <h1 style={{ fontSize: '3rem', marginBottom: '1rem', color: '#dc3545' }}>404</h1>
              <h2 style={{ marginBottom: '1rem' }}>Admin Page Not Found</h2>
              <p style={{ marginBottom: '2rem', color: '#666' }}>
                The admin page you're looking for doesn't exist.
              </p>
              <a href="/admin/dashboard" style={{
                display: 'inline-block',
                padding: '0.75rem 2rem',
                backgroundColor: '#007bff',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '4px',
                fontWeight: 'bold'
              }}>
                Go to Dashboard
              </a>
            </div>
          } />
        </Routes>
      </AdminLayout>
    </AdminRoute>
  );
};

export default AdminRoutes;