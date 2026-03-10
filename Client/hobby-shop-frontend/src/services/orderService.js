import api from './api';

// User orders
export const getUserOrders = async () => {
  const response = await api.get('/orders');
  return response.data;
};

export const getOrderByNumber = async (orderNumber) => {
  const response = await api.get(`/orders/${orderNumber}`);
  return response.data;
};

export const createOrder = async (orderData) => {
  const response = await api.post('/orders', orderData);
  return response.data;
};

export const cancelOrder = async (orderId) => {
  const response = await api.put(`/orders/${orderId}/cancel`);
  return response.data;
};

// Guest orders
export const lookupGuestOrder = async (email, orderNumber) => {
  const response = await api.get('/orders/guest/lookup', {
    params: { email, orderNumber }
  });
  return response.data;
};

// Admin orders
export const getAllOrders = async () => {
  const response = await api.get('/orders/all');
  return response.data;
};

export const getOrdersByStatus = async (status) => {
  const response = await api.get(`/orders/status/${status}`);
  return response.data;
};

export const updateOrderStatus = async (orderId, statusData) => {
  const response = await api.put(`/orders/${orderId}/status`, statusData);
  return response.data;
};

export const updatePaymentStatus = async (orderId, paymentData) => {
  const response = await api.put(`/orders/${orderId}/payment`, paymentData);
  return response.data;
};

export const updateTrackingNumber = async (orderId, trackingData) => {
  const response = await api.put(`/orders/${orderId}/tracking`, trackingData);
  return response.data;
};