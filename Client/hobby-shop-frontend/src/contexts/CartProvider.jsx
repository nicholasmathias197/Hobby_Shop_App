import React, { useState, useEffect } from 'react';
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

  useEffect(() => {
    loadCart();
  }, [user]);

  const loadCart = async () => {
    try {
      const cartData = await getCart();
      setCart(cartData);
    } catch (error) {
      console.error('Error loading cart:', error);
    } finally {
      setLoading(false);
    }
  };

  const addToCart = async (product, quantity = 1) => {
    try {
      await addItemToCart(product.id, quantity);
      await loadCart();
    } catch (error) {
      console.error('Error adding to cart:', error);
      throw error;
    }
  };

  const updateQuantity = async (itemId, quantity) => {
    try {
      await updateCartItem(itemId, quantity);
      await loadCart();
    } catch (error) {
      console.error('Error updating cart:', error);
      throw error;
    }
  };

  const removeFromCart = async (itemId) => {
    try {
      await removeCartItem(itemId);
      await loadCart();
    } catch (error) {
      console.error('Error removing from cart:', error);
      throw error;
    }
  };

  const clearCart = async () => {
    try {
      await apiClearCart();
      setCart(null);
    } catch (error) {
      console.error('Error clearing cart:', error);
      throw error;
    }
  };

  const cartTotal = cart?.items?.reduce((total, item) => 
    total + (item.price * item.quantity), 0
  ) || 0;

  return (
    <CartContext.Provider value={{
      cart,
      loading,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      cartTotal
    }}>
      {children}
    </CartContext.Provider>
  );
};