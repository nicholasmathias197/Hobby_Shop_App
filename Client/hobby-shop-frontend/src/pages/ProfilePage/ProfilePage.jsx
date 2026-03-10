import React from 'react';
import { useAuth } from '../../hooks/useAuth';
import { updateProfile } from '../../services/userService';
import ProfileForm from './ProfileForm';

const ProfilePage = () => {
  const { user, updateUser } = useAuth();

  const handleUpdate = async (formData) => {
    const updatedUser = await updateProfile(formData);
    updateUser(updatedUser);
  };

  return (
    <div style={{ maxWidth: '600px', margin: '0 auto' }}>
      <h1 style={{ marginBottom: '2rem' }}>My Profile</h1>
      
      <div style={{
        padding: '1rem',
        backgroundColor: '#f8f9fa',
        borderRadius: '4px',
        marginBottom: '2rem'
      }}>
        <h3>Account Information</h3>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Role:</strong> {user?.role}</p>
      </div>

      <ProfileForm user={user} onUpdate={handleUpdate} />
    </div>
  );
};

export default ProfilePage;