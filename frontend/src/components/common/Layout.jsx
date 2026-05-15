import React, { useState, useEffect } from 'react';
import { Box, Typography } from '@mui/material';
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
    <Box
      sx={{
        minHeight: '100vh',
        bgcolor: '#f5f5f5',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      <Navbar unreadCount={unreadCount} />
      <Box
        className="fade-in"
        sx={{
          flex: 1,
          width: '100%',
          maxWidth: 1200,
          margin: '0 auto',
          padding: { xs: 2, sm: 3 }
        }}
      >
        {children}
      </Box>
      <Box
        component="footer"
        sx={{
          mt: 'auto',
          py: 3,
          textAlign: 'center',
          color: 'text.secondary',
          borderTop: '1px solid #e0e0e0',
          bgcolor: 'white'
        }}
      >
        <Typography variant="body2">
          © 2026 RoommateMatch. Connecting people, building homes. 🏠
        </Typography>
      </Box>
    </Box>
  );
};

export default Layout;
