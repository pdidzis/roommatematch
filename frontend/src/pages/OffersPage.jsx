import React, { useState, useEffect } from 'react';
import {
  Box, Typography, Grid, Paper, Chip,
  CircularProgress, Avatar, Button,
  TextField, Dialog, DialogTitle,
  DialogContent, DialogActions
} from '@mui/material';
import {
  LocalOffer, CalendarToday, Percent,
  Add, Store
} from '@mui/icons-material';
import { getActiveOffers, createOffer } from '../api/offerApi';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

const OfferCard = ({ offer }) => {
  const daysLeft = Math.ceil(
    (new Date(offer.validUntil) - new Date()) / 86400000);

  return (
    <Paper
      sx={{
        p: 3,
        borderRadius: 3,
        boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
        transition: 'transform 0.2s',
        '&:hover': { transform: 'translateY(-2px)' },
        border: '1px solid #f0f0f0',
        display: 'flex',
        flexDirection: 'column',
        height: '100%'
      }}
    >
      {/* Top row */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          mb: 2
        }}
      >
        <Avatar sx={{ bgcolor: '#9c27b020', color: '#9c27b0' }}>
          <Store />
        </Avatar>
        <Typography variant="subtitle1" fontWeight="bold">
          {offer.partnerName}
        </Typography>
        <Box sx={{ flexGrow: 1 }} />
        <Chip
          label={`${offer.discountPercent}% OFF`}
          size="small"
          sx={{
            bgcolor: '#9c27b020',
            color: '#9c27b0',
            fontWeight: 700
          }}
        />
      </Box>

      <Typography variant="h6" fontWeight="bold" sx={{ mb: 1 }}>
        {offer.title}
      </Typography>

      <Typography
        variant="body2"
        color="text.secondary"
        sx={{ mb: 2, flexGrow: 1 }}
      >
        {offer.description}
      </Typography>

      {/* Bottom row */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          flexWrap: 'wrap'
        }}
      >
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 0.5,
            color: 'text.secondary'
          }}
        >
          <CalendarToday fontSize="small" />
          <Typography variant="body2">
            Valid for {daysLeft} days
          </Typography>
        </Box>
        {daysLeft <= 7 && (
          <Chip
            label="Expiring soon!"
            size="small"
            sx={{
              bgcolor: '#ff980020',
              color: '#ff9800',
              fontWeight: 600
            }}
          />
        )}
      </Box>
    </Paper>
  );
};

const OffersPage = () => {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [newOffer, setNewOffer] = useState({
    title: '', description: '',
    discountPercent: '', validUntil: ''
  });
  const { hasRole } = useAuth();

  useEffect(() => {
    getActiveOffers()
      .then(res => setOffers(res.data))
      .catch(() => toast.error('Failed to load offers'))
      .finally(() => setLoading(false));
  }, []);

  const handleCreate = async () => {
    try {
      await createOffer(newOffer);
      toast.success('Offer created!');
      setCreateOpen(false);
      setNewOffer({
        title: '', description: '',
        discountPercent: '', validUntil: ''
      });
      const res = await getActiveOffers();
      setOffers(res.data);
    } catch (err) {
      toast.error('Failed to create offer');
    }
  };

  const updateNewOffer = (key, value) => {
    setNewOffer(prev => ({ ...prev, [key]: value }));
  };

  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}>
      <CircularProgress />
    </Box>
  );

  return (
    <Box>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start',
          mb: 3,
          flexWrap: 'wrap',
          gap: 2
        }}
      >
        <Box>
          <Typography variant="h4" fontWeight="bold">
            Partner Offers 🎁
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Exclusive deals for RoommateMatch users
          </Typography>
        </Box>
        {hasRole('PARTNER') && (
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => setCreateOpen(true)}
            sx={{
              bgcolor: '#9c27b0',
              '&:hover': { bgcolor: '#7b1fa2' }
            }}
          >
            Add Offer
          </Button>
        )}
      </Box>

      {offers.length === 0 ? (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            py: 8,
            color: 'text.secondary'
          }}
        >
          <LocalOffer sx={{ fontSize: 64, color: '#ccc', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">
            No offers available right now
          </Typography>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {offers.map(offer => (
            <Grid item xs={12} sm={6} md={4} key={offer.id}>
              <OfferCard offer={offer} />
            </Grid>
          ))}
        </Grid>
      )}

      {/* Create Offer Dialog */}
      <Dialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Create New Offer</DialogTitle>
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
              value={newOffer.title}
              onChange={(e) => updateNewOffer('title', e.target.value)}
              fullWidth
            />
            <TextField
              label="Description"
              value={newOffer.description}
              onChange={(e) =>
                updateNewOffer('description', e.target.value)}
              multiline
              rows={3}
              fullWidth
            />
            <TextField
              label="Discount Percent (%)"
              type="number"
              value={newOffer.discountPercent}
              onChange={(e) =>
                updateNewOffer('discountPercent', e.target.value)}
              fullWidth
            />
            <TextField
              label="Valid Until"
              type="date"
              value={newOffer.validUntil}
              onChange={(e) =>
                updateNewOffer('validUntil', e.target.value)}
              fullWidth
              InputLabelProps={{ shrink: true }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
          <Button
            variant="contained"
            onClick={handleCreate}
            sx={{ bgcolor: '#9c27b0', '&:hover': { bgcolor: '#7b1fa2' } }}
          >
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OffersPage;
