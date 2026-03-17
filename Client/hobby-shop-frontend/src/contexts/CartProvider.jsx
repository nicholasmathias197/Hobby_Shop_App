// src/contexts/CartProvider.jsx
import React, { useState, useEffect, useCallback, useMemo } from 'react';
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

  useEffect(() => {
    loadCart();
  }, [loadCart]);

  useEffect(() => {
    console.log('👤 User changed, reloading cart...');
    loadCart();
  }, [user, loadCart]);

  const addToCart = useCallback(async (product, quantity = 1) => {
    try {
      await addItemToCart(product.id, quantity);
      await loadCart();
    } catch (error) {
      console.error('❌ Error adding to cart:', error);
      throw error;
    }
  }, [loadCart]);

  const updateQuantity = useCallback(async (itemId, quantity) => {
    try {
      await updateCartItem(itemId, quantity);
      await loadCart();
    } catch (error) {
      console.error('❌ Error updating cart:', error);
      throw error;
    }
  }, [loadCart]);

  const removeFromCart = useCallback(async (itemId) => {
    try {
      await removeCartItem(itemId);
      await loadCart();
    } catch (error) {
      console.error('❌ Error removing from cart:', error);
      throw error;
    }
  }, [loadCart]);

  const clearCart = useCallback(async () => {
    try {
      await apiClearCart();
      setCart(null);
      await loadCart();
    } catch (error) {
      console.error('❌ Error clearing cart:', error);
      throw error;
    }
  }, [loadCart]);

  const cartTotal = useMemo(() =>
    cart?.items?.reduce((total, item) => total + (item.price * item.quantity), 0) || 0
  , [cart]);

  const cartItemsCount = useMemo(() => cart?.items?.length || 0, [cart]);

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