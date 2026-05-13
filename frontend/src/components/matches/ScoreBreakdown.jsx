import React from 'react';
import { Box, Typography, LinearProgress } from '@mui/material';

const factorLabels = {
  sleepSchedule: '😴 Sleep Schedule',
  cleanliness: '🧹 Cleanliness',
  socialHabits: '👥 Social Habits',
  noiseLevel: '🔊 Noise Level',
  guestFrequency: '🚪 Guests',
  workFromHome: '💻 Work From Home',
  budgetCompatibility: '💰 Budget Match'
};

const ScoreBreakdown = ({ breakdown }) => {
  if (!breakdown) return null;

  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="subtitle2" fontWeight="bold"
        color="text.secondary" sx={{ mb: 1 }}>
        Compatibility Breakdown
      </Typography>
      {Object.entries(breakdown).map(([key, value]) => (
        <Box key={key} sx={{ mb: 1 }}>
          <Box sx={{ display: 'flex',
            justifyContent: 'space-between', mb: 0.5 }}>
            <Typography variant="caption">
              {factorLabels[key] || key}
            </Typography>
            <Typography variant="caption" fontWeight="bold">
              {Math.round(value * 100)}%
            </Typography>
          </Box>
          <LinearProgress
            variant="determinate"
            value={value * 100}
            sx={{
              height: 6,
              borderRadius: 3,
              bgcolor: '#e0e0e0',
              '& .MuiLinearProgress-bar': {
                bgcolor: value >= 0.8 ? '#4caf50'
                  : value >= 0.6 ? '#667eea'
                  : value >= 0.4 ? '#ff9800'
                  : '#f44336',
                borderRadius: 3
              }
            }}
          />
        </Box>
      ))}
    </Box>
  );
};

export default ScoreBreakdown;
