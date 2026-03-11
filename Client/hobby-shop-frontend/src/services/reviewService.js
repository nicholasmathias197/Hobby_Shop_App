import api from './api';

// Get reviews for a specific product
export const getProductReviews = async (productId, page = 0, size = 10) => {
  try {
    const response = await api.get(`/products/${productId}/reviews`, {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    console.error('Error fetching reviews:', error);
    return { content: [], totalElements: 0 };
  }
};

// Submit a new review (authenticated users only)
export const submitReview = async (productId, reviewData) => {
  const response = await api.post(`/products/${productId}/reviews`, reviewData);
  return response.data;
};

// Update a review (authenticated users only)
export const updateReview = async (reviewId, reviewData) => {
  const response = await api.put(`/reviews/${reviewId}`, reviewData);
  return response.data;
};

// Delete a review (authenticated users only)
export const deleteReview = async (reviewId) => {
  const response = await api.delete(`/reviews/${reviewId}`);
  return response.data;
};

// Get average rating for a product
export const getProductAverageRating = async (productId) => {
  try {
    const response = await api.get(`/products/${productId}/rating`);
    return response.data;
  } catch (error) {
    console.error('Error fetching average rating:', error);
    return { average: 0, count: 0 };
  }
};