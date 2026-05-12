import React, { useState, useRef } from 'react';
import {
  Paper, Box, Avatar, Typography, Button,
  TextField, Chip, IconButton, CircularProgress
} from '@mui/material';
import { Edit, Save, Cancel, PhotoCamera } from '@mui/icons-material';
import { updateProfile, uploadProfilePhoto }
  from '../../api/userApi';
import toast from 'react-hot-toast';

const roleColors = {
  TENANT: '#667eea',
  LANDLORD: '#4caf50',
  PARTNER: '#ff9800',
  ADMIN: '#f44336'
};

const ProfileCard = ({ profile, onUpdated }) => {
  const [editing, setEditing] = useState(false);
  const [formData, setFormData] = useState({
    firstName: profile?.firstName || '',
    lastName: profile?.lastName || '',
    phoneNumber: profile?.phoneNumber || ''
  });
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  const handleEdit = () => {
    setFormData({
      firstName: profile?.firstName || '',
      lastName: profile?.lastName || '',
      phoneNumber: profile?.phoneNumber || ''
    });
    setEditing(true);
  };

  const handleCancel = () => {
    setEditing(false);
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      await updateProfile(formData);
      toast.success('Profile updated!');
      setEditing(false);
      if (onUpdated) onUpdated();
    } catch (err) {
      toast.error('Failed to update profile');
    } finally {
      setLoading(false);
    }
  };

  const handlePhotoUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const fd = new FormData();
    fd.append('file', file);
    setUploading(true);
    try {
      await uploadProfilePhoto(fd);
      toast.success('Photo uploaded!');
      if (onUpdated) onUpdated();
    } catch (err) {
      toast.error('Failed to upload photo');
    } finally {
      setUploading(false);
    }
  };

  const triggerPhotoSelect = () => {
    if (fileInputRef.current) fileInputRef.current.click();
  };

  const role = profile?.role || 'TENANT';
  const roleColor = roleColors[role] || '#667eea';

  return (
    <Paper sx={{
      p: 3,
      borderRadius: 3,
      boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: 2
    }}>
      <Box sx={{ position: 'relative' }}>
        <Avatar
          src={profile?.profilePhotoUrl}
          onClick={triggerPhotoSelect}
          sx={{
            width: 100,
            height: 100,
            cursor: 'pointer',
            fontSize: 40,
            bgcolor: roleColor
          }}
        >
          {profile?.firstName?.[0]}
        </Avatar>
        <IconButton
          onClick={triggerPhotoSelect}
          disabled={uploading}
          sx={{
            position: 'absolute',
            bottom: 0,
            right: 0,
            bgcolor: 'background.paper',
            border: '2px solid white',
            boxShadow: '0 2px 8px rgba(0,0,0,0.15)',
            '&:hover': { bgcolor: 'grey.100' }
          }}
          size="small"
        >
          {uploading ? <CircularProgress size={18} /> : <PhotoCamera fontSize="small" />}
        </IconButton>
        <input
          type="file"
          accept="image/*"
          hidden
          ref={fileInputRef}
          onChange={handlePhotoUpload}
        />
      </Box>

      <Typography variant="h5" fontWeight="bold" align="center">
        {profile?.firstName} {profile?.lastName}
      </Typography>

      <Chip
        label={role}
        sx={{
          bgcolor: roleColor + '20',
          color: roleColor,
          fontWeight: 'bold'
        }}
      />

      <Box sx={{ width: '100%', mt: 2 }}>
        <Typography variant="caption" color="text.secondary">
          Email
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          {profile?.email}
        </Typography>

        {editing ? (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="First Name"
              size="small"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              fullWidth
            />
            <TextField
              label="Last Name"
              size="small"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              fullWidth
            />
            <TextField
              label="Phone Number"
              size="small"
              value={formData.phoneNumber}
              onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
              fullWidth
            />
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                startIcon={<Save />}
                onClick={handleSave}
                disabled={loading}
                sx={{
                  bgcolor: '#4caf50',
                  '&:hover': { bgcolor: '#43a047' },
                  flex: 1,
                  borderRadius: 2
                }}
              >
                Save
              </Button>
              <Button
                variant="outlined"
                startIcon={<Cancel />}
                onClick={handleCancel}
                disabled={loading}
                sx={{ flex: 1, borderRadius: 2 }}
              >
                Cancel
              </Button>
            </Box>
          </Box>
        ) : (
          <Box>
            <Typography variant="caption" color="text.secondary">
              First Name
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              {profile?.firstName || '—'}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Last Name
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              {profile?.lastName || '—'}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              Phone Number
            </Typography>
            <Typography variant="body2" sx={{ mb: 2 }}>
              {profile?.phoneNumber || '—'}
            </Typography>
            <Button
              variant="outlined"
              startIcon={<Edit />}
              onClick={handleEdit}
              fullWidth
              sx={{ borderRadius: 2 }}
            >
              Edit Profile
            </Button>
          </Box>
        )}
      </Box>
    </Paper>
  );
};

export default ProfileCard;
