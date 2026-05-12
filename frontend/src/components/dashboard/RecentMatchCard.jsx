import React from 'react';
import {
  Paper, Box, Typography, Avatar,
  Chip, Button
} from '@mui/material';
import { useNavigate } from 'react-router-dom';

const statusColors = {
  PENDING: '#ff9800',
  ACCEPTED: '#4caf50',
  DECLINED: '#f44336',
  ROOMMATE_CONFIRMED: '#9c27b0'
};

const statusLabels = {
  PENDING: 'Pending',
  ACCEPTED: 'Accepted',
  DECLINED: 'Declined',
  ROOMMATE_CONFIRMED: '🏠 Roommates'
};

const RecentMatchCard = ({ match }) => {
  const navigate = useNavigate();
  const { otherUser, compatibilityScore, status } = match;

  return (
    <Paper
      sx={{
        p: 2,
        borderRadius: 2,
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)'
      }}
    >
      <Avatar
        src={otherUser?.profilePhotoUrl}
        sx={{ width: 48, height: 48 }}
      >
        {otherUser?.firstName?.[0]}
      </Avatar>
      <Box sx={{ flex: 1 }}>
        <Typography fontWeight="bold">
          {otherUser?.firstName} {otherUser?.lastName}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {otherUser?.city}
        </Typography>
      </Box>
      <Box sx={{
        display: 'flex', flexDirection: 'column',
        alignItems: 'flex-end', gap: 0.5
      }}>
        <Typography variant="body2" fontWeight="bold"
          color="primary">
          {Math.round((compatibilityScore || 0) * 100)}% match
        </Typography>
        <Chip
          label={statusLabels[status]}
          size="small"
          sx={{
            bgcolor: statusColors[status] + '20',
            color: statusColors[status],
            fontWeight: 'bold'
          }}
        />
      </Box>
      <Button size="small" variant="outlined"
        onClick={() => navigate('/matches')}
        sx={{ borderRadius: 2 }}>
        View
      </Button>
    </Paper>
  );
};

export default RecentMatchCard;
