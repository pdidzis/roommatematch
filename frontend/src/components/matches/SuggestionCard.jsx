import React, { useState } from 'react';
import {
  Paper, Box, Avatar, Typography, Button,
  Chip, Collapse, IconButton, Tooltip
} from '@mui/material';
import {
  ExpandMore, ExpandLess, PersonAdd,
  LocationOn, AttachMoney, Pets, SmokeFree,
  SmokingRooms, CalendarMonth
} from '@mui/icons-material';
import MatchScoreRing from './MatchScoreRing';
import ScoreBreakdown from './ScoreBreakdown';
import { requestMatch } from '../../api/matchApi';
import toast from 'react-hot-toast';

const SuggestionCard = ({ match, onRequested }) => {
  const [expanded, setExpanded] = useState(false);
  const [loading, setLoading] = useState(false);
  const { otherUser, compatibilityScore,
    confidenceScore, scoreBreakdown } = match;

  const handleRequest = async () => {
    setLoading(true);
    try {
      await requestMatch(otherUser.userId);
      toast.success(`Match request sent to ${otherUser.firstName}!`);
      if (onRequested) onRequested(otherUser.userId);
    } catch (err) {
      const msg = err.response?.data?.message
        || 'Failed to send request';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  const formatBudget = () => {
    const min = otherUser?.budgetMin;
    const max = otherUser?.budgetMax;
    if (min == null && max == null) return null;
    return `€${min ?? '?'} - €${max ?? '?'}/month`;
  };

  const formatMoveIn = () => {
    const date = otherUser?.moveInDate;
    if (!date) return null;
    try {
      return new Date(date).toLocaleDateString();
    } catch {
      return date;
    }
  };

  const hasPets = otherUser?.hasPets;
  const smokes = otherUser?.smokes;

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
        <Box sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center'
        }}>
          <MatchScoreRing score={compatibilityScore} />
        </Box>
      </Box>

      {confidenceScore != null && (
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ mt: 1, textAlign: 'right', display: 'block' }}
        >
          Based on {Math.round(confidenceScore * 100)}% profile completion
        </Typography>
      )}

      <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
        {formatBudget() && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <AttachMoney sx={{ fontSize: 18, color: 'text.secondary' }} />
            <Typography variant="body2">{formatBudget()}</Typography>
          </Box>
        )}
        {formatMoveIn() && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <CalendarMonth sx={{ fontSize: 18, color: 'text.secondary' }} />
            <Typography variant="body2">
              Move-in: {formatMoveIn()}
            </Typography>
          </Box>
        )}
        <Box sx={{ display: 'flex', gap: 1, mt: 0.5 }}>
          <Tooltip title={hasPets ? 'Has pets' : 'No pets'}>
            <Chip
              size="small"
              icon={<Pets sx={{ fontSize: 16 }} />}
              label={hasPets ? 'Pets' : 'No pets'}
              color={hasPets ? 'primary' : 'default'}
              variant={hasPets ? 'filled' : 'outlined'}
            />
          </Tooltip>
          <Tooltip title={smokes ? 'Smokes' : 'Non-smoker'}>
            <Chip
              size="small"
              icon={smokes
                ? <SmokingRooms sx={{ fontSize: 16 }} />
                : <SmokeFree sx={{ fontSize: 16 }} />}
              label={smokes ? 'Smoker' : 'Non-smoker'}
              color={smokes ? 'warning' : 'success'}
              variant="outlined"
            />
          </Tooltip>
        </Box>
      </Box>

      <Box sx={{
        mt: 2,
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <Typography variant="caption" color="text.secondary">
          {expanded ? 'Hide details' : 'See compatibility details'}
        </Typography>
        <IconButton
          size="small"
          onClick={() => setExpanded(!expanded)}
          aria-label="toggle breakdown"
        >
          {expanded ? <ExpandLess /> : <ExpandMore />}
        </IconButton>
      </Box>
      <Collapse in={expanded} timeout="auto" unmountOnExit>
        <ScoreBreakdown breakdown={scoreBreakdown} />
      </Collapse>

      <Box sx={{ flexGrow: 1 }} />

      <Button
        variant="contained"
        startIcon={<PersonAdd />}
        onClick={handleRequest}
        disabled={loading}
        sx={{
          mt: 2,
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          fontWeight: 'bold',
          textTransform: 'none'
        }}
        fullWidth
      >
        {loading ? 'Sending...' : 'Send Match Request'}
      </Button>
    </Paper>
  );
};

export default SuggestionCard;
