import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography
} from '@mui/material';

const ConfirmDialog = ({
  open,
  title,
  message,
  onConfirm,
  onCancel,
  confirmText = 'Confirm',
  confirmColor = 'primary',
  loading = false
}) => (
  <Dialog open={open} onClose={onCancel} maxWidth="xs" fullWidth>
    <DialogTitle fontWeight="bold">{title}</DialogTitle>
    <DialogContent>
      <Typography color="text.secondary">{message}</Typography>
    </DialogContent>
    <DialogActions sx={{ p: 2, gap: 1 }}>
      <Button
        onClick={onCancel}
        disabled={loading}
        variant="outlined"
        sx={{ borderRadius: 2 }}
      >
        Cancel
      </Button>
      <Button
        onClick={onConfirm}
        disabled={loading}
        variant="contained"
        color={confirmColor}
        sx={{ borderRadius: 2 }}
      >
        {loading ? 'Please wait...' : confirmText}
      </Button>
    </DialogActions>
  </Dialog>
);

export default ConfirmDialog;
