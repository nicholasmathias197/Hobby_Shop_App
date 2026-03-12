import api, { extractContent } from './api';

// ============= ORDER CREATION =============

/**
 * Create a new order (works for both authenticated and guest users)
 * @param {Object} orderData - Order data with shipping address, payment info, and items
 */
export const createOrder = async (orderData) => {
  try {
    console.log('📦 Creating order with data:', orderData);
    
    const token = localStorage.getItem('token');
    const sessionId = localStorage.getItem('sessionId');
    
    let response;
    if (token) {
      // Authenticated user
      console.log('👤 Creating order as authenticated user');
      response = await api.post('/orders', orderData);
    } else {
      // Guest user
      console.log('👤 Creating order as guest with session:', sessionId);
      response = await api.post('/orders/guest', orderData, {
        params: sessionId ? { sessionId } : {},
        headers: sessionId ? { 'X-Session-ID': sessionId } : {}
      });
    }
    
    console.log('✅ Order created:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ Error creating order:', error);
    throw error;
  }
};

// ============= AUTHENTICATED USER ENDPOINTS =============

/**
 * Get all orders for authenticated user (paginated)
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const getUserOrders = async (page = 0, size = 10) => {
  try {
    const response = await api.get('/orders', {
      params: { page, size }
    });
    return extractContent(response.data);
  } catch (error) {
    console.error('❌ Error fetching user orders:', error);
    throw error;
  }
};

/**
 * Get specific order by order number
 * @param {string} orderNumber - Order number
 */
export const getOrderByNumber = async (orderNumber) => {
  try {
    const response = await api.get(`/orders/${orderNumber}`);
    return response.data;
  } catch (error) {
    console.error('❌ Error fetching order:', error);
    throw error;
  }
};

/**
 * Cancel an order
 * @param {number} orderId - Order ID
 * @param {string} reason - Cancellation reason (optional)
 */
export const cancelOrder = async (orderId, reason = null) => {
  try {
    const response = await api.put(`/orders/${orderId}/cancel`, 
      reason ? { reason } : null
    );
    return response.data;
  } catch (error) {
    console.error('❌ Error cancelling order:', error);
    throw error;
  }
};

/**
 * Get orders by customer ID
 * @param {number} customerId - Customer ID
 * @param {number} page - Page number
 * @param {number} size - Page size
 */
export const getOrdersByCustomer = async (customerId, page = 0, size = 10) => {
  try {
    const response = await api.get(`/orders/customer/${customerId}`, {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    console.error('❌ Error fetching orders by customer:', error);
    throw error;
  }
};

// ============= GUEST ORDER LOOKUP =============

/**
 * Look up a guest order
 * @param {string} email - Guest email
 * @param {string} orderNumber - Order number
 */
export const lookupGuestOrder = async (email, orderNumber) => {
  try {
    const response = await api.get('/orders/guest/lookup', {
      params: { email, orderNumber }
    });
    return response.data;
  } catch (error) {
    console.error('❌ Error looking up guest order:', error);
    throw error;
  }
};

// ============= ADMIN ENDPOINTS =============

/**
 * Get all orders (admin only)
 */
export const getAllOrders = async (page = 0, size = 20) => {
  try {
    const response = await api.get('/orders/all', {
      params: { page, size }
    });
    return extractContent(response.data);
  } catch (error) {
    console.error('❌ Error fetching all orders:', error);
    throw error;
  }
};

/**
 * Get orders by status (admin only)
 * @param {string} status - Order status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
 */
export const getOrdersByStatus = async (status, page = 0, size = 20) => {
  try {
    const response = await api.get(`/orders/status/${status}`, {
      params: { page, size }
    });
    return extractContent(response.data);
  } catch (error) {
    console.error('❌ Error fetching orders by status:', error);
    throw error;
  }
};

/**
 * Update order status (admin only)
 * @param {number} orderId - Order ID
 * @param {string} status - New status
 * @param {string} comment - Optional comment
 */
export const updateOrderStatus = async (orderId, status, comment = '') => {
  try {
    const response = await api.put(`/orders/${orderId}/status`, {
      status,
      comment
    });
    return response.data;
  } catch (error) {
    console.error('❌ Error updating order status:', error);
    throw error;
  }
};

/**
 * Update payment status (admin only)
 * @param {number} orderId - Order ID
 * @param {string} paymentStatus - Payment status (PAID, FAILED, REFUNDED, PENDING)
 */
export const updatePaymentStatus = async (orderId, paymentStatus) => {
  try {
    const response = await api.put(`/orders/${orderId}/payment`, {
      paymentStatus
    });
    return response.data;
  } catch (error) {
    console.error('❌ Error updating payment status:', error);
    throw error;
  }
};

/**
 * Update tracking number (admin only)
 * @param {number} orderId - Order ID
 * @param {string} trackingNumber - New tracking number
 */
export const updateTrackingNumber = async (orderId, trackingNumber) => {
  try {
    const response = await api.put(`/orders/${orderId}/tracking`, {
      trackingNumber
    });
    return response.data;
  } catch (error) {
    console.error('❌ Error updating tracking number:', error);
    throw error;
  }
};