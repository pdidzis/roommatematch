import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Grid, CircularProgress,
  Alert, Button, Tabs, Tab, Fab, Dialog,
  DialogTitle, DialogContent, DialogActions,
  TextField, Switch, FormControlLabel
} from '@mui/material';
import { Add, Apartment } from '@mui/icons-material';
import {
  getPublicListings, getMyListings,
  createListing, verifyListing,
  getListingsForRoommates, expressInterest
} from '../api/listingApi';
import { getMyMatches } from '../api/matchApi';
import ListingCard from '../components/listings/ListingCard';
import ListingFilters from '../components/listings/ListingFilters';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

const emptyListing = {
  title: '', description: '', address: '',
  city: '', country: 'Latvia', monthlyRent: '',
  availableFrom: '', totalRooms: 1, availableRooms: 1,
  petsAllowed: false, smokingAllowed: false
};

const ListingsPage = () => {
  const [tab, setTab] = useState(0);
  const [listings, setListings] = useState([]);
  const [myListings, setMyListings] = useState([]);
  const [roommateListings, setRoommateListings] = useState([]);
  const [confirmedMatch, setConfirmedMatch] = useState(null);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({
    city: '', maxRent: '', petsAllowed: '', smokingAllowed: ''
  });
  const [createOpen, setCreateOpen] = useState(false);
  const [newListing, setNewListing] = useState({ ...emptyListing });
  const { hasRole, user } = useAuth();
  const navigate = useNavigate();

  const loadData = async () => {
    setLoading(true);
    try {
      const params = {};
      if (filters.city) params.city = filters.city;
      if (filters.maxRent) params.maxRent = filters.maxRent;
      if (filters.petsAllowed !== '')
        params.petsAllowed = filters.petsAllowed;
      if (filters.smokingAllowed !== '')
        params.smokingAllowed = filters.smokingAllowed;

      const publicRes = await getPublicListings(params);
      setListings(publicRes.data);

      if (hasRole('LANDLORD')) {
        const myRes = await getMyListings();
        setMyListings(myRes.data);
      }

      if (hasRole('TENANT')) {
        const matchesRes = await getMyMatches();
        const confirmed = matchesRes.data.find(
          m => m.status === 'ROOMMATE_CONFIRMED');
        setConfirmedMatch(confirmed || null);

        if (confirmed) {
          try {
            const roommateRes = await getListingsForRoommates();
            setRoommateListings(roommateRes.data);
          } catch (err) {
            console.log('No roommate listings available');
          }
        }
      }
    } catch (err) {
      toast.error('Failed to load listings');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  const handleFilterChange = (key, value) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  const handleClearFilters = () => {
    setFilters({
      city: '', maxRent: '', petsAllowed: '', smokingAllowed: ''
    });
  };

  const handleInterest = async (listingId, matchId) => {
    try {
      const res = await expressInterest(listingId, matchId);
      toast.success(res.data);
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message
        || 'Failed to express interest');
    }
  };

  const handleCreateListing = async () => {
    try {
      await createListing(newListing);
      toast.success('Listing created! Waiting for verification.');
      setCreateOpen(false);
      setNewListing({ ...emptyListing });
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message
        || 'Failed to create listing');
    }
  };

  const handleVerify = async (listingId) => {
    try {
      await verifyListing(listingId);
      toast.success('Listing verified!');
      loadData();
    } catch (err) {
      toast.error('Failed to verify listing');
    }
  };

  const updateNewListing = (key, value) => {
    setNewListing(prev => ({ ...prev, [key]: value }));
  };

  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}>
      <CircularProgress />
    </Box>
  );

  const tabs = ['All Listings'];
  if (hasRole('TENANT') && confirmedMatch)
    tabs.push('Browse With Roommate');
  if (hasRole('LANDLORD')) tabs.push('My Listings');
  if (hasRole('ADMIN')) tabs.push('Pending Verification');

  const currentTabLabel = tabs[tab];
  const pendingListings = listings.filter(l => !l.isVerified);

  return (
    <Box>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          mb: 3,
          flexWrap: 'wrap',
          gap: 2
        }}
      >
        <Typography variant="h4" fontWeight="bold">
          Find Your Perfect Home 🏠
        </Typography>
        {hasRole('LANDLORD') && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setCreateOpen(true)}
            sx={{
              bgcolor: '#667eea',
              '&:hover': { bgcolor: '#5568d3' }
            }}
          >
            Add Listing
          </Button>
        )}
      </Box>

      <ListingFilters
        filters={filters}
        onChange={handleFilterChange}
        onClear={handleClearFilters}
      />

      <Tabs
        value={tab}
        onChange={(e, v) => setTab(v)}
        sx={{ mb: 3 }}
      >
        {tabs.map((label) => (
          <Tab key={label} label={label} />
        ))}
      </Tabs>

      {/* Tab: All Listings */}
      {currentTabLabel === 'All Listings' && (
        listings.length === 0 ? (
          <Alert severity="info">No listings found</Alert>
        ) : (
          <Grid container spacing={3}>
            {listings.map(listing => (
              <Grid item xs={12} sm={6} md={4} key={listing.id}>
                <ListingCard
                  listing={listing}
                  isAdmin={hasRole('ADMIN')}
                  onVerify={handleVerify}
                />
              </Grid>
            ))}
          </Grid>
        )
      )}

      {/* Tab: Browse With Roommate */}
      {currentTabLabel === 'Browse With Roommate' && (
        <Box>
          <Alert severity="info" sx={{ mb: 3 }}>
            You are browsing with your confirmed roommate.
            Express interest in listings you like. When you both
            like the same listing, a landlord chat is created! 🏠
          </Alert>
          {roommateListings.length === 0 ? (
            <Alert severity="info">No listings found</Alert>
          ) : (
            <Grid container spacing={3}>
              {roommateListings.map(listing => (
                <Grid item xs={12} sm={6} md={4} key={listing.id}>
                  <ListingCard
                    listing={listing}
                    showInterestButton={true}
                    matchId={confirmedMatch?.matchId}
                    onInterest={handleInterest}
                    currentUserInterested={listing.currentUserInterested}
                  />
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}

      {/* Tab: My Listings */}
      {currentTabLabel === 'My Listings' && (
        myListings.length === 0 ? (
          <Alert severity="info">
            No listings yet. Create your first one!
          </Alert>
        ) : (
          <Grid container spacing={3}>
            {myListings.map(listing => (
              <Grid item xs={12} sm={6} md={4} key={listing.id}>
                <ListingCard listing={listing} />
              </Grid>
            ))}
          </Grid>
        )
      )}

      {/* Tab: Pending Verification */}
      {currentTabLabel === 'Pending Verification' && (
        pendingListings.length === 0 ? (
          <Alert severity="info">
            No listings pending verification
          </Alert>
        ) : (
          <Grid container spacing={3}>
            {pendingListings.map(listing => (
              <Grid item xs={12} sm={6} md={4} key={listing.id}>
                <ListingCard
                  listing={listing}
                  isAdmin={true}
                  onVerify={handleVerify}
                />
              </Grid>
            ))}
          </Grid>
        )
      )}

      {/* Create Listing Dialog */}
      <Dialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Create New Listing</DialogTitle>
        <DialogContent>
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              gap: 2,
              mt: 1
            }}
          >
            <TextField
              label="Title"
              value={newListing.title}
              onChange={(e) => updateNewListing('title', e.target.value)}
              fullWidth
            />
            <TextField
              label="Description"
              value={newListing.description}
              onChange={(e) =>
                updateNewListing('description', e.target.value)}
              multiline
              rows={3}
              fullWidth
            />
            <TextField
              label="Address"
              value={newListing.address}
              onChange={(e) =>
                updateNewListing('address', e.target.value)}
              fullWidth
            />
            <TextField
              label="City"
              value={newListing.city}
              onChange={(e) => updateNewListing('city', e.target.value)}
              fullWidth
            />
            <TextField
              label="Country"
              value={newListing.country}
              onChange={(e) =>
                updateNewListing('country', e.target.value)}
              fullWidth
            />
            <TextField
              label="Monthly Rent (€)"
              type="number"
              value={newListing.monthlyRent}
              onChange={(e) =>
                updateNewListing('monthlyRent', e.target.value)}
              fullWidth
            />
            <TextField
              label="Available From"
              type="date"
              value={newListing.availableFrom}
              onChange={(e) =>
                updateNewListing('availableFrom', e.target.value)}
              fullWidth
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              label="Total Rooms"
              type="number"
              value={newListing.totalRooms}
              onChange={(e) =>
                updateNewListing('totalRooms', e.target.value)}
              fullWidth
            />
            <TextField
              label="Available Rooms"
              type="number"
              value={newListing.availableRooms}
              onChange={(e) =>
                updateNewListing('availableRooms', e.target.value)}
              fullWidth
            />
            <FormControlLabel
              control={
                <Switch
                  checked={newListing.petsAllowed}
                  onChange={(e) =>
                    updateNewListing('petsAllowed', e.target.checked)}
                />
              }
              label="Pets Allowed"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={newListing.smokingAllowed}
                  onChange={(e) =>
                    updateNewListing('smokingAllowed', e.target.checked)}
                />
              }
              label="Smoking Allowed"
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleCreateListing}
            sx={{ bgcolor: '#667eea', '&:hover': { bgcolor: '#5568d3' } }}
          >
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ListingsPage;
