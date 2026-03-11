import api, { extractContent } from './api';

// ============= AUTHENTICATED USER ENDPOINTS =============

/**
 * Create a new order
 * @param {Object} orderData - Order data with shipping address and items
 */
export const createOrder = async (orderData) => {
  const response = await api.post('/orders', orderData);
  return response.data;
};

/**
 * Get all orders for authenticated user (paginated)
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const getUserOrders = async (page = 0, size = 10) => {
  const response = await api.get('/orders', {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Get specific order by order number
 * @param {string} orderNumber - Order number
 */
export const getOrderByNumber = async (orderNumber) => {
  const response = await api.get(`/orders/${orderNumber}`);
  return response.data;
};

/**
 * Cancel an order
 * @param {number} orderId - Order ID
 * @param {string} reason - Cancellation reason (optional)
 */
export const cancelOrder = async (orderId, reason = null) => {
  const response = await api.put(`/orders/${orderId}/cancel`, 
    reason ? { reason } : null
  );
  return response.data;
};

// ============= GUEST ORDER LOOKUP =============

/**
 * Look up a guest order
 * @param {string} email - Guest email
 * @param {string} orderNumber - Order number
 */
export const lookupGuestOrder = async (email, orderNumber) => {
  const response = await api.get('/orders/guest/lookup', {
    params: { email, orderNumber }
  });
  return response.data;
};

// ============= ADMIN ENDPOINTS =============

/**
 * Get all orders (admin only)
 */
export const getAllOrders = async (page = 0, size = 20) => {
  const response = await api.get('/orders/all', {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Get orders by status (admin only)
 * @param {string} status - Order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
 */
export const getOrdersByStatus = async (status, page = 0, size = 20) => {
  const response = await api.get(`/orders/status/${status}`, {
    params: { page, size }
  });
  return extractContent(response.data);
};

/**
 * Update order status (admin only)
 * @param {number} orderId - Order ID
 * @param {string} status - New status
 * @param {string} comment - Optional comment
 */
export const updateOrderStatus = async (orderId, status, comment = '') => {
  const response = await api.put(`/orders/${orderId}/status`, {
    status,
    comment
  });
  return response.data;
};

/**
 * Update payment status (admin only)
 * @param {number} orderId - Order ID
 * @param {string} paymentStatus - Payment status (PAID, FAILED, REFUNDED, PENDING)
 */
export const updatePaymentStatus = async (orderId, paymentStatus) => {
  const response = await api.put(`/orders/${orderId}/payment`, {
    paymentStatus
  });
  return response.data;
};

/**
 * Update tracking number (admin only)
 * @param {number} orderId - Order ID
 * @param {string} trackingNumber - New tracking number
 */
export const updateTrackingNumber = async (orderId, trackingNumber) => {
  const response = await api.put(`/orders/${orderId}/tracking`, {
    trackingNumber
  });
  return response.data;
};