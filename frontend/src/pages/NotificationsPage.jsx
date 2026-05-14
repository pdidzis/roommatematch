import React, { useState, useEffect } from 'react';
import {
  Box, Typography, List, ListItem, ListItemText,
  ListItemIcon, IconButton, Button, Chip,
  CircularProgress, Paper, Divider
} from '@mui/material';
import {
  Notifications, People, Chat, Home,
  CheckCircle, Info, MarkEmailRead,
  FiberManualRecord
} from '@mui/icons-material';
import {
  getNotifications, markAllAsRead, markAsRead
} from '../api/notificationApi';
import toast from 'react-hot-toast';
import { useNavigate } from 'react-router-dom';

const typeIcons = {
  MATCH_REQUEST: <People sx={{ color: '#667eea' }} />,
  MATCH_ACCEPTED: <CheckCircle sx={{ color: '#4caf50' }} />,
  MATCH_DECLINED: <People sx={{ color: '#f44336' }} />,
  ROOMMATE_CONFIRMED: <Home sx={{ color: '#9c27b0' }} />,
  LISTING_VERIFIED: <Home sx={{ color: '#4caf50' }} />,
  NEW_MESSAGE: <Chat sx={{ color: '#2196f3' }} />,
  LANDLORD_CHAT_CREATED: <Chat sx={{ color: '#9c27b0' }} />,
  WELCOME: <Info sx={{ color: '#ff9800' }} />,
  PARTNER_OFFER: <Info sx={{ color: '#f44336' }} />
};

const typeRoutes = {
  MATCH_REQUEST: '/matches',
  MATCH_ACCEPTED: '/matches',
  ROOMMATE_CONFIRMED: '/matches',
  NEW_MESSAGE: '/chat',
  LANDLORD_CHAT_CREATED: '/chat',
  LISTING_VERIFIED: '/listings'
};

const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const loadNotifications = async () => {
    try {
      const res = await getNotifications();
      setNotifications(res.data);
    } catch (err) {
      toast.error('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadNotifications(); }, []);

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead();
      setNotifications(prev =>
        prev.map(n => ({ ...n, isRead: true })));
      toast.success('All marked as read');
    } catch (err) {
      toast.error('Failed to mark as read');
    }
  };

  const handleNotificationClick = async (notification) => {
    if (!notification.isRead) {
      await markAsRead(notification.id);
      setNotifications(prev =>
        prev.map(n => n.id === notification.id
          ? { ...n, isRead: true } : n));
    }
    const route = typeRoutes[notification.type];
    if (route) navigate(route);
  };

  const formatTime = (dateStr) => {
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now - date;
    const mins = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);
    if (mins < 1) return 'Just now';
    if (mins < 60) return `${mins}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return `${days}d ago`;
  };

  if (loading) return (
    <Box display="flex" justifyContent="center" mt={4}>
      <CircularProgress />
    </Box>
  );

  const unreadCount = notifications.filter(
    n => !n.isRead).length;

  return (
    <Box>
      {/* Header row */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 2,
          mb: 3,
          flexWrap: 'wrap'
        }}
      >
        <Typography variant="h4" fontWeight="bold">
          Notifications
        </Typography>
        {unreadCount > 0 && (
          <Chip
            label={`${unreadCount} unread`}
            size="small"
            sx={{
              bgcolor: '#667eea20',
              color: '#667eea',
              fontWeight: 600
            }}
          />
        )}
        <Box sx={{ flexGrow: 1 }} />
        {unreadCount > 0 && (
          <Button
            startIcon={<MarkEmailRead />}
            onClick={handleMarkAllRead}
            size="small"
          >
            Mark all as read
          </Button>
        )}
      </Box>

      {notifications.length === 0 ? (
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
          <Notifications sx={{ fontSize: 64, color: '#ccc', mb: 2 }} />
          <Typography variant="h6" color="text.secondary">
            You're all caught up!
          </Typography>
        </Box>
      ) : (
        <Paper
          sx={{
            borderRadius: 3,
            boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
            overflow: 'hidden'
          }}
        >
          <List disablePadding>
            {notifications.map((notification, index) => (
              <React.Fragment key={notification.id}>
                <ListItem
                  onClick={() => handleNotificationClick(notification)}
                  sx={{
                    cursor: 'pointer',
                    bgcolor: notification.isRead ? 'white' : '#f0f4ff',
                    '&:hover': { bgcolor: '#eef1ff' }
                  }}
                >
                  {!notification.isRead && (
                    <FiberManualRecord
                      sx={{ color: '#2196f3', fontSize: 12, mr: 1 }}
                    />
                  )}
                  <ListItemIcon>
                    {typeIcons[notification.type] ||
                      <Info sx={{ color: '#999' }} />}
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <Typography
                        variant="body1"
                        fontWeight={notification.isRead ? 400 : 700}
                      >
                        {notification.title}
                      </Typography>
                    }
                    secondary={
                      `${notification.message} • ${formatTime(notification.createdAt)}`
                    }
                  />
                </ListItem>
                {index < notifications.length - 1 && <Divider />}
              </React.Fragment>
            ))}
          </List>
        </Paper>
      )}
    </Box>
  );
};

export default NotificationsPage;
