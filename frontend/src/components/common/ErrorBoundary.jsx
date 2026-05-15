import React from 'react';
import { Box, Typography, Button } from '@mui/material';
import { BugReport } from '@mui/icons-material';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Box
          sx={{
            minHeight: '100vh',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 2,
            p: 4
          }}
        >
          <BugReport sx={{ fontSize: 64, color: '#f44336' }} />
          <Typography variant="h5" fontWeight="bold">
            Something went wrong
          </Typography>
          <Typography color="text.secondary" textAlign="center">
            An unexpected error occurred. Please refresh the page.
          </Typography>
          <Button
            variant="contained"
            onClick={() => window.location.reload()}
            sx={{ borderRadius: 2 }}
          >
            Refresh Page
          </Button>
        </Box>
      );
    }
    return this.props.children;
  }
}

export default ErrorBoundary;
