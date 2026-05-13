import React, { useState } from 'react';
import {
  Paper, Box, Avatar, Typography, Button,
  Chip, Divider, Stack
} from '@mui/material';
import {
  Check, Close, Chat, Home, Handshake, LocationOn
} from '@mui/icons-material';
import MatchScoreRing from './MatchScoreRing';
import { respondToMatch } from '../../api/matchApi';
import { confirmRoommate } from '../../api/journeyApi';
import { openChatRoom } from '../../api/chatApi';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

const statusConfig = {
  PENDING: { label: 'Pending', color: '#ff9800' },
  ACCEPTED: { label: 'Accepted ✓', color: '#4caf50' },
  DECLINED: { label: 'Declined', color: '#f44336' },
  ROOMMATE_CONFIRMED: {
    label: '🏠 Roommates!', color: '#9c27b0'
  }
};

const MatchCard = ({ match, currentUserId, onUpdated }) => {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { matchId, otherUser, compatibilityScore,
    status } = match;

  const isReceiver = match.receiverId === currentUserId;
  const isPending = status === 'PENDING';
  const isAccepted = status === 'ACCEPTED';
  const isConfirmed = status === 'ROOMMATE_CONFIRMED';

  const handleRespond = async (accept) => {
    setLoading(true);
    try {
      await respondToMatch(matchId, accept);
      toast.success(accept
        ? 'Match accepted! You can now chat.'
        : 'Match declined.');
      if (onUpdated) onUpdated();
    } catch (err) {
      toast.error(err.response?.data?.message
        || 'Action failed');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmRoommate = async () => {
    setLoading(true);
    try {
      await confirmRoommate(matchId);
      toast.success('Roommate confirmed! 🏠 ' +
        'You can now browse listings together!');
      if (onUpdated) onUpdated();
    } catch (err) {
      toast.error(err.response?.data?.message
        || 'Failed to confirm');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenChat = async () => {
    try {
      const res = await openChatRoom(otherUser.userId);
      navigate('/chat', {
        state: { activeChatId: res.data.chatRoomId }
      });
    } catch (err) {
      toast.error('Failed to open chat');
    }
  };

  const cfg = statusConfig[status] || { label: status, color: '#9e9e9e' };

  return (
    <Paper sx={{
      p: 3,
      borderRadius: 3,
      boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
      transition: 'transform 0.2s',
      '&:hover': { transform: 'translateY(-2px)' },
      display: 'flex',
      flexDirection: 'column',
      height: '100%'
    }}>
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: 2
      }}>
        <Box sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 2,
          minWidth: 0
        }}>
          <Avatar
            src={otherUser?.profilePhotoUrl}
            sx={{ width: 56, height: 56 }}
          >
            {otherUser?.firstName?.[0]}
          </Avatar>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="subtitle1" fontWeight="bold" noWrap>
              {otherUser?.firstName} {otherUser?.lastName}
            </Typography>
            {otherUser?.city && (
              <Box sx={{
                display: 'flex',
                alignItems: 'center',
                color: 'text.secondary'
              }}>
                <LocationOn sx={{ fontSize: 14, mr: 0.5 }} />
                <Typography variant="caption" noWrap>
                  {otherUser.city}
                </Typography>
              </Box>
            )}
          </Box>
        </Box>
        <MatchScoreRing score={compatibilityScore} />
      </Box>

      <Box sx={{ mt: 2 }}>
        <Chip
          label={cfg.label}
          sx={{
            bgcolor: cfg.color,
            color: 'white',
            fontWeight: 'bold'
          }}
          size="small"
        />
      </Box>

      <Divider sx={{ my: 2 }} />

      <Box sx={{ flexGrow: 1 }} />

      {isPending && isReceiver && (
        <Stack direction="row" spacing={1}>
          <Button
            variant="contained"
            color="success"
            startIcon={<Check />}
            onClick={() => handleRespond(true)}
            disabled={loading}
            fullWidth
            sx={{ textTransform: 'none', fontWeight: 'bold' }}
          >
            Accept
          </Button>
          <Button
            variant="outlined"
            color="error"
            startIcon={<Close />}
            onClick={() => handleRespond(false)}
            disabled={loading}
            fullWidth
            sx={{ textTransform: 'none', fontWeight: 'bold' }}
          >
            Decline
          </Button>
        </Stack>
      )}

      {isPending && !isReceiver && (
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ textAlign: 'center', fontStyle: 'italic' }}
        >
          Waiting for response...
        </Typography>
      )}

      {isAccepted && (
        <Stack direction="column" spacing={1}>
          <Button
            variant="outlined"
            startIcon={<Chat />}
            onClick={handleOpenChat}
            disabled={loading}
            fullWidth
            sx={{ textTransform: 'none', fontWeight: 'bold' }}
          >
            Chat
          </Button>
          <Button
            variant="contained"
            startIcon={<Handshake />}
            onClick={handleConfirmRoommate}
            disabled={loading}
            fullWidth
            sx={{
              textTransform: 'none',
              fontWeight: 'bold',
              bgcolor: '#9c27b0',
              '&:hover': { bgcolor: '#7b1fa2' }
            }}
          >
            Confirm Roommate
          </Button>
        </Stack>
      )}

      {isConfirmed && (
        <Box>
          <Typography
            variant="body2"
            sx={{
              textAlign: 'center',
              color: '#9c27b0',
              fontWeight: 'bold',
              mb: 1.5
            }}
          >
            🎉 You're roommates! Time to find a place.
          </Typography>
          <Stack direction="column" spacing={1}>
            <Button
              variant="outlined"
              startIcon={<Chat />}
              onClick={handleOpenChat}
              disabled={loading}
              fullWidth
              sx={{ textTransform: 'none', fontWeight: 'bold' }}
            >
              Chat
            </Button>
            <Button
              variant="contained"
              startIcon={<Home />}
              onClick={() => navigate('/listings')}
              fullWidth
              sx={{
                textTransform: 'none',
                fontWeight: 'bold',
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
              }}
            >
              Browse Listings
            </Button>
          </Stack>
        </Box>
      )}
    </Paper>
  );
};

export default MatchCard;
