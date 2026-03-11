// src/routes/AppRoutes.jsx
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import MainLayout from '../components/layout/MainLayout';

// ============= PUBLIC PAGE IMPORTS =============
import HomePage from '../pages/HomePage/Homepage';
import ProductsPage from '../pages/ProductsPage';
import ProductDetailPage from '../pages/ProductDetailPage';
import BrandPage from '../pages/BrandPage';
import CategoryPage from '../pages/CategoryPage';
import CartPage from '../pages/CartPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';

// ============= PROTECTED PAGE IMPORTS =============
import CheckoutPage from '../pages/CheckoutPage/CheckoutPage';
import OrdersPage from '../pages/OrdersPage';
import OrderDetailPage from '../pages/OrderDetailPage';
import ProfilePage from '../pages/ProfilePage/ProfilePage';

// ============= ADMIN PAGE IMPORTS =============
import AdminDashboard from '../admin/AdminDashboard/AdminDashboard';
import AdminProductsPage from '../admin/AdminProductsPage/AdminProductsPage';
import AdminCreateProductPage from '../admin/AdminProductsPage/CreateProductPage';
import AdminEditProductPage from '../admin/AdminProductsPage/EditProductPage';
import AdminOrdersPage from '../admin/AdminOrdersPage/AdminOrdersPage';
import AdminOrderDetailsPage from '../admin/AdminOrdersPage/OrderDetailsPage';
import AdminBrandsPage from '../admin/AdminBrandsPage/AdminBrandsPage';
import AdminCreateBrandPage from '../admin/AdminBrandsPage/CreateBrandPage';
import AdminEditBrandPage from '../admin/AdminBrandsPage/EditBrandPage';
import AdminCategoriesPage from '../admin/AdminCategoriesPage/AdminCategoriesPage';
import AdminCreateCategoryPage from '../admin/AdminCategoriesPage/CreateCategoryPage';
import AdminEditCategoryPage from '../admin/AdminCategoriesPage/EditCategoryPage';
import AdminCustomersPage from '../admin/AdminCustomersPage/AdminCustomersPage';
import AdminCustomerDetailsPage from '../admin/AdminCustomersPage/CustomerDetailsPage';

// ============= ROUTE GUARDS =============
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';

// ============= 404 PAGE COMPONENT =============
const NotFoundPage = () => (
  <MainLayout>
    <div style={{ 
      textAlign: 'center', 
      padding: '4rem 2rem',
      maxWidth: '600px',
      margin: '0 auto'
    }}>
      <h1 style={{ fontSize: '6rem', marginBottom: '1rem', color: '#dc3545' }}>404</h1>
      <h2 style={{ marginBottom: '1rem' }}>Page Not Found</h2>
      <p style={{ marginBottom: '2rem', color: '#666' }}>
        The page you're looking for doesn't exist or has been moved.
      </p>
      <a href="/" style={{
        display: 'inline-block',
        padding: '0.75rem 2rem',
        backgroundColor: '#007bff',
        color: 'white',
        textDecoration: 'none',
        borderRadius: '4px',
        fontWeight: 'bold'
      }}>
        Go Home
      </a>
    </div>
  </MainLayout>
);

const AppRoutes = () => {
  return (
    <Routes>
     {/* ============= PUBLIC ROUTES (No Authentication Required) ============= */}

{/* Home Page */}
<Route path="/" element={
  <MainLayout>
    <HomePage />
  </MainLayout>
} />

{/* Products */}
<Route path="/products" element={
  <MainLayout>
    <ProductsPage />
  </MainLayout>
} />

{/* Product Details */}
<Route path="/product/:id" element={
  <MainLayout>
    <ProductDetailPage />
  </MainLayout>
} />

{/* Category Page */}
<Route path="/category/:categoryId" element={
  <MainLayout>
    <CategoryPage />
  </MainLayout>
} />

{/* Brand Page - ADD THIS */}
<Route path="/brand/:brandId" element={
  <MainLayout>
    <BrandPage />
  </MainLayout>
} />

{/* Shopping Cart */}
<Route path="/cart" element={
  <MainLayout>
    <CartPage />
  </MainLayout>
} />

{/* Authentication */}
<Route path="/login" element={
  <MainLayout>
    <LoginPage />
  </MainLayout>
} />

<Route path="/register" element={
  <MainLayout>
    <RegisterPage />
  </MainLayout>
} />

      {/* ============= PROTECTED ROUTES (Authentication Required) ============= */}
      
      {/* Checkout */}
      <Route path="/checkout" element={
        <ProtectedRoute>
          <MainLayout>
            <CheckoutPage />
          </MainLayout>
        </ProtectedRoute>
      } />
      
      {/* User Orders */}
      <Route path="/orders" element={
        <ProtectedRoute>
          <MainLayout>
            <OrdersPage />
          </MainLayout>
        </ProtectedRoute>
      } />
      
      {/* Order Details */}
      <Route path="/order/:orderNumber" element={
        <ProtectedRoute>
          <MainLayout>
            <OrderDetailPage />
          </MainLayout>
        </ProtectedRoute>
      } />
      
      {/* User Profile */}
      <Route path="/profile" element={
        <ProtectedRoute>
          <MainLayout>
            <ProfilePage />
          </MainLayout>
        </ProtectedRoute>
      } />

      {/* ============= ADMIN ROUTES (Admin Role Required) ============= */}
      
      {/* Admin Dashboard - Redirect */}
      <Route path="/admin" element={
        <AdminRoute>
          <Navigate to="/admin/dashboard" replace />
        </AdminRoute>
      } />
      
      {/* Admin Dashboard */}
      <Route path="/admin/dashboard" element={
        <AdminRoute>
          <MainLayout>
            <AdminDashboard />
          </MainLayout>
        </AdminRoute>
      } />
      
      {/* ===== Product Management ===== */}
      <Route path="/admin/products" element={
        <AdminRoute>
          <MainLayout>
            <AdminProductsPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/products/new" element={
        <AdminRoute>
          <MainLayout>
            <AdminCreateProductPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/products/edit/:id" element={
        <AdminRoute>
          <MainLayout>
            <AdminEditProductPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      {/* ===== Brand Management ===== */}
      <Route path="/admin/brands" element={
        <AdminRoute>
          <MainLayout>
            <AdminBrandsPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/brands/new" element={
        <AdminRoute>
          <MainLayout>
            <AdminCreateBrandPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/brands/edit/:id" element={
        <AdminRoute>
          <MainLayout>
            <AdminEditBrandPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      {/* ===== Category Management ===== */}
      <Route path="/admin/categories" element={
        <AdminRoute>
          <MainLayout>
            <AdminCategoriesPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/categories/new" element={
        <AdminRoute>
          <MainLayout>
            <AdminCreateCategoryPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/categories/edit/:id" element={
        <AdminRoute>
          <MainLayout>
            <AdminEditCategoryPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      {/* ===== Order Management ===== */}
      <Route path="/admin/orders" element={
        <AdminRoute>
          <MainLayout>
            <AdminOrdersPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/orders/:orderId" element={
        <AdminRoute>
          <MainLayout>
            <AdminOrderDetailsPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      {/* ===== Customer Management ===== */}
      <Route path="/admin/customers" element={
        <AdminRoute>
          <MainLayout>
            <AdminCustomersPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/customers/:customerId" element={
        <AdminRoute>
          <MainLayout>
            <AdminCustomerDetailsPage />
          </MainLayout>
        </AdminRoute>
      } />

      {/* ============= 404 NOT FOUND - Catch All ============= */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRoutes;