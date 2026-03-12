// src/services/orderService.js
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
 * @param {number} page - Page number
 * @param {number} size - Page size
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
 * @param {number} page - Page number
 * @param {number} size - Page size
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
 * This function handles two calling conventions:
 * 1. updateOrderStatus(orderId, status, comment)
 * 2. updateOrderStatus(orderId, { status, comment })
 * 
 * @param {number} orderId - Order ID
 * @param {string|Object} statusOrData - New status string OR object with status and comment
 * @param {string} comment - Optional comment (only used when second param is string)
 */
export const updateOrderStatus = async (orderId, statusOrData, comment = '') => {
  try {
    let payload;
    
    // Check if second parameter is an object (called with { status, comment })
    if (typeof statusOrData === 'object' && statusOrData !== null) {
      // Handle object format: updateOrderStatus(orderId, { status, comment })
      payload = {
        status: statusOrData.status,
        comment: statusOrData.comment || ''
      };
      console.log('📤 Updating order status (object format):', { orderId, payload });
    } else {
      // Handle separate parameters format: updateOrderStatus(orderId, status, comment)
      payload = {
        status: statusOrData,
        comment: comment
      };
      console.log('📤 Updating order status (param format):', { orderId, payload });
    }
    
    const response = await api.put(`/orders/${orderId}/status`, payload);
    console.log('📥 Update status response:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ Error updating order status:', error.response?.data || error.message);
    throw error;
  }
};

/**
 * Update payment status (admin only)
 * This function handles two calling conventions:
 * 1. updatePaymentStatus(orderId, paymentStatus)
 * 2. updatePaymentStatus(orderId, { paymentStatus })
 * 
 * @param {number} orderId - Order ID
 * @param {string|Object} statusOrData - Payment status string OR object with paymentStatus
 */
export const updatePaymentStatus = async (orderId, statusOrData) => {
  try {
    let payload;
    
    // Check if second parameter is an object (called with { paymentStatus })
    if (typeof statusOrData === 'object' && statusOrData !== null) {
      // Handle object format: updatePaymentStatus(orderId, { paymentStatus })
      payload = {
        paymentStatus: statusOrData.paymentStatus
      };
      console.log('📤 Updating payment status (object format):', { orderId, payload });
    } else {
      // Handle string format: updatePaymentStatus(orderId, paymentStatus)
      payload = {
        paymentStatus: statusOrData
      };
      console.log('📤 Updating payment status (string format):', { orderId, payload });
    }
    
    const response = await api.put(`/orders/${orderId}/payment`, payload);
    console.log('📥 Update payment status response:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ Error updating payment status:', error.response?.data || error.message);
    throw error;
  }
};

/**
 * Update tracking number (admin only)
 * This function handles two calling conventions:
 * 1. updateTrackingNumber(orderId, trackingNumber)
 * 2. updateTrackingNumber(orderId, { trackingNumber })
 * 
 * @param {number} orderId - Order ID
 * @param {string|Object} trackingOrData - Tracking number string OR object with trackingNumber
 */
export const updateTrackingNumber = async (orderId, trackingOrData) => {
  try {
    let payload;
    
    // Check if second parameter is an object (called with { trackingNumber })
    if (typeof trackingOrData === 'object' && trackingOrData !== null) {
      // Handle object format: updateTrackingNumber(orderId, { trackingNumber })
      payload = {
        trackingNumber: trackingOrData.trackingNumber
      };
      console.log('📤 Updating tracking number (object format):', { orderId, payload });
    } else {
      // Handle string format: updateTrackingNumber(orderId, trackingNumber)
      payload = {
        trackingNumber: trackingOrData
      };
      console.log('📤 Updating tracking number (string format):', { orderId, payload });
    }
    
    const response = await api.put(`/orders/${orderId}/tracking`, payload);
    console.log('📥 Update tracking response:', response.data);
    return response.data;
  } catch (error) {
    console.error('❌ Error updating tracking number:', error.response?.data || error.message);
    throw error;
  }
};