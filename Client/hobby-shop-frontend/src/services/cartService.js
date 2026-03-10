import api from './api';

export const getCart = async () => {
  const response = await api.get('/cart');
  return response.data;
};

export const addItemToCart = async (productId, quantity) => {
  const response = await api.post('/cart/items', { productId, quantity });
  return response.data;
};

export const updateCartItem = async (itemId, quantity) => {
  const response = await api.put(`/cart/items/${itemId}`, null, {
    params: { quantity }
  });
  return response.data;
};

export const removeCartItem = async (itemId) => {
  const response = await api.delete(`/cart/items/${itemId}`);
  return response.data;
};

export const clearCart = async () => {
  const response = await api.delete('/cart/clear');
  return response.data;
};

export const mergeCart = async (sessionId) => {
  const response = await api.post(`/cart/merge?sessionId=${sessionId}`);
  return response.data;
};