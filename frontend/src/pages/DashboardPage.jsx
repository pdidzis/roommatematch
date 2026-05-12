import React, { useState, useEffect } from 'react';
import {
  Box, Grid, Typography, CircularProgress,
  Divider, Alert, Button
} from '@mui/material';
import {
  People, Chat, Apartment, CheckCircle,
  TrendingUp
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getMyMatches } from '../api/matchApi';
import { getMyChatRooms } from '../api/chatApi';
import { getMyProfile } from '../api/userApi';
import StatsCard from '../components/dashboard/StatsCard';
import QuickActionCard from '../components/dashboard/QuickActionCard';
import RecentMatchCard from '../components/dashboard/RecentMatchCard';

const DashboardPage = () => {
  const { user, hasRole } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const [chatRooms, setChatRooms] = useState([]);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const [profileRes, matchesRes, chatsRes] =
          await Promise.all([
            getMyProfile(),
            hasRole('TENANT') ? getMyMatches() :
              Promise.resolve({ data: [] }),
            getMyChatRooms()
          ]);
        setProfile(profileRes.data);
        setMatches(matchesRes.data || []);
        setChatRooms(chatsRes.data || []);
      } catch (err) {
        setError('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };
    loadDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}>
      <CircularProgress />
    </Box>
  );

  const confirmedMatches = matches.filter(
    m => m.status === 'ROOMMATE_CONFIRMED');
  const acceptedMatches = matches.filter(
    m => m.status === 'ACCEPTED');
  const pendingMatches = matches.filter(
    m => m.status === 'PENDING');
  const unreadChats = chatRooms.filter(
    r => r.unreadCount > 0);

  const profileIncomplete = !profile?.preferences?.city;

  const tenantActions = [
    {
      title: 'Find Roommates',
      description: 'Browse compatible roommate matches based on your lifestyle preferences.',
      buttonText: 'View Matches',
      onClick: () => navigate('/matches'),
      icon: <People />,
      color: '#667eea'
    },
    {
      title: 'Your Chats',
      description: unreadChats.length > 0
        ? `You have ${unreadChats.length} unread messages.`
        : 'Stay connected with your matches.',
      buttonText: 'Open Chat',
      onClick: () => navigate('/chat'),
      icon: <Chat />,
      color: '#4caf50'
    },
    {
      title: 'Browse Listings',
      description: confirmedMatches.length > 0
        ? 'You have a confirmed roommate! Browse listings together.'
        : 'Confirm a roommate first to unlock listings.',
      buttonText: confirmedMatches.length > 0
        ? 'Browse Listings' : 'Find Roommate First',
      onClick: () => confirmedMatches.length > 0
        ? navigate('/listings') : navigate('/matches'),
      icon: <Apartment />,
      color: '#ff9800'
    }
  ];

  const landlordActions = [
    {
      title: 'My Listings',
      description: 'Manage your property listings.',
      buttonText: 'Manage Listings',
      onClick: () => navigate('/listings'),
      icon: <Apartment />,
      color: '#667eea'
    },
    {
      title: 'Tenant Chats',
      description: 'Communicate with interested tenants.',
      buttonText: 'Open Chat',
      onClick: () => navigate('/chat'),
      icon: <Chat />,
      color: '#4caf50'
    }
  ];

  const actions = hasRole('TENANT') ? tenantActions
    : hasRole('LANDLORD') ? landlordActions : [];

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
          {error}
        </Alert>
      )}

      {hasRole('TENANT') && profileIncomplete && (
        <Alert severity="warning" sx={{ mb: 3, borderRadius: 2 }}
          action={
            <Button color="inherit" size="small"
              onClick={() => navigate('/profile')}>
              Complete Now
            </Button>
          }>
          Your profile is incomplete. Add your preferences
          to start matching!
        </Alert>
      )}

      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" fontWeight="bold">
          Welcome back, {user?.firstName}! 👋
        </Typography>
        <Typography color="text.secondary">
          Here's what's happening with your roommate search.
        </Typography>
      </Box>

      {hasRole('TENANT') && (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6} md={3}>
            <StatsCard title="Total Matches"
              value={matches.length}
              icon={<People />} color="#667eea" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatsCard title="Accepted"
              value={acceptedMatches.length}
              icon={<CheckCircle />} color="#4caf50" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatsCard title="Pending"
              value={pendingMatches.length}
              icon={<TrendingUp />} color="#ff9800" />
          </Grid>
          <Grid item xs={12} sm={6} md={3}>
            <StatsCard title="Confirmed Roommates"
              value={confirmedMatches.length}
              icon={<Apartment />} color="#9c27b0" />
          </Grid>
        </Grid>
      )}

      {actions.length > 0 && (
        <Box sx={{ mb: 4 }}>
          <Typography variant="h6" fontWeight="bold"
            sx={{ mb: 2 }}>
            Quick Actions
          </Typography>
          <Grid container spacing={3}>
            {actions.map((action, index) => (
              <Grid item xs={12} sm={6} md={4} key={index}>
                <QuickActionCard {...action} />
              </Grid>
            ))}
          </Grid>
        </Box>
      )}

      {hasRole('TENANT') && matches.length > 0 && (
        <Box>
          <Divider sx={{ mb: 3 }} />
          <Typography variant="h6" fontWeight="bold"
            sx={{ mb: 2 }}>
            Recent Matches
          </Typography>
          <Box sx={{
            display: 'flex', flexDirection: 'column',
            gap: 2
          }}>
            {matches.slice(0, 5).map(match => (
              <RecentMatchCard key={match.matchId}
                match={match} />
            ))}
          </Box>
        </Box>
      )}
    </Box>
  );
};

export default DashboardPage;
