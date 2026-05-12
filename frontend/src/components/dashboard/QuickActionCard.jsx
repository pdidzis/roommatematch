import React from 'react';
import { Paper, Box, Typography, Button } from '@mui/material';

const QuickActionCard = ({ title, description,
  buttonText, onClick, icon, color }) => {
  return (
    <Paper
      sx={{
        p: 3,
        borderRadius: 3,
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        gap: 2,
        boxShadow: '0 4px 20px rgba(0,0,0,0.08)'
      }}
    >
      <Box sx={{ color: color, fontSize: 40 }}>
        {icon}
      </Box>
      <Typography variant="h6" fontWeight="bold">
        {title}
      </Typography>
      <Typography variant="body2" color="text.secondary"
        sx={{ flex: 1 }}>
        {description}
      </Typography>
      <Button
        variant="contained"
        onClick={onClick}
        sx={{
          bgcolor: color,
          '&:hover': { bgcolor: color, opacity: 0.9 },
          borderRadius: 2
        }}
      >
        {buttonText}
      </Button>
    </Paper>
  );
};

export default QuickActionCard;
