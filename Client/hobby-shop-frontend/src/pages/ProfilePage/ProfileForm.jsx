import React, { useState } from 'react';
import { Input, Button } from '../../components/ui';

const ProfileForm = ({ user, onUpdate }) => {
  const [formData, setFormData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    phone: user?.phone || '',
    address: user?.address || '',
    city: user?.city || '',
    postalCode: user?.postalCode || '',
    country: user?.country || ''
  });
  const [loading, setLoading] = useState(false);

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
      await onUpdate(formData);
      alert('Profile updated successfully!');
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h3>Edit Profile</h3>
      <Input
        label="First Name"
        name="firstName"
        value={formData.firstName}
        onChange={handleChange}
        required
      />
      <Input
        label="Last Name"
        name="lastName"
        value={formData.lastName}
        onChange={handleChange}
        required
      />
      <Input
        label="Phone"
        name="phone"
        value={formData.phone}
        onChange={handleChange}
        required
      />
      <Input
        label="Address"
        name="address"
        value={formData.address}
        onChange={handleChange}
        required
      />
      <Input
        label="City"
        name="city"
        value={formData.city}
        onChange={handleChange}
        required
      />
      <Input
        label="Postal Code"
        name="postalCode"
        value={formData.postalCode}
        onChange={handleChange}
        required
      />
      <Input
        label="Country"
        name="country"
        value={formData.country}
        onChange={handleChange}
        required
      />
      <Button type="submit" variant="primary" disabled={loading}>
        {loading ? 'Updating...' : 'Update Profile'}
      </Button>
    </form>
  );
};

export default ProfileForm;