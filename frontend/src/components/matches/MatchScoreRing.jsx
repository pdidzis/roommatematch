import React from 'react';
import { Box, Typography } from '@mui/material';

const MatchScoreRing = ({ score, size = 80 }) => {
  const percentage = Math.round((score || 0) * 100);
  const color = percentage >= 80 ? '#4caf50'
    : percentage >= 60 ? '#667eea'
    : percentage >= 40 ? '#ff9800'
    : '#f44336';

  return (
    <Box sx={{
      width: size, height: size,
      borderRadius: '50%',
      background: `conic-gradient(
        ${color} ${percentage * 3.6}deg,
        #e0e0e0 ${percentage * 3.6}deg
      )`,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      position: 'relative'
    }}>
      <Box sx={{
        width: size - 16,
        height: size - 16,
        borderRadius: '50%',
        bgcolor: 'white',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column'
      }}>
        <Typography variant="caption" fontWeight="bold"
          sx={{ color, lineHeight: 1 }}>
          {percentage}%
        </Typography>
      </Box>
    </Box>
  );
};

export default MatchScoreRing;
