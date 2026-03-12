// src/pages/CheckoutPage/PaymentForm.jsx
import React, { useState } from 'react';
import { Input } from '../../components/ui';

const PaymentForm = ({ formData, onChange, onSameAsShippingChange }) => {
  const [showBilling, setShowBilling] = useState(!formData.sameAsShipping);

  const handleSameAsShippingChange = (e) => {
    const checked = e.target.checked;
    setShowBilling(!checked);
    onSameAsShippingChange(checked);
  };

  // Format card number with spaces
  const formatCardNumber = (value) => {
    const v = value.replace(/\s+/g, '').replace(/[^0-9]/gi, '');
    const matches = v.match(/\d{4,16}/g);
    const match = (matches && matches[0]) || '';
    const parts = [];

    for (let i = 0; i < match.length; i += 4) {
      parts.push(match.substring(i, i + 4));
    }

    if (parts.length) {
      return parts.join(' ');
    } else {
      return value;
    }
  };

  const handleCardNumberChange = (e) => {
    const formatted = formatCardNumber(e.target.value);
    onChange({
      target: {
        name: 'cardNumber',
        value: formatted
      }
    });
  };

  return (
    <div className="payment-form">
      <h3>Payment Information</h3>
      
      <div className="form-group">
        <label>Payment Method</label>
        <select
          name="paymentMethod"
          value={formData.paymentMethod || 'credit_card'}
          onChange={onChange}
          className="payment-select"
        >
          <option value="credit_card">Credit Card</option>
          <option value="paypal">PayPal</option>
          <option value="cash_on_delivery">Cash on Delivery</option>
        </select>
      </div>

      {formData.paymentMethod === 'credit_card' && (
        <div className="credit-card-form">
          <div className="card-preview">
            <div className="card-chip">💳</div>
            <div className="card-number-display">
              {formData.cardNumber || '**** **** **** ****'}
            </div>
            <div className="card-details">
              <span>{formData.cardHolderName?.toUpperCase() || 'CARD HOLDER'}</span>
              <span>
                {formData.cardExpiryMonth || 'MM'}/{formData.cardExpiryYear || 'YY'}
              </span>
            </div>
          </div>

          <Input
            label="Card Number"
            name="cardNumber"
            value={formData.cardNumber || ''}
            onChange={handleCardNumberChange}
            placeholder="1234 5678 9012 3456"
            maxLength="19"
          />

          <Input
            label="Card Holder Name"
            name="cardHolderName"
            value={formData.cardHolderName || ''}
            onChange={onChange}
            placeholder="John Doe"
          />

          <div className="form-row">
            <div className="form-group half">
              <label>Expiry Month</label>
              <select
                name="cardExpiryMonth"
                value={formData.cardExpiryMonth || ''}
                onChange={onChange}
                className="payment-select"
              >
                <option value="">MM</option>
                {Array.from({ length: 12 }, (_, i) => {
                  const month = (i + 1).toString().padStart(2, '0');
                  return <option key={month} value={month}>{month}</option>;
                })}
              </select>
            </div>

            <div className="form-group half">
              <label>Expiry Year</label>
              <select
                name="cardExpiryYear"
                value={formData.cardExpiryYear || ''}
                onChange={onChange}
                className="payment-select"
              >
                <option value="">YY</option>
                {Array.from({ length: 10 }, (_, i) => {
                  const year = (new Date().getFullYear() + i).toString();
                  return <option key={year} value={year}>{year}</option>;
                })}
              </select>
            </div>

            <div className="form-group half">
              <Input
                label="CVV"
                name="cardCvv"
                value={formData.cardCvv || ''}
                onChange={onChange}
                placeholder="123"
                maxLength="3"
              />
            </div>
          </div>
        </div>
      )}

      {formData.paymentMethod === 'paypal' && (
        <div className="paypal-info">
          <p>You will be redirected to PayPal to complete your payment.</p>
        </div>
      )}

      {formData.paymentMethod === 'cash_on_delivery' && (
        <div className="cod-info">
          <p>Pay with cash when your order arrives.</p>
        </div>
      )}

      <div className="billing-section">
        <h3>Billing Address</h3>
        
        <label className="checkbox-label">
          <input
            type="checkbox"
            name="sameAsShipping"
            checked={formData.sameAsShipping}
            onChange={handleSameAsShippingChange}
          />
          Same as shipping address
        </label>

        {/* Use showBilling state to conditionally show billing form */}
        {showBilling && (
          <div className="billing-form">
            <Input
              label="Billing Address"
              name="billingAddress"
              value={formData.billingAddress || ''}
              onChange={onChange}
              required={!formData.sameAsShipping}
              placeholder="123 Main St"
            />

            <div className="form-row">
              <Input
                label="City"
                name="billingCity"
                value={formData.billingCity || ''}
                onChange={onChange}
                required={!formData.sameAsShipping}
                placeholder="New York"
              />
              <Input
                label="Postal Code"
                name="billingPostalCode"
                value={formData.billingPostalCode || ''}
                onChange={onChange}
                required={!formData.sameAsShipping}
                placeholder="10001"
              />
            </div>

            <Input
              label="Country"
              name="billingCountry"
              value={formData.billingCountry || ''}
              onChange={onChange}
              required={!formData.sameAsShipping}
              placeholder="USA"
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentForm;