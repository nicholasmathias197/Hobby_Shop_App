import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../../hooks/useCart';
import { useAuth } from '../../hooks/useAuth';
import { createOrder } from '../../services/orderService';
import ShippingForm from './ShippingForm';
import OrderSummary from './OrderSummary';
import { Button } from '../../components/ui';

const CheckoutPage = () => {
  const navigate = useNavigate();
  const { cart, clearCart } = useCart();
  const { user, isAuthenticated } = useAuth();  // Keep isAuthenticated
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phone: user?.phone || '',
    address: user?.address || '',
    city: user?.city || '',
    state: user?.state || '',
    postalCode: user?.postalCode || '',
    country: user?.country || ''
  });

  if (!cart || cart.items.length === 0) {
    navigate('/cart');
    return null;
  }

  // You could add guest checkout logic here later
  const isGuest = !isAuthenticated();  // Using it here

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const orderData = {
        shippingAddress: {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          phone: formData.phone,
          address: formData.address,
          city: formData.city,
          state: formData.state,
          postalCode: formData.postalCode,
          country: formData.country
        },
        items: cart.items.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price
        })),
        // Add flag for guest checkout if needed
        isGuest: isGuest
      };

      const order = await createOrder(orderData);
      clearCart();
      navigate(`/order/${order.orderNumber}`);
    } catch (error) {
      console.error('Error creating order:', error);
      alert('Failed to create order. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <h1 style={{ marginBottom: '2rem' }}>Checkout</h1>
      
      {isGuest && (
        <div style={{
          padding: '1rem',
          backgroundColor: 'rgba(0,217,255,0.1)',
          color: '#856404',
          borderRadius: '4px',
          marginBottom: '1rem'
        }}>
          You're checking out as a guest. Create an account to track your orders!
        </div>
      )}
      
      <form onSubmit={handleSubmit}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 300px', gap: '2rem' }}>
          <ShippingForm formData={formData} onChange={handleChange} />
          <OrderSummary />
        </div>

        <div style={{ marginTop: '2rem', textAlign: 'right' }}>
          <Button 
            type="submit" 
            variant="success" 
            disabled={loading}
            fullWidth
          >
            {loading ? 'Processing...' : 'Place Order'}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default CheckoutPage;