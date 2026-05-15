import React from 'react';
import { Box, CircularProgress, Typography } from '@mui/material';

const LoadingScreen = ({ message = 'Loading...' }) => (
  <Box
    sx={{
      minHeight: '100vh',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      gap: 3
    }}
  >
    <Typography variant="h4" color="white" fontWeight="bold">
      🏠 RoommateMatch
    </Typography>
    <CircularProgress sx={{ color: 'white' }} size={48} />
    <Typography color="rgba(255,255,255,0.8)">
      {message}
    </Typography>
  </Box>
);

export default LoadingScreen;
