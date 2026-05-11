import React, { useState, useEffect } from 'react';
import { Box } from '@mui/material';
import Navbar from './Navbar';
import { getUnreadCount } from '../../api/notificationApi';
import { useAuth } from '../../context/AuthContext';

const Layout = ({ children }) => {
  const [unreadCount, setUnreadCount] = useState(0);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated()) {
      const fetchUnreadCount = () => {
        getUnreadCount()
          .then(res => setUnreadCount(res.data.count || 0))
          .catch(() => {});
      };

      fetchUnreadCount();

      const interval = setInterval(fetchUnreadCount, 30000);

      return () => clearInterval(interval);
    }
  }, [isAuthenticated]);

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      <Navbar unreadCount={unreadCount} />
      <Box sx={{ maxWidth: 1200, margin: '0 auto', padding: 3 }}>
        {children}
      </Box>
    </Box>
  );
};

export default Layout;
