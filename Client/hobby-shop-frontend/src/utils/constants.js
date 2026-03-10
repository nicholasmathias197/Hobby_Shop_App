export const API_BASE_URL = 'http://localhost:8080/api';

export const ORDER_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  SHIPPED: 'SHIPPED',
  DELIVERED: 'DELIVERED',
  CANCELLED: 'CANCELLED'
};

export const PAYMENT_STATUS = {
  PENDING: 'PENDING',
  PAID: 'PAID',
  FAILED: 'FAILED',
  REFUNDED: 'REFUNDED'
};

export const USER_ROLES = {
  USER: 'USER',
  ADMIN: 'ADMIN'
};

export const PRODUCT_CATEGORIES = {
  GUNDAM: 'Gundam Models',
  ACTION_FIGURES: 'Action Figures',
  PAINTS: 'Paints',
  TOOLS: 'Tools'
};