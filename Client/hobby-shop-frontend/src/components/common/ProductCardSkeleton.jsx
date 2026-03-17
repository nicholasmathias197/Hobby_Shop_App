const ProductCardSkeleton = () => (
  <div className="product-card skeleton-card">
    <div className="skeleton skeleton-image" />
    <div className="product-info">
      <div className="skeleton skeleton-title" />
      <div className="skeleton skeleton-meta" />
      <div className="skeleton skeleton-rating" />
      <div className="product-footer" style={{ borderTop: 'none' }}>
        <div className="skeleton skeleton-price" />
        <div className="skeleton skeleton-btn" />
      </div>
    </div>
  </div>
);

export default ProductCardSkeleton;
