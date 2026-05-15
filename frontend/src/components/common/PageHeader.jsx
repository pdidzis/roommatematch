import React from 'react';
import { Box, Typography, Button } from '@mui/material';

const PageHeader = ({ title, subtitle, actionText, onAction, actionIcon }) => (
  <Box
    sx={{
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'flex-start',
      mb: 3,
      flexWrap: 'wrap',
      gap: 2
    }}
  >
    <Box>
      <Typography variant="h4" fontWeight="bold">
        {title}
      </Typography>
      {subtitle && (
        <Typography color="text.secondary" sx={{ mt: 0.5 }}>
          {subtitle}
        </Typography>
      )}
    </Box>
    {actionText && onAction && (
      <Button
        variant="contained"
        startIcon={actionIcon}
        onClick={onAction}
        sx={{
          borderRadius: 2,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          boxShadow: '0 4px 15px rgba(102,126,234,0.4)',
          '&:hover': {
            boxShadow: '0 6px 20px rgba(102,126,234,0.6)'
          }
        }}
      >
        {actionText}
      </Button>
    )}
  </Box>
);

export default PageHeader;
