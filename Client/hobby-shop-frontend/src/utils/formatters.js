export const formatters = {
  currency: (amount) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  },

  date: (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  },

  dateTime: (dateString) => {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  },

  phone: (phoneNumber) => {
    const cleaned = ('' + phoneNumber).replace(/\D/g, '');
    const match = cleaned.match(/^(\d{3})(\d{3})(\d{4})$/);
    if (match) {
      return '(' + match[1] + ') ' + match[2] + '-' + match[3];
    }
    return phoneNumber;
  },

  orderStatus: (status) => {
    const statusMap = {
      'PENDING': 'Pending',
      'PROCESSING': 'Processing',
      'SHIPPED': 'Shipped',
      'DELIVERED': 'Delivered',
      'CANCELLED': 'Cancelled'
    };
    return statusMap[status] || status;
  },

  paymentStatus: (status) => {
    const statusMap = {
      'PENDING': 'Pending',
      'PAID': 'Paid',
      'FAILED': 'Failed',
      'REFUNDED': 'Refunded'
    };
    return statusMap[status] || status;
  },

  capitalize: (string) => {
    return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase();
  },

  truncate: (text, length) => {
    if (text.length <= length) return text;
    return text.substring(0, length) + '...';
  }
};