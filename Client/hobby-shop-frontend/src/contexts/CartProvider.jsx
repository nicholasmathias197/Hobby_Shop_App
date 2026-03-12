// src/contexts/CartProvider.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { CartContext } from './CartContext';
import { 
  getCart, 
  addItemToCart, 
  updateCartItem, 
  removeCartItem,
  clearCart as apiClearCart 
} from '../services/cartService';
import { useAuth } from '../hooks/useAuth';

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState(null);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  const loadCart = useCallback(async () => {
    try {
      console.log('🔄 Loading cart...');
      const cartData = await getCart();
      console.log('✅ Cart loaded:', cartData);
      setCart(cartData);
    } catch (error) {
      console.error('❌ Error loading cart:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  // Load cart on mount
  useEffect(() => {
    loadCart();
  }, [loadCart]);

  // Reload cart when user changes (login/logout)
  useEffect(() => {
    console.log('👤 User changed, reloading cart...');
    loadCart();
  }, [user, loadCart]);

  const addToCart = async (product, quantity = 1) => {
    try {
      console.log('➕ Adding to cart:', { product, quantity });
      await addItemToCart(product.id, quantity);
      await loadCart();
    } catch (error) {
      console.error('❌ Error adding to cart:', error);
      throw error;
    }
  };

  const updateQuantity = async (itemId, quantity) => {
    try {
      console.log('🔄 Updating cart item:', { itemId, quantity });
      await updateCartItem(itemId, quantity);
      await loadCart();
    } catch (error) {
      console.error('❌ Error updating cart:', error);
      throw error;
    }
  };

  const removeFromCart = async (itemId) => {
    try {
      console.log('❌ Removing from cart:', itemId);
      await removeCartItem(itemId);
      await loadCart();
    } catch (error) {
      console.error('❌ Error removing from cart:', error);
      throw error;
    }
  };

  const clearCart = async () => {
    try {
      console.log('🧹 Clearing cart');
      await apiClearCart();
      setCart(null);
      await loadCart();
    } catch (error) {
      console.error('❌ Error clearing cart:', error);
      throw error;
    }
  };

  const cartTotal = cart?.items?.reduce((total, item) => 
    total + (item.price * item.quantity), 0
  ) || 0;

  const cartItemsCount = cart?.items?.length || 0;

  return (
    <CartContext.Provider value={{
      cart,
      loading,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      cartTotal,
      cartItemsCount,
      refreshCart: loadCart
    }}>
      {children}
    </CartContext.Provider>
  );
};