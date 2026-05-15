export const formatCurrency = (amount, currency = '€') =>
  `${currency}${Number(amount).toLocaleString()}`;

export const formatDate = (dateStr) => {
  if (!dateStr) return 'Not specified';
  return new Date(dateStr).toLocaleDateString('en-GB', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  });
};

export const formatRelativeTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  const mins = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);
  if (mins < 1) return 'Just now';
  if (mins < 60) return `${mins}m ago`;
  if (hours < 24) return `${hours}h ago`;
  if (days < 7) return `${days}d ago`;
  return formatDate(dateStr);
};

export const formatScore = (score) =>
  `${Math.round((score || 0) * 100)}%`;

export const truncate = (text, maxLength = 100) => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

export const formatRole = (role) => {
  const labels = {
    TENANT: 'Tenant',
    LANDLORD: 'Landlord',
    PARTNER: 'Partner',
    ADMIN: 'Admin'
  };
  return labels[role] || role;
};
