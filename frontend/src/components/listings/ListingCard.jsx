import React, { useState } from 'react';
import {
  Paper, Box, Typography, Chip, Button,
  Avatar, Divider, IconButton, Tooltip
} from '@mui/material';
import {
  LocationOn, AttachMoney, Bed, CalendarToday,
  Pets, SmokeFree, SmokingRooms, Verified,
  Favorite, FavoriteBorder, Chat
} from '@mui/icons-material';

const ListingCard = ({ listing, onInterest,
  showInterestButton, matchId, currentUserInterested,
  onVerify, isAdmin }) => {

  const {
    id, title, description, city, address,
    monthlyRent, availableFrom, availableRooms,
    totalRooms, petsAllowed, smokingAllowed,
    isVerified, landlordName, daysUntilAvailable,
    currentUserInterested: interested,
    bothRoommatesInterested
  } = listing;

  const [localInterested, setLocalInterested] =
    useState(currentUserInterested || interested || false);
  const [loading, setLoading] = useState(false);

  const handleInterest = async () => {
    if (localInterested || !matchId) return;
    setLoading(true);
    try {
      await onInterest(id, matchId);
      setLocalInterested(true);
    } finally {
      setLoading(false);
    }
  };

  const truncatedDescription =
    description && description.length > 100
      ? description.substring(0, 100) + '...'
      : description;

  return (
    <Paper
      sx={{
        borderRadius: 3,
        boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
        p: 3,
        transition: 'transform 0.2s',
        '&:hover': { transform: 'translateY(-2px)' },
        border: bothRoommatesInterested
          ? '2px solid #9c27b0'
          : 'none',
        display: 'flex',
        flexDirection: 'column',
        height: '100%'
      }}
    >
      {/* Top section */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          gap: 1,
          mb: 1
        }}
      >
        <Typography variant="h6" fontWeight="bold">
          {title}
        </Typography>
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'flex-end',
            gap: 0.5
          }}
        >
          {isVerified && (
            <Chip
              icon={<Verified />}
              label="Verified"
              size="small"
              sx={{
                bgcolor: '#4caf5020',
                color: '#4caf50',
                fontWeight: 600
              }}
            />
          )}
          {bothRoommatesInterested && (
            <Chip
              label="Both interested! 🎉"
              size="small"
              sx={{
                bgcolor: '#9c27b020',
                color: '#9c27b0',
                fontWeight: 600
              }}
            />
          )}
        </Box>
      </Box>

      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 0.5,
          color: 'text.secondary',
          mb: 2
        }}
      >
        <LocationOn fontSize="small" />
        <Typography variant="body2">
          {address}{address && city ? ', ' : ''}{city}
        </Typography>
      </Box>

      {/* Price section */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'baseline',
          mb: 2
        }}
      >
        <Typography
          variant="h5"
          fontWeight="bold"
          sx={{ color: '#667eea' }}
        >
          €{monthlyRent}/month
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {availableRooms}/{totalRooms} rooms
        </Typography>
      </Box>

      {/* Details row with chips */}
      <Box
        sx={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: 1,
          mb: 2
        }}
      >
        <Chip
          icon={<CalendarToday />}
          label={
            daysUntilAvailable === 0
              ? 'Available now'
              : `Available in ${daysUntilAvailable} days`
          }
          size="small"
          variant="outlined"
        />
        <Chip
          icon={<Bed />}
          label={`${totalRooms} rooms`}
          size="small"
          variant="outlined"
        />
        <Chip
          icon={<Pets />}
          label={petsAllowed ? 'Pets allowed' : 'No pets'}
          size="small"
          sx={{
            bgcolor: petsAllowed ? '#4caf5020' : '#f4433620',
            color: petsAllowed ? '#4caf50' : '#f44336'
          }}
        />
        <Chip
          icon={smokingAllowed ? <SmokingRooms /> : <SmokeFree />}
          label={smokingAllowed ? 'Smoking allowed' : 'No smoking'}
          size="small"
          sx={{
            bgcolor: smokingAllowed ? '#4caf5020' : '#f4433620',
            color: smokingAllowed ? '#4caf50' : '#f44336'
          }}
        />
      </Box>

      {/* Landlord info */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          mb: 2
        }}
      >
        <Avatar sx={{ width: 28, height: 28, fontSize: 14 }}>
          {landlordName ? landlordName.charAt(0).toUpperCase() : '?'}
        </Avatar>
        <Typography variant="body2" color="text.secondary">
          Listed by {landlordName}
        </Typography>
      </Box>

      {/* Description */}
      <Typography
        variant="body2"
        color="text.secondary"
        sx={{ mb: 2, flexGrow: 1 }}
      >
        {truncatedDescription}
      </Typography>

      <Divider sx={{ mb: 2 }} />

      {/* Action buttons */}
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
        {showInterestButton && matchId && (
          localInterested ? (
            <Button
              variant="contained"
              startIcon={<Favorite />}
              disabled
              sx={{
                bgcolor: '#4caf50',
                '&.Mui-disabled': {
                  bgcolor: '#4caf50',
                  color: 'white',
                  opacity: 0.9
                }
              }}
            >
              Interested ✓
            </Button>
          ) : (
            <Button
              variant="contained"
              startIcon={<FavoriteBorder />}
              onClick={handleInterest}
              disabled={loading}
              sx={{
                bgcolor: '#9c27b0',
                '&:hover': { bgcolor: '#7b1fa2' }
              }}
            >
              I'm Interested
            </Button>
          )
        )}

        {isAdmin && !isVerified && (
          <Button
            variant="contained"
            startIcon={<Verified />}
            onClick={() => onVerify(id)}
            sx={{
              bgcolor: '#4caf50',
              '&:hover': { bgcolor: '#388e3c' }
            }}
          >
            Verify Listing
          </Button>
        )}
      </Box>
    </Paper>
  );
};

export default ListingCard;
