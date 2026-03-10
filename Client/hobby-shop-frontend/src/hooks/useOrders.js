import { useState } from 'react';
import { 
  getUserOrders, 
  getOrderByNumber,
  createOrder,
  cancelOrder 
} from '../services/orderService';

export const useOrders = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchUserOrders = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getUserOrders();
      setOrders(data.content || data);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const getOrder = async (orderNumber) => {
    setLoading(true);
    setError(null);
    try {
      return await getOrderByNumber(orderNumber);
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const placeOrder = async (orderData) => {
    setLoading(true);
    setError(null);
    try {
      return await createOrder(orderData);
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const cancelUserOrder = async (orderId) => {
    setLoading(true);
    setError(null);
    try {
      await cancelOrder(orderId);
      await fetchUserOrders();
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  };

  return {
    orders,
    loading,
    error,
    fetchUserOrders,
    getOrder,
    placeOrder,
    cancelUserOrder
  };
};