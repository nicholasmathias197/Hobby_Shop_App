// ==================================================
// Cart Context Provider Component
// ==================================================
// This provider manages the shopping cart state throughout the application.
// It handles:
// - Loading cart data from the backend API
// - Adding, updating, and removing items
// - Calculating cart totals and item counts
// - Automatic cart refresh when user changes
// ==================================================

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

/**
 * CartProvider Component
 * 
 * Provides cart functionality to all child components through React Context.
 * Wraps the application or parts that need access to cart data.
 * 
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components to be wrapped
 * @returns {JSX.Element} Provider component with cart context value
 */
export const CartProvider = ({ children }) => {
  // ==================================================
  // State Management
  // ==================================================
  /** @type {[Object|null, Function]} Current cart data from API */
  const [cart, setCart] = useState(null);
  
  /** @type {[boolean, Function]} Loading state for async operations */
  const [loading, setLoading] = useState(true);
  
  /** @type {Object|null} Current authenticated user from auth hook */
  const { user } = useAuth();

  // ==================================================
  // Cart Data Loading
  // ==================================================
  /**
   * Loads the current user's cart from the backend API
   * @async
   * @function loadCart
   * @returns {Promise<void>}
   */
  const loadCart = useCallback(async () => {
    try {
      console.log('🔄 Loading cart...');
      const cartData = await getCart();
      console.log('✅ Cart loaded:', cartData);
      setCart(cartData);
    } catch (error) {
      console.error('❌ Error loading cart:', error);
      // Cart remains in previous state on error
    } finally {
      setLoading(false);
    }
  }, []);

  // ==================================================
  // Effects
  // ==================================================
  /**
   * Initial cart load on component mount
   */
  useEffect(() => {
    loadCart();
  }, [loadCart]);

  /**
   * Reload cart when user authentication state changes
   * This ensures cart data is fresh and belongs to the correct user
   */
  useEffect(() => {
    console.log('👤 User changed, reloading cart...');
    loadCart();
  }, [user, loadCart]);

  // ==================================================
  // Cart Operations
  // ==================================================
  /**
   * Adds a product to the cart
   * @async
   * @function addToCart
   * @param {Object} product - Product to add
   * @param {string|number} product.id - Product ID
   * @param {number} [quantity=1] - Quantity to add
   * @returns {Promise<void>}
   * @throws {Error} If API call fails
   */
  const addToCart = useCallback(async (product, quantity = 1) => {
    try {
      await addItemToCart(product.id, quantity);
      await loadCart(); // Refresh cart data after successful operation
    } catch (error) {
      console.error('❌ Error adding to cart:', error);
      throw error; // Re-throw for component-level error handling
    }
  }, [loadCart]);

  /**
   * Updates quantity of an existing cart item
   * @async
   * @function updateQuantity
   * @param {string|number} itemId - Cart item ID to update
   * @param {number} quantity - New quantity
   * @returns {Promise<void>}
   * @throws {Error} If API call fails
   */
  const updateQuantity = useCallback(async (itemId, quantity) => {
    try {
      await updateCartItem(itemId, quantity);
      await loadCart(); // Refresh cart data after successful operation
    } catch (error) {
      console.error('❌ Error updating cart:', error);
      throw error;
    }
  }, [loadCart]);

  /**
   * Removes an item from the cart
   * @async
   * @function removeFromCart
   * @param {string|number} itemId - Cart item ID to remove
   * @returns {Promise<void>}
   * @throws {Error} If API call fails
   */
  const removeFromCart = useCallback(async (itemId) => {
    try {
      await removeCartItem(itemId);
      await loadCart(); // Refresh cart data after successful operation
    } catch (error) {
      console.error('❌ Error removing from cart:', error);
      throw error;
    }
  }, [loadCart]);

  /**
   * Clears all items from the cart
   * @async
   * @function clearCart
   * @returns {Promise<void>}
   * @throws {Error} If API call fails
   */
  const clearCart = useCallback(async () => {
    try {
      await apiClearCart();
      setCart(null); // Optimistically clear local state
      await loadCart(); // Verify with fresh data from API
    } catch (error) {
      console.error('❌ Error clearing cart:', error);
      throw error;
    }
  }, [loadCart]);

  // ==================================================
  // Computed Values (Memoized)
  // ==================================================
  /**
   * Calculates the total price of all items in the cart
   * @type {number}
   */
  const cartTotal = useMemo(() =>
    cart?.items?.reduce((total, item) => total + (item.price * item.quantity), 0) || 0
  , [cart]);

  /**
   * Counts the number of unique items in the cart
   * @type {number}
   */
  const cartItemsCount = useMemo(() => cart?.items?.length || 0, [cart]);

  // ==================================================
  // Context Value
  // ==================================================
  /**
   * Context value object containing cart state and operations
   * Provided to all consuming components
   */
  const contextValue = useMemo(() => ({
    cart,               // Current cart data from API
    loading,            // Loading state indicator
    addToCart,          // Function to add items
    updateQuantity,     // Function to update quantities
    removeFromCart,     // Function to remove items
    clearCart,          // Function to clear cart
    cartTotal,          // Total price of all items
    cartItemsCount,     // Count of unique items
    refreshCart: loadCart // Manual refresh function
  }), [cart, loading, addToCart, updateQuantity, removeFromCart, clearCart, cartTotal, cartItemsCount, loadCart]);

  return (
    <CartContext.Provider value={contextValue}>
      {children}
    </CartContext.Provider>
  );
};

