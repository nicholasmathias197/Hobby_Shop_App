import React from 'react';
import { Input } from '../../components/ui';

const ShippingForm = ({ formData, onChange }) => {
  return (
    <div>
      <h3>Shipping Information</h3>
      <Input
        label="First Name"
        name="firstName"
        value={formData.firstName || ''}
        onChange={onChange}
        required
      />
      <Input
        label="Last Name"
        name="lastName"
        value={formData.lastName || ''}
        onChange={onChange}
        required
      />
      <Input
        label="Email"
        type="email"
        name="email"
        value={formData.email || ''}
        onChange={onChange}
        required
      />
      <Input
        label="Phone"
        name="phone"
        value={formData.phone || ''}
        onChange={onChange}
        required
      />
      <Input
        label="Address"
        name="address"
        value={formData.address || ''}
        onChange={onChange}
        required
      />
      <Input
        label="City"
        name="city"
        value={formData.city || ''}
        onChange={onChange}
        required
      />
      <Input
        label="State"
        name="state"
        value={formData.state || ''}
        onChange={onChange}
        required
      />
      <Input
        label="Postal Code"
        name="postalCode"
        value={formData.postalCode || ''}
        onChange={onChange}
        required
      />
      <Input
        label="Country"
        name="country"
        value={formData.country || ''}
        onChange={onChange}
        required
      />
    </div>
  );
};

export default ShippingForm;