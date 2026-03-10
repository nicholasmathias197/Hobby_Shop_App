import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { MainLayout } from '../components/layout';

// Public pages
import HomePage from '../pages/HomePage';
import ProductsPage from '../pages/ProductsPage';
import ProductDetailPage from '../pages/ProductDetailPage';
import CartPage from '../pages/CartPage';
import LoginPage from '../pages/LoginPage';
import RegisterPage from '../pages/RegisterPage';

// Protected pages
import CheckoutPage from '../pages/CheckoutPage';
import OrdersPage from '../pages/OrdersPage';
import OrderDetailPage from '../pages/OrderDetailPage';
import ProfilePage from '../pages/ProfilePage';

// Admin pages
import AdminDashboard from '../admin/AdminDashboard';
import AdminProductsPage from '../admin/AdminProductsPage';
import AdminOrdersPage from '../admin/AdminOrdersPage';

// Route guards
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';

const AppRoutes = () => {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={
        <MainLayout>
          <HomePage />
        </MainLayout>
      } />
      
      <Route path="/products" element={
        <MainLayout>
          <ProductsPage />
        </MainLayout>
      } />
      
      <Route path="/product/:id" element={
        <MainLayout>
          <ProductDetailPage />
        </MainLayout>
      } />
      
      <Route path="/cart" element={
        <MainLayout>
          <CartPage />
        </MainLayout>
      } />
      
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

      {/* Protected routes */}
      <Route path="/checkout" element={
        <ProtectedRoute>
          <MainLayout>
            <CheckoutPage />
          </MainLayout>
        </ProtectedRoute>
      } />
      
      <Route path="/orders" element={
        <ProtectedRoute>
          <MainLayout>
            <OrdersPage />
          </MainLayout>
        </ProtectedRoute>
      } />
      
      <Route path="/order/:orderNumber" element={
        <ProtectedRoute>
          <MainLayout>
            <OrderDetailPage />
          </MainLayout>
        </ProtectedRoute>
      } />
      
      <Route path="/profile" element={
        <ProtectedRoute>
          <MainLayout>
            <ProfilePage />
          </MainLayout>
        </ProtectedRoute>
      } />

      {/* Admin routes */}
      <Route path="/admin/dashboard" element={
        <AdminRoute>
          <MainLayout>
            <AdminDashboard />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/products" element={
        <AdminRoute>
          <MainLayout>
            <AdminProductsPage />
          </MainLayout>
        </AdminRoute>
      } />
      
      <Route path="/admin/orders" element={
        <AdminRoute>
          <MainLayout>
            <AdminOrdersPage />
          </MainLayout>
        </AdminRoute>
      } />
    </Routes>
  );
};

export default AppRoutes;