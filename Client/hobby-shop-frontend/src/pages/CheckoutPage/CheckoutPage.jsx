// src/pages/CheckoutPage/CheckoutPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../../hooks/useCart';
import { useAuth } from '../../hooks/useAuth';
import { createOrder } from '../../services/orderService';
import ShippingForm from './ShippingForm';
import PaymentForm from './PaymentForm';
import OrderSummary from './OrderSummary';
import { Button } from '../../components/ui';

const CheckoutPage = () => {
  const navigate = useNavigate();
  const { cart, clearCart, cartTotal } = useCart();
  const { user, isAuthenticated } = useAuth();
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState(1); // 1: Shipping, 2: Payment, 3: Review
  const [formData, setFormData] = useState({
    // Shipping
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phone: user?.phone || '',
    address: user?.address || '',
    city: user?.city || '',
    state: user?.state || '',
    postalCode: user?.postalCode || '',
    country: user?.country || '',
    
    // Payment
    paymentMethod: 'credit_card',
    sameAsShipping: true,
    cardNumber: '',
    cardHolderName: '',
    cardExpiryMonth: '',
    cardExpiryYear: '',
    cardCvv: '',
    billingAddress: '',
    billingCity: '',
    billingPostalCode: '',
    billingCountry: '',
    
    // Guest
    guestEmail: !isAuthenticated() ? (user?.email || '') : ''
  });

  if (!cart || cart.items.length === 0) {
    navigate('/cart');
    return null;
  }

  const isGuest = !isAuthenticated();

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSameAsShippingChange = (checked) => {
    setFormData(prev => ({
      ...prev,
      sameAsShipping: checked
    }));
  };

  const handleNextStep = () => {
    setStep(step + 1);
    window.scrollTo(0, 0);
  };

  const handlePrevStep = () => {
    setStep(step - 1);
    window.scrollTo(0, 0);
  };

  const validateShipping = () => {
    const required = ['firstName', 'lastName', 'email', 'address', 'city', 'postalCode', 'country'];
    for (const field of required) {
      if (!formData[field]) {
        alert(`Please fill in ${field}`);
        return false;
      }
    }
    return true;
  };

  const validatePayment = () => {
    if (formData.paymentMethod === 'credit_card') {
      if (!formData.cardNumber || !formData.cardHolderName || 
          !formData.cardExpiryMonth || !formData.cardExpiryYear || !formData.cardCvv) {
        alert('Please fill in all credit card details');
        return false;
      }
      
      // Simple validation (for demo purposes)
      if (formData.cardNumber.replace(/\s/g, '').length < 16) {
        alert('Please enter a valid card number');
        return false;
      }
      
      if (formData.cardCvv.length < 3) {
        alert('Please enter a valid CVV');
        return false;
      }
    }
    
    if (!formData.sameAsShipping) {
      const required = ['billingAddress', 'billingCity', 'billingPostalCode', 'billingCountry'];
      for (const field of required) {
        if (!formData[field]) {
          alert(`Please fill in billing ${field}`);
          return false;
        }
      }
    }
    
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (step === 1) {
      if (validateShipping()) {
        handleNextStep();
      }
      return;
    }
    
    if (step === 2) {
      if (validatePayment()) {
        handleNextStep();
      }
      return;
    }

    // Step 3: Submit order
    setLoading(true);

    try {
      // Prepare billing address
      const billingAddress = formData.sameAsShipping ? {
        address: formData.address,
        city: formData.city,
        postalCode: formData.postalCode,
        country: formData.country
      } : {
        address: formData.billingAddress,
        city: formData.billingCity,
        postalCode: formData.billingPostalCode,
        country: formData.billingCountry
      };

      const orderData = {
        // Shipping
        shippingAddress: formData.address,
        shippingCity: formData.city,
        shippingPostalCode: formData.postalCode,
        shippingCountry: formData.country,
        
        // Payment
        paymentMethod: formData.paymentMethod,
        
        // For credit card (in real app, you'd use a payment processor)
        ...(formData.paymentMethod === 'credit_card' && {
          cardLastFour: formData.cardNumber.slice(-4)
        }),
        
        // Billing
        billingAddress: billingAddress.address,
        billingCity: billingAddress.city,
        billingPostalCode: billingAddress.postalCode,
        billingCountry: billingAddress.country,
        
        // Customer info
        customerEmail: formData.email,
        customerPhone: formData.phone,
        customerName: `${formData.firstName} ${formData.lastName}`,
        
        // Items
        items: cart.items.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price
        })),
        
        // Guest checkout
        guestEmail: isGuest ? formData.email : undefined,
        notes: formData.notes
      };

      console.log('Submitting order:', orderData);
      
      const order = await createOrder(orderData);
      console.log('Order created:', order);
      
      // Clear cart and redirect
      await clearCart();
      navigate(`/order/${order.orderNumber}`);
    } catch (error) {
      console.error('Error creating order:', error);
      alert('Failed to create order. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="checkout-page">
      <h1>Checkout</h1>
      
      {/* Progress Steps */}
      <div className="checkout-steps">
        <div className={`step ${step >= 1 ? 'active' : ''} ${step > 1 ? 'completed' : ''}`}>
          <span className="step-number">1</span>
          <span className="step-label">Shipping</span>
        </div>
        <div className={`step ${step >= 2 ? 'active' : ''} ${step > 2 ? 'completed' : ''}`}>
          <span className="step-number">2</span>
          <span className="step-label">Payment</span>
        </div>
        <div className={`step ${step >= 3 ? 'active' : ''}`}>
          <span className="step-number">3</span>
          <span className="step-label">Review</span>
        </div>
      </div>

      {isGuest && step === 1 && (
        <div className="guest-notice">
          <span className="notice-icon">ℹ️</span>
          <div className="notice-content">
            <strong>You're checking out as a guest</strong>
            <p>Create an account after checkout to track your orders!</p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="checkout-form">
        <div className="checkout-grid">
          <div className="checkout-main">
            {step === 1 && (
              <ShippingForm 
                formData={formData} 
                onChange={handleChange} 
              />
            )}

            {step === 2 && (
              <PaymentForm 
                formData={formData}
                onChange={handleChange}
                onSameAsShippingChange={handleSameAsShippingChange}
              />
            )}

            {step === 3 && (
              <div className="review-section">
                <h3>Review Your Order</h3>
                
                <div className="review-block">
                  <h4>Shipping Address</h4>
                  <p>
                    {formData.firstName} {formData.lastName}<br />
                    {formData.address}<br />
                    {formData.city}, {formData.state} {formData.postalCode}<br />
                    {formData.country}<br />
                    {formData.email}<br />
                    {formData.phone}
                  </p>
                  <button type="button" onClick={() => setStep(1)} className="edit-btn">
                    Edit
                  </button>
                </div>

                <div className="review-block">
                  <h4>Payment Method</h4>
                  <p>
                    {formData.paymentMethod === 'credit_card' && (
                      <>
                        Credit Card ending in {formData.cardNumber?.slice(-4)}<br />
                        {formData.cardHolderName}
                      </>
                    )}
                    {formData.paymentMethod === 'paypal' && 'PayPal'}
                    {formData.paymentMethod === 'cash_on_delivery' && 'Cash on Delivery'}
                  </p>
                  <button type="button" onClick={() => setStep(2)} className="edit-btn">
                    Edit
                  </button>
                </div>

                <div className="review-block">
                  <h4>Billing Address</h4>
                  {formData.sameAsShipping ? (
                    <p>Same as shipping address</p>
                  ) : (
                    <p>
                      {formData.billingAddress}<br />
                      {formData.billingCity}, {formData.billingPostalCode}<br />
                      {formData.billingCountry}
                    </p>
                  )}
                </div>
              </div>
            )}
          </div>

          <div className="checkout-sidebar">
            <OrderSummary cartItems={cart.items || []} cartTotal={cartTotal} />
          </div>
        </div>

        <div className="checkout-actions">
          {step > 1 && (
            <Button 
              type="button" 
              variant="secondary" 
              onClick={handlePrevStep}
              disabled={loading}
            >
              ← Back
            </Button>
          )}
          
          <Button 
            type="submit" 
            variant="success" 
            disabled={loading}
          >
            {loading ? 'Processing...' : step === 3 ? 'Place Order' : 'Continue'}
          </Button>
        </div>
      </form>
    </div>
  );
};

export default CheckoutPage;