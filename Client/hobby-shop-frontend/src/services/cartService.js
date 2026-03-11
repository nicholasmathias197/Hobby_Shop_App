import api from './api';

// ============= CART MANAGEMENT =============

/**
 * Get current user's cart (authenticated or guest)
 */
export const getCart = async () => {
  const response = await api.get('/cart');
  console.log('Cart response:', response.data);
  return response.data;
};

/**
 * Add item to cart
 * @param {number} productId - Product ID
 * @param {number} quantity - Quantity to add
 */
export const addItemToCart = async (productId, quantity) => {
  const response = await api.post('/cart/items', { productId, quantity });
  return response.data;
};

/**
 * Update quantity of a cart item
 * @param {number} itemId - Cart item ID
 * @param {number} quantity - New quantity
 */
export const updateCartItem = async (itemId, quantity) => {
  const response = await api.put(`/cart/items/${itemId}`, null, {
    params: { quantity }
  });
  return response.data;
};

/**
 * Remove item from cart
 * @param {number} itemId - Cart item ID
 */
export const removeCartItem = async (itemId) => {
  const response = await api.delete(`/cart/items/${itemId}`);
  return response.data;
};

/**
 * Clear entire cart
 */
export const clearCart = async () => {
  const response = await api.delete('/cart');
  return response.data;
};

/**
 * Get cart item count
 */
export const getCartItemCount = async () => {
  const response = await api.get('/cart/count');
  return response.data;
};

// ============= CART MERGING =============

/**
 * Merge guest cart with authenticated user's cart (called after login)
 * @param {string} sessionId - Guest session ID
 */
export const mergeCart = async (sessionId) => {
  const response = await api.post('/cart/merge', null, {
    params: { sessionId }
  });
  return response.data;
};