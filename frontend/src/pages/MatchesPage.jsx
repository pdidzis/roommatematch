import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Tabs, Tab, Grid,
  CircularProgress, Alert, Button,
  TextField, InputAdornment
} from '@mui/material';
import { Search, Refresh } from '@mui/icons-material';
import { getSuggestions, getMyMatches } from '../api/matchApi';
import { getMyProfile } from '../api/userApi';
import SuggestionCard from '../components/matches/SuggestionCard';
import MatchCard from '../components/matches/MatchCard';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

const MatchesPage = () => {
  const [tab, setTab] = useState(0);
  const [suggestions, setSuggestions] = useState([]);
  const [matches, setMatches] = useState([]);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  const currentUserId = user?.userId ?? user?.id;

  const loadData = async () => {
    setLoading(true);
    try {
      const [profileRes, matchesRes] = await Promise.all([
        getMyProfile(),
        getMyMatches()
      ]);
      setProfile(profileRes.data);
      setMatches(matchesRes.data || []);

      if (profileRes.data?.preferences?.city) {
        try {
          const suggestionsRes = await getSuggestions();
          setSuggestions(suggestionsRes.data || []);
        } catch (err) {
          if (err.response?.status !== 400) {
            toast.error('Failed to load suggestions');
          }
        }
      }
    } catch (err) {
      toast.error('Failed to load matches data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSuggestionRequested = (userId) => {
    setSuggestions(prev =>
      prev.filter(s => s.otherUser.userId !== userId));
    loadData();
  };

  const filteredSuggestions = suggestions.filter(s =>
    (s.otherUser.firstName || '').toLowerCase()
      .includes(search.toLowerCase()) ||
    (s.otherUser.lastName || '').toLowerCase()
      .includes(search.toLowerCase()) ||
    (s.otherUser.city || '').toLowerCase()
      .includes(search.toLowerCase())
  );

  const pendingCount = matches.filter(
    m => m.status === 'PENDING').length;
  const acceptedCount = matches.filter(
    m => m.status === 'ACCEPTED' ||
      m.status === 'ROOMMATE_CONFIRMED').length;

  const pendingMatches = matches.filter(m => m.status === 'PENDING');
  const acceptedMatches = matches.filter(
    m => m.status === 'ACCEPTED' || m.status === 'ROOMMATE_CONFIRMED'
  );

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" mt={4}>
        <CircularProgress />
      </Box>
    );
  }

  if (!profile?.preferences?.city) {
    return (
      <Box sx={{ p: { xs: 2, sm: 3 } }}>
        <Alert
          severity="info"
          sx={{ mb: 3 }}
          action={
            <Button
              color="inherit"
              size="small"
              onClick={() => navigate('/profile')}
            >
              Set Preferences
            </Button>
          }
        >
          Complete your preferences to see match suggestions!
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: { xs: 2, sm: 3 } }}>
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        mb: 3,
        flexWrap: 'wrap',
        gap: 2
      }}>
        <Box>
          <Typography
            variant="h4"
            fontWeight="bold"
            sx={{
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent'
            }}
          >
            Find Your Roommate
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Discover compatible roommates and manage your matches
          </Typography>
        </Box>
        <Button
          variant="outlined"
          startIcon={<Refresh />}
          onClick={loadData}
          sx={{ textTransform: 'none' }}
        >
          Refresh
        </Button>
      </Box>

      <Tabs
        value={tab}
        onChange={(_, v) => setTab(v)}
        sx={{ mb: 3, borderBottom: 1, borderColor: 'divider' }}
        variant="scrollable"
        scrollButtons="auto"
      >
        <Tab label={`Suggestions (${filteredSuggestions.length})`} />
        <Tab label={`Pending (${pendingCount})`} />
        <Tab label={`My Matches (${acceptedCount})`} />
      </Tabs>

      {tab === 0 && (
        <Box>
          <TextField
            placeholder="Search by name or city..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            fullWidth
            sx={{ mb: 3 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <Search />
                </InputAdornment>
              )
            }}
          />
          {filteredSuggestions.length === 0 ? (
            <Alert severity="info">
              No suggestions found. Try updating your preferences.
            </Alert>
          ) : (
            <Grid container spacing={3}>
              {filteredSuggestions.map((s) => (
                <Grid item xs={12} sm={6} md={4}
                  key={s.otherUser.userId}>
                  <SuggestionCard
                    match={s}
                    onRequested={handleSuggestionRequested}
                  />
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}

      {tab === 1 && (
        <Box>
          {pendingMatches.length === 0 ? (
            <Typography
              variant="body1"
              color="text.secondary"
              sx={{ textAlign: 'center', mt: 4 }}
            >
              No pending match requests
            </Typography>
          ) : (
            <Grid container spacing={3}>
              {pendingMatches.map((m) => (
                <Grid item xs={12} sm={6} md={4} key={m.matchId}>
                  <MatchCard
                    match={m}
                    currentUserId={currentUserId}
                    onUpdated={loadData}
                  />
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}

      {tab === 2 && (
        <Box>
          {acceptedMatches.length === 0 ? (
            <Typography
              variant="body1"
              color="text.secondary"
              sx={{ textAlign: 'center', mt: 4 }}
            >
              No accepted matches yet
            </Typography>
          ) : (
            <Grid container spacing={3}>
              {acceptedMatches.map((m) => (
                <Grid item xs={12} sm={6} md={4} key={m.matchId}>
                  <MatchCard
                    match={m}
                    currentUserId={currentUserId}
                    onUpdated={loadData}
                  />
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}
    </Box>
  );
};

export default MatchesPage;
