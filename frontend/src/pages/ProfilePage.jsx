import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Grid, CircularProgress,
  Tabs, Tab, Paper
} from '@mui/material';
import { getMyProfile } from '../api/userApi';
import ProfileCard from '../components/profile/ProfileCard';
import PreferencesForm from '../components/profile/PreferencesForm';
import { useAuth } from '../context/AuthContext';

const ProfilePage = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState(0);
  const { hasRole } = useAuth();

  const loadProfile = async () => {
    try {
      const res = await getMyProfile();
      setProfile(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadProfile(); }, []);

  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}>
      <CircularProgress />
    </Box>
  );

  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" sx={{ mb: 3 }}>
        My Profile
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <ProfileCard profile={profile}
            onUpdated={loadProfile} />
        </Grid>

        <Grid item xs={12} md={8}>
          {hasRole('TENANT') && (
            <Paper sx={{ borderRadius: 3 }}>
              <Tabs value={tab}
                onChange={(e, v) => setTab(v)}
                sx={{
                  borderBottom: 1,
                  borderColor: 'divider', px: 2
                }}>
                <Tab label="Lifestyle Preferences" />
              </Tabs>
              <Box sx={{ p: 3 }}>
                {tab === 0 && (
                  <PreferencesForm
                    existingPreferences={profile?.preferences}
                    onSaved={loadProfile}
                  />
                )}
              </Box>
            </Paper>
          )}
          {!hasRole('TENANT') && (
            <Paper sx={{ p: 3, borderRadius: 3 }}>
              <Typography color="text.secondary">
                Profile preferences are available for tenants.
              </Typography>
            </Paper>
          )}
        </Grid>
      </Grid>
    </Box>
  );
};

export default ProfilePage;
