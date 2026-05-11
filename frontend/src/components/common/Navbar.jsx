import React, { useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  IconButton,
  Box,
  Avatar,
  Menu,
  MenuItem,
  Badge,
  Tooltip,
  Divider
} from '@mui/material';
import {
  Home,
  People,
  Chat,
  Apartment,
  Notifications,
  LocalOffer,
  AdminPanelSettings,
  AccountCircle,
  Logout
} from '@mui/icons-material';

const Navbar = ({ unreadCount = 0 }) => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState(null);

  const handleMenuOpen = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    handleMenuClose();
    logout();
    navigate('/login');
  };

  const handleNavigate = (path) => {
    handleMenuClose();
    navigate(path);
  };

  const navItems = [
    {
      label: 'Dashboard',
      path: '/dashboard',
      icon: <Home />,
      roles: ['TENANT', 'LANDLORD', 'PARTNER', 'ADMIN']
    },
    {
      label: 'Matches',
      path: '/matches',
      icon: <People />,
      roles: ['TENANT']
    },
    {
      label: 'Chat',
      path: '/chat',
      icon: <Chat />,
      roles: ['TENANT', 'LANDLORD']
    },
    {
      label: 'Listings',
      path: '/listings',
      icon: <Apartment />,
      roles: ['TENANT', 'LANDLORD', 'ADMIN']
    },
    {
      label: 'Offers',
      path: '/offers',
      icon: <LocalOffer />,
      roles: ['TENANT', 'PARTNER']
    },
    {
      label: 'Admin',
      path: '/admin',
      icon: <AdminPanelSettings />,
      roles: ['ADMIN']
    }
  ];

  const filteredNavItems = navItems.filter(item =>
    item.roles.some(role => hasRole(role))
  );

  const isActive = (path) => location.pathname === path;

  return (
    <AppBar
      position="sticky"
      sx={{
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        zIndex: 1100
      }}
    >
      <Toolbar>
        <Typography
          variant="h6"
          component={Link}
          to="/dashboard"
          sx={{
            flexGrow: 0,
            textDecoration: 'none',
            color: 'white',
            fontWeight: 'bold',
            mr: 4
          }}
        >
          RoommateMatch
        </Typography>

        <Box sx={{ flexGrow: 1, display: 'flex', gap: 1 }}>
          {filteredNavItems.map((item) => (
            <Button
              key={item.path}
              component={Link}
              to={item.path}
              startIcon={item.icon}
              sx={{
                color: 'white',
                backgroundColor: isActive(item.path)
                  ? 'rgba(255,255,255,0.2)'
                  : 'transparent',
                '&:hover': {
                  backgroundColor: 'rgba(255,255,255,0.15)'
                },
                textTransform: 'none',
                px: 2
              }}
            >
              {item.label}
            </Button>
          ))}
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Tooltip title="Notifications">
            <IconButton
              color="inherit"
              onClick={() => navigate('/notifications')}
            >
              <Badge badgeContent={unreadCount} color="error">
                <Notifications />
              </Badge>
            </IconButton>
          </Tooltip>

          <Tooltip title="Account">
            <IconButton
              onClick={handleMenuOpen}
              color="inherit"
            >
              {user?.profilePhotoUrl ? (
                <Avatar
                  src={user.profilePhotoUrl}
                  sx={{ width: 32, height: 32 }}
                />
              ) : (
                <AccountCircle />
              )}
            </IconButton>
          </Tooltip>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
            anchorOrigin={{
              vertical: 'bottom',
              horizontal: 'right'
            }}
            transformOrigin={{
              vertical: 'top',
              horizontal: 'right'
            }}
          >
            <MenuItem disabled sx={{ opacity: '1 !important' }}>
              <Box>
                <Typography variant="body1" fontWeight="medium">
                  {user?.firstName} {user?.lastName}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {user?.email}
                </Typography>
              </Box>
            </MenuItem>
            <Divider />
            <MenuItem onClick={() => handleNavigate('/profile')}>
              <AccountCircle sx={{ mr: 1 }} />
              Profile
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <Logout sx={{ mr: 1 }} />
              Logout
            </MenuItem>
          </Menu>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar;
