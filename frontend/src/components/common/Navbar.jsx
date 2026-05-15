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
  Divider,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText
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
  Logout,
  Menu as MenuIcon
} from '@mui/icons-material';

const Navbar = ({ unreadCount = 0 }) => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [anchorEl, setAnchorEl] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);

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
    setDrawerOpen(false);
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

  const notificationTooltip =
    unreadCount > 0
      ? `${unreadCount} unread notification${unreadCount === 1 ? '' : 's'}`
      : 'No new notifications';

  return (
    <>
      <AppBar
        position="sticky"
        sx={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          zIndex: 1100
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            edge="start"
            onClick={() => setDrawerOpen(true)}
            sx={{ mr: 1, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>

          <Typography
            variant="h6"
            component={Link}
            to="/dashboard"
            sx={{
              flexGrow: 0,
              textDecoration: 'none',
              color: 'white',
              fontWeight: 'bold',
              mr: 4,
              cursor: 'pointer'
            }}
          >
            🏠 RoommateMatch
          </Typography>

          <Box
            sx={{
              flexGrow: 1,
              display: { xs: 'none', sm: 'flex' },
              gap: 1
            }}
          >
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
                  borderBottom: isActive(item.path)
                    ? '2px solid white'
                    : '2px solid transparent',
                  borderRadius: 1,
                  transition: 'all 0.25s ease',
                  '&:hover': {
                    backgroundColor: 'rgba(255,255,255,0.18)',
                    transform: 'translateY(-1px)'
                  },
                  textTransform: 'none',
                  px: 2
                }}
              >
                {item.label}
              </Button>
            ))}
          </Box>

          <Box sx={{ flexGrow: { xs: 1, sm: 0 } }} />

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Tooltip title={notificationTooltip}>
              <IconButton
                color="inherit"
                onClick={() => navigate('/notifications')}
                sx={{
                  transition: 'transform 0.2s ease',
                  '&:hover': { transform: 'scale(1.1)' }
                }}
              >
                <Badge badgeContent={unreadCount} color="error">
                  <Notifications />
                </Badge>
              </IconButton>
            </Tooltip>

            <Tooltip title="Account">
              <IconButton onClick={handleMenuOpen} color="inherit">
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

      <Drawer
        anchor="left"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        sx={{ display: { sm: 'none' } }}
      >
        <Box sx={{ width: 260 }} role="presentation">
          <Box
            sx={{
              p: 2,
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              color: 'white'
            }}
          >
            <Typography variant="h6" fontWeight="bold">
              🏠 RoommateMatch
            </Typography>
            {user && (
              <Typography variant="body2" sx={{ opacity: 0.85, mt: 0.5 }}>
                {user.firstName} {user.lastName}
              </Typography>
            )}
          </Box>
          <List>
            {filteredNavItems.map((item) => (
              <ListItem key={item.path} disablePadding>
                <ListItemButton
                  selected={isActive(item.path)}
                  onClick={() => handleNavigate(item.path)}
                >
                  <ListItemIcon>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.label} />
                </ListItemButton>
              </ListItem>
            ))}
            <Divider sx={{ my: 1 }} />
            <ListItem disablePadding>
              <ListItemButton onClick={() => handleNavigate('/profile')}>
                <ListItemIcon>
                  <AccountCircle />
                </ListItemIcon>
                <ListItemText primary="Profile" />
              </ListItemButton>
            </ListItem>
            <ListItem disablePadding>
              <ListItemButton onClick={handleLogout}>
                <ListItemIcon>
                  <Logout />
                </ListItemIcon>
                <ListItemText primary="Logout" />
              </ListItemButton>
            </ListItem>
          </List>
        </Box>
      </Drawer>
    </>
  );
};

export default Navbar;
