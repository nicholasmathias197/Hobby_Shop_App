import React, { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getProductReviews, submitReview } from '../../services/reviewService';
import { Button } from '../ui';

const ProductReviews = ({ productId, onRatingUpdate }) => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [newReview, setNewReview] = useState({ rating: 5, comment: '' });
  const [submitting, setSubmitting] = useState(false);
  const [stats, setStats] = useState({ average: 0, total: 0, distribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 } });
  
  const {  isAuthenticated } = useAuth();

  useEffect(() => {
    loadReviews();
  }, [productId]);

  const loadReviews = async () => {
    setLoading(true);
    try {
      const response = await getProductReviews(productId);
      const reviewsData = response.content || [];
      setReviews(reviewsData);
      
      // Calculate stats
      calculateStats(reviewsData);
      
    } catch (error) {
      console.error('Error loading reviews:', error);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = (reviewsData) => {
    if (reviewsData.length === 0) {
      setStats({ average: 0, total: 0, distribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 } });
      return;
    }

    const total = reviewsData.length;
    const sum = reviewsData.reduce((acc, review) => acc + review.rating, 0);
    const average = sum / total;
    
    const distribution = {
      5: reviewsData.filter(r => r.rating === 5).length,
      4: reviewsData.filter(r => r.rating === 4).length,
      3: reviewsData.filter(r => r.rating === 3).length,
      2: reviewsData.filter(r => r.rating === 2).length,
      1: reviewsData.filter(r => r.rating === 1).length,
    };

    setStats({ average, total, distribution });
    
    // Pass average to parent component if needed
    if (onRatingUpdate) {
      onRatingUpdate({ average, total });
    }
  };

  const handleSubmitReview = async (e) => {
    e.preventDefault();
    if (!isAuthenticated()) {
      alert('Please login to submit a review');
      return;
    }

    setSubmitting(true);
    try {
      await submitReview(productId, newReview);
      setNewReview({ rating: 5, comment: '' });
      setShowForm(false);
      loadReviews(); // Reload reviews
    } catch (error) {
      console.error('Error submitting review:', error);
      alert('Failed to submit review');
    } finally {
      setSubmitting(false);
    }
  };

  const renderStars = (rating) => {
    return (
      <div className="star-rating">
        {[1, 2, 3, 4, 5].map(star => (
          <span key={star} style={{ 
            color: star <= rating ? '#ffc107' : '#e4e5e9',
            fontSize: '1.2rem',
            cursor: 'pointer'
          }}>
            ★
          </span>
        ))}
      </div>
    );
  };

  const renderRatingBars = () => {
    const total = stats.total;
    if (total === 0) return null;

    return (
      <div className="rating-bars">
        {[5, 4, 3, 2, 1].map(rating => (
          <div key={rating} className="rating-bar-row" style={{ display: 'flex', alignItems: 'center', marginBottom: '0.5rem' }}>
            <span style={{ width: '30px' }}>{rating}★</span>
            <div style={{ 
              flex: 1, 
              height: '8px', 
              backgroundColor: '#e0e0e0', 
              borderRadius: '4px',
              margin: '0 0.5rem'
            }}>
              <div style={{
                width: `${(stats.distribution[rating] / total) * 100}%`,
                height: '100%',
                backgroundColor: '#ffc107',
                borderRadius: '4px'
              }} />
            </div>
            <span style={{ width: '40px', fontSize: '0.9rem' }}>{stats.distribution[rating]}</span>
          </div>
        ))}
      </div>
    );
  };

  if (loading) return <div>Loading reviews...</div>;

  return (
    <div className="product-reviews" style={{ marginTop: '3rem' }}>
      <h2>Customer Reviews</h2>
      
      {/* Reviews Summary */}
      <div className="reviews-summary" style={{ 
        display: 'grid', 
        gridTemplateColumns: '200px 1fr', 
        gap: '2rem',
        padding: '1.5rem',
        backgroundColor: 'rgba(0,0,0,0.3)',
        borderRadius: '8px',
        marginBottom: '2rem'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '3rem', fontWeight: 'bold', color: '#ffc107' }}>
            {stats.average.toFixed(1)}
          </div>
          <div style={{ marginBottom: '0.5rem' }}>
            {renderStars(Math.round(stats.average))}
          </div>
          <div style={{ color: '#e0e1dd' }}>
            Based on {stats.total} {stats.total === 1 ? 'review' : 'reviews'}
          </div>
        </div>
        <div>
          {renderRatingBars()}
        </div>
      </div>

      {/* Write Review Button */}
      {isAuthenticated() && (
        <Button 
          variant="primary" 
          onClick={() => setShowForm(!showForm)}
          style={{ marginBottom: '2rem' }}
        >
          {showForm ? 'Cancel' : 'Write a Review'}
        </Button>
      )}

      {/* Review Form */}
      {showForm && (
        <form onSubmit={handleSubmitReview} style={{ 
          padding: '1.5rem', 
          backgroundColor: 'rgba(0,0,0,0.3)',
          borderRadius: '8px',
          marginBottom: '2rem'
        }}>
          <h3>Write Your Review</h3>
          
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem' }}>Rating</label>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              {[1, 2, 3, 4, 5].map(star => (
                <button
                  key={star}
                  type="button"
                  onClick={() => setNewReview({ ...newReview, rating: star })}
                  style={{
                    background: 'none',
                    border: 'none',
                    fontSize: '2rem',
                    color: star <= newReview.rating ? '#ffc107' : '#e4e5e9',
                    cursor: 'pointer',
                    padding: '0 0.25rem'
                  }}
                >
                  ★
                </button>
              ))}
            </div>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem' }}>Your Review</label>
            <textarea
              value={newReview.comment}
              onChange={(e) => setNewReview({ ...newReview, comment: e.target.value })}
              rows="4"
              placeholder="Share your thoughts about this product..."
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                background: 'rgba(0,0,0,0.6)',
                border: '2px solid rgba(0,217,255,0.3)',
                borderRadius: '4px',
                color: '#e0e1dd'
              }}
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem' }}>
            <Button type="submit" variant="primary" disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Review'}
            </Button>
            <Button type="button" variant="secondary" onClick={() => setShowForm(false)}>
              Cancel
            </Button>
          </div>
        </form>
      )}

      {/* Reviews List */}
      <div className="reviews-list">
        {reviews.length === 0 ? (
          <p style={{ textAlign: 'center', padding: '2rem', color: '#e0e1dd' }}>
            No reviews yet. Be the first to review this product!
          </p>
        ) : (
          reviews.map(review => (
            <div key={review.id} style={{
              padding: '1.5rem',
              borderBottom: '1px solid rgba(0,217,255,0.2)',
              marginBottom: '1rem'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <strong style={{ color: '#00d9ff' }}>{review.customerName || 'Anonymous'}</strong>
                  {review.isVerifiedPurchase && (
                    <span style={{
                      fontSize: '0.75rem',
                      padding: '0.2rem 0.5rem',
                      backgroundColor: '#28a745',
                      color: 'white',
                      borderRadius: '4px'
                    }}>
                      Verified Purchase
                    </span>
                  )}
                </div>
                <div style={{ color: '#e0e1dd', fontSize: '0.9rem' }}>
                  {new Date(review.createdAt).toLocaleDateString()}
                </div>
              </div>
              
              <div style={{ marginBottom: '0.75rem' }}>
                {renderStars(review.rating)}
              </div>
              
              <p style={{ lineHeight: '1.6', color: '#e0e1dd' }}>
                {review.comment}
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default ProductReviews;