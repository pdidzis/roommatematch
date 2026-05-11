import React from 'react';
import { Typography, Box, Paper } from '@mui/material';
import { useAuth } from '../context/AuthContext';

const DashboardPage = () => {
  const { user } = useAuth();

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Welcome back, {user?.firstName}!
      </Typography>
      <Paper sx={{ p: 3, borderRadius: 2 }}>
        <Typography variant="body1" color="text.secondary">
          Your dashboard is loading...
        </Typography>
      </Paper>
    </Box>
  );
};

export default DashboardPage;
