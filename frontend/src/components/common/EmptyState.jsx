import React from 'react';
import { Box, Typography, Button } from '@mui/material';

const EmptyState = ({ icon, title, description, buttonText, onButtonClick }) => (
  <Box
    sx={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      py: 8,
      gap: 2,
      color: 'text.secondary'
    }}
  >
    <Box sx={{ fontSize: 64, opacity: 0.3 }}>{icon}</Box>
    <Typography variant="h6" fontWeight="bold" color="text.secondary">
      {title}
    </Typography>
    {description && (
      <Typography
        variant="body2"
        color="text.secondary"
        textAlign="center"
        maxWidth={400}
      >
        {description}
      </Typography>
    )}
    {buttonText && onButtonClick && (
      <Button
        variant="contained"
        onClick={onButtonClick}
        sx={{
          mt: 1,
          borderRadius: 2,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
        }}
      >
        {buttonText}
      </Button>
    )}
  </Box>
);

export default EmptyState;
