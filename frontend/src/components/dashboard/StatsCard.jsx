import React from 'react';
import { Paper, Box, Typography } from '@mui/material';

const StatsCard = ({ title, value, icon, color, subtitle }) => {
  return (
    <Paper
      sx={{
        p: 3,
        borderRadius: 3,
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
        transition: 'transform 0.2s',
        '&:hover': { transform: 'translateY(-2px)' }
      }}
    >
      <Box
        sx={{
          width: 56,
          height: 56,
          borderRadius: 2,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: color + '20',
          color: color,
          fontSize: 28
        }}
      >
        {icon}
      </Box>
      <Box>
        <Typography variant="h4" fontWeight="bold">
          {value}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {title}
        </Typography>
        {subtitle && (
          <Typography variant="caption" color={color}>
            {subtitle}
          </Typography>
        )}
      </Box>
    </Paper>
  );
};

export default StatsCard;
