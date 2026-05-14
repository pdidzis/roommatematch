import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Grid, Paper, CircularProgress,
  Divider, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow
} from '@mui/material';
import {
  People, Apartment, Chat, CheckCircle,
  TrendingUp, AdminPanelSettings
} from '@mui/icons-material';
import { getAdminStats } from '../api/adminApi';
import StatsCard from '../components/dashboard/StatsCard';
import toast from 'react-hot-toast';

const roleColors = {
  TENANT: '#667eea',
  LANDLORD: '#4caf50',
  ADMIN: '#f44336',
  PARTNER: '#9c27b0'
};

const statusColors = {
  PENDING: '#ff9800',
  ACCEPTED: '#2196f3',
  DECLINED: '#f44336',
  ROOMMATE_CONFIRMED: '#9c27b0',
  CANCELLED: '#999'
};

const AdminPage = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAdminStats()
      .then(res => setStats(res.data))
      .catch(() => toast.error('Failed to load stats'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}>
      <CircularProgress />
    </Box>
  );

  if (!stats) return (
    <Box mt={4}>
      <Typography color="text.secondary">
        No statistics available.
      </Typography>
    </Box>
  );

  const usersByRole = stats.usersByRole || {};
  const matchesByStatus = stats.matchesByStatus || {};

  return (
    <Box>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          mb: 3
        }}
      >
        <AdminPanelSettings sx={{ fontSize: 36, color: '#f44336' }} />
        <Typography variant="h4" fontWeight="bold">
          Admin Dashboard
        </Typography>
      </Box>

      {/* Top stats row */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Total Users"
            value={stats.totalUsers ?? 0}
            icon={<People />}
            color="#667eea"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Total Listings"
            value={stats.totalListings ?? 0}
            icon={<Apartment />}
            color="#4caf50"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Total Matches"
            value={stats.totalMatches ?? 0}
            icon={<TrendingUp />}
            color="#9c27b0"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatsCard
            title="Confirmed Roommates"
            value={stats.confirmedRoommates ?? 0}
            icon={<CheckCircle />}
            color="#ff9800"
          />
        </Grid>
      </Grid>

      {/* Two tables side by side */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={6}>
          <TableContainer
            component={Paper}
            sx={{
              borderRadius: 3,
              boxShadow: '0 4px 20px rgba(0,0,0,0.08)'
            }}
          >
            <Box sx={{ p: 2 }}>
              <Typography variant="h6" fontWeight="bold">
                Users by Role
              </Typography>
            </Box>
            <Divider />
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Role</TableCell>
                  <TableCell align="right">Count</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {Object.entries(usersByRole).map(([role, count]) => (
                  <TableRow key={role}>
                    <TableCell>
                      <Chip
                        label={role}
                        size="small"
                        sx={{
                          bgcolor: (roleColors[role] || '#999') + '20',
                          color: roleColors[role] || '#999',
                          fontWeight: 600
                        }}
                      />
                    </TableCell>
                    <TableCell align="right">{count}</TableCell>
                  </TableRow>
                ))}
                {Object.keys(usersByRole).length === 0 && (
                  <TableRow>
                    <TableCell colSpan={2} align="center">
                      No data
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>

        <Grid item xs={12} md={6}>
          <TableContainer
            component={Paper}
            sx={{
              borderRadius: 3,
              boxShadow: '0 4px 20px rgba(0,0,0,0.08)'
            }}
          >
            <Box sx={{ p: 2 }}>
              <Typography variant="h6" fontWeight="bold">
                Matches by Status
              </Typography>
            </Box>
            <Divider />
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Count</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {Object.entries(matchesByStatus).map(([status, count]) => (
                  <TableRow key={status}>
                    <TableCell>
                      <Chip
                        label={status}
                        size="small"
                        sx={{
                          bgcolor: (statusColors[status] || '#999') + '20',
                          color: statusColors[status] || '#999',
                          fontWeight: 600
                        }}
                      />
                    </TableCell>
                    <TableCell align="right">{count}</TableCell>
                  </TableRow>
                ))}
                {Object.keys(matchesByStatus).length === 0 && (
                  <TableRow>
                    <TableCell colSpan={2} align="center">
                      No data
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>
      </Grid>

      {/* Bottom stats row */}
      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={4}>
          <StatsCard
            title="Total Messages"
            value={stats.totalMessages ?? 0}
            icon={<Chat />}
            color="#2196f3"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatsCard
            title="Average Compatibility Score"
            value={
              stats.averageCompatibilityScore != null
                ? (stats.averageCompatibilityScore * 100).toFixed(1) + '%'
                : 'N/A'
            }
            icon={<TrendingUp />}
            color="#9c27b0"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={4}>
          <StatsCard
            title="Total Landlord Chats"
            value={stats.totalLandlordChats ?? 0}
            icon={<Chat />}
            color="#4caf50"
          />
        </Grid>
      </Grid>
    </Box>
  );
};

export default AdminPage;
