// src/services/cartService.js
import api from './api';

// ============= CART MANAGEMENT =============

/**
 * Get current user's cart (authenticated or guest)
 */
export const getCart = async () => {
  try {
    console.log('🔄 ===== GET CART =====');
    console.log('📤 Current sessionId in localStorage:', localStorage.getItem('sessionId'));
    console.log('📤 Current token in localStorage:', localStorage.getItem('token') ? 'Present' : 'None');
    
    const response = await api.get('/cart');
    
    console.log('📥 Cart response status:', response.status);
    console.log('📥 Cart response headers:', response.headers);
    console.log('📦 Cart response data:', response.data);
    
    // CAPTURE SESSION ID FROM RESPONSE DATA
    if (response.data && response.data.sessionId) {
      localStorage.setItem('sessionId', response.data.sessionId);
      console.log('✅✅✅ Session ID saved from response data:', response.data.sessionId);
    } else {
      console.log('❌ No sessionId in response data');
    }
    
    console.log('📤 SessionId after request:', localStorage.getItem('sessionId'));
    console.log('🔄 ===== END GET CART =====');
    
    return response.data;
  } catch (error) {
    console.error('❌ Error fetching cart:', error);
    throw error;
  }
};

/**
 * Add item to cart
 * @param {number} productId - Product ID
 * @param {number} quantity - Quantity to add
 */
export const addItemToCart = async (productId, quantity) => {
  try {
    console.log('➕ ===== ADD ITEM TO CART =====');
    console.log('➕ Product ID:', productId);
    console.log('➕ Quantity:', quantity);
    console.log('📤 Current sessionId before request:', localStorage.getItem('sessionId'));
    console.log('📤 Current token before request:', localStorage.getItem('token') ? 'Present' : 'None');
    
    const response = await api.post('/cart/items', { productId, quantity });
    
    console.log('📥 Add item response status:', response.status);
    console.log('📥 Add item response headers:', response.headers);
    console.log('📦 Add item response data:', response.data);
    
    // CAPTURE SESSION ID FROM RESPONSE DATA
    if (response.data && response.data.sessionId) {
      localStorage.setItem('sessionId', response.data.sessionId);
      console.log('✅✅✅ Session ID saved from response data:', response.data.sessionId);
    } else {
      console.log('❌ No sessionId in response data');
    }
    
    console.log('📤 Current sessionId after request:', localStorage.getItem('sessionId'));
    console.log('➕ ===== END ADD ITEM =====');
    
    return response.data;
  } catch (error) {
    console.error('❌ Error adding item to cart:', error);
    throw error;
  }
};

/**
 * Update quantity of a cart item
 * @param {number} itemId - Cart item ID
 * @param {number} quantity - New quantity
 */
export const updateCartItem = async (itemId, quantity) => {
  try {
    console.log('🔄 ===== UPDATE CART ITEM =====');
    console.log('🔄 Item ID:', itemId);
    console.log('🔄 New Quantity:', quantity);
    console.log('📤 Current sessionId:', localStorage.getItem('sessionId'));
    
    const response = await api.put(`/cart/items/${itemId}`, null, {
      params: { quantity }
    });
    
    console.log('📥 Update response:', response.data);
    console.log('🔄 ===== END UPDATE =====');
    
    return response.data;
  } catch (error) {
    console.error('❌ Error updating cart item:', error);
    throw error;
  }
};

/**
 * Remove item from cart
 * @param {number} itemId - Cart item ID
 */
export const removeCartItem = async (itemId) => {
  try {
    console.log('❌ ===== REMOVE CART ITEM =====');
    console.log('❌ Item ID:', itemId);
    console.log('📤 Current sessionId:', localStorage.getItem('sessionId'));
    
    const response = await api.delete(`/cart/items/${itemId}`);
    
    console.log('📥 Remove response:', response.data);
    console.log('❌ ===== END REMOVE =====');
    
    return response.data;
  } catch (error) {
    console.error('❌ Error removing cart item:', error);
    throw error;
  }
};

/**
 * Clear entire cart
 */
export const clearCart = async () => {
  try {
    console.log('🧹 ===== CLEAR CART =====');
    console.log('📤 Current sessionId:', localStorage.getItem('sessionId'));
    
    const response = await api.delete('/cart');
    
    console.log('📥 Clear response:', response.data);
    console.log('🧹 ===== END CLEAR =====');
    
    return response.data;
  } catch (error) {
    console.error('❌ Error clearing cart:', error);
    throw error;
  }
};

/**
 * Get cart item count
 */
export const getCartItemCount = async () => {
  try {
    console.log('🔢 ===== GET CART COUNT =====');
    console.log('📤 Current sessionId:', localStorage.getItem('sessionId'));
    
    const response = await api.get('/cart/count');
    
    console.log('📥 Cart count response:', response.data);
    console.log('🔢 ===== END GET COUNT =====');
    
    return response.data;
  } catch (error) {
    console.error('❌ Error getting cart count:', error);
    return 0;
  }
};

// ============= CART MERGING =============

/**
 * Merge guest cart with authenticated user's cart (called after login)
 * @param {string} sessionId - Guest session ID
 */
export const mergeCart = async (sessionId) => {
  try {
    console.log('🔄 ===== MERGE CART =====');
    console.log('🔄 Merging session:', sessionId);
    
    const response = await api.post('/cart/merge', null, {
      params: { sessionId }
    });
    
    console.log('📥 Merge response:', response.data);
    console.log('🔄 ===== END MERGE =====');
    
    return response.data;
  } catch (error) {
    console.error('❌ Error merging cart:', error);
    throw error;
  }
};

// ============= HELPER FUNCTIONS =============

/**
 * Get the current session ID from localStorage
 */
export const getSessionId = () => {
  return localStorage.getItem('sessionId');
};

/**
 * Clear session ID from localStorage
 */
export const clearSessionId = () => {
  localStorage.removeItem('sessionId');
  console.log('🧹 Session ID cleared from localStorage');
};

/**
 * Check if there is an active session
 */
export const hasActiveSession = () => {
  return !!localStorage.getItem('sessionId');
};