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
        backgroundColor: 'rgba(0,217,255,0.1)',
        borderRadius: '4px',
        marginBottom: '2rem'
      }}>
        <h3>Account Information</h3>
        <p><strong>Email:</strong> {user?.email}</p>
        <p><strong>Name:</strong> {user?.firstName} {user?.lastName}</p>
        <p><strong>Member Since:</strong> {user?.memberSince && new Date(user.memberSince).toLocaleDateString()}</p>
        
      </div>

      <ProfileForm user={user} onUpdate={handleUpdate} />
    </div>
  );
};

export default ProfilePage;