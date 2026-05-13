import React from 'react';
import {
  Box, List, ListItem, ListItemAvatar,
  ListItemText, Avatar, Typography, Badge,
  Chip, Divider, TextField, InputAdornment
} from '@mui/material';
import { Search, Home } from '@mui/icons-material';

const formatLastMessageTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const isToday =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate();
  if (isToday) {
    return date.toLocaleTimeString([], {
      hour: '2-digit', minute: '2-digit'
    });
  }
  return date.toLocaleDateString([], {
    month: 'short', day: 'numeric'
  });
};

const truncate = (text, max = 40) => {
  if (!text) return '';
  return text.length > max ? text.substring(0, max) + '…' : text;
};

const getDisplayName = (room) => {
  if (room.otherParticipant) {
    return `${room.otherParticipant.firstName} ${room.otherParticipant.lastName}`;
  }
  return room.landlordName || 'Unknown';
};

const getInitial = (name) => {
  if (!name) return '?';
  return name.trim().charAt(0).toUpperCase();
};

const ChatRoomList = ({
  rooms,
  activeRoomId,
  onSelectRoom,
  searchTerm,
  onSearchChange
}) => {
  const filteredRooms = rooms.filter((room) => {
    const name = getDisplayName(room);
    return name.toLowerCase()
      .includes((searchTerm || '').toLowerCase());
  });

  return (
    <Box sx={{
      width: 320,
      minWidth: 320,
      height: '100%',
      borderRight: '1px solid #e0e0e0',
      overflowY: 'auto',
      display: 'flex',
      flexDirection: 'column',
      bgcolor: 'background.paper'
    }}>
      <Box sx={{ p: 2, borderBottom: '1px solid #e0e0e0' }}>
        <Typography variant="h6" sx={{ fontWeight: 600, mb: 1.5 }}>
          Messages
        </Typography>
        <TextField
          fullWidth
          size="small"
          placeholder="Search conversations..."
          value={searchTerm || ''}
          onChange={(e) => onSearchChange(e.target.value)}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <Search fontSize="small" />
              </InputAdornment>
            )
          }}
        />
      </Box>

      {filteredRooms.length === 0 ? (
        <Box sx={{
          flex: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          p: 3
        }}>
          <Typography variant="body2" color="text.secondary"
            sx={{ textAlign: 'center' }}>
            No conversations yet
          </Typography>
        </Box>
      ) : (
        <List disablePadding sx={{ flex: 1 }}>
          {filteredRooms.map((room, idx) => {
            const name = getDisplayName(room);
            const isActive = room.chatRoomId === activeRoomId;
            const isLandlord = room.chatType === 'LANDLORD';

            return (
              <React.Fragment key={room.chatRoomId}>
                <ListItem
                  button
                  onClick={() => onSelectRoom(room)}
                  sx={{
                    py: 1.5,
                    px: 2,
                    bgcolor: isActive ? 'primary.light' : 'transparent',
                    '&:hover': {
                      bgcolor: isActive ? 'primary.light' : 'action.hover'
                    },
                    cursor: 'pointer'
                  }}
                >
                  <ListItemAvatar>
                    <Badge
                      color="error"
                      badgeContent={room.unreadCount || 0}
                      invisible={!room.unreadCount || room.unreadCount === 0}
                      overlap="circular"
                    >
                      <Avatar
                        src={room.otherParticipant?.avatarUrl}
                        sx={{
                          bgcolor: isLandlord ? '#4caf50' : '#667eea'
                        }}
                      >
                        {getInitial(name)}
                      </Avatar>
                    </Badge>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        gap: 1
                      }}>
                        <Typography
                          variant="subtitle2"
                          sx={{
                            fontWeight: room.unreadCount > 0 ? 700 : 500,
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap'
                          }}
                        >
                          {name}
                        </Typography>
                        <Typography variant="caption"
                          color="text.secondary"
                          sx={{ flexShrink: 0 }}>
                          {formatLastMessageTime(room.lastMessageTime)}
                        </Typography>
                      </Box>
                    }
                    secondary={
                      <Box sx={{ mt: 0.5 }}>
                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                            fontWeight: room.unreadCount > 0 ? 600 : 400
                          }}
                        >
                          {room.lastMessage
                            ? truncate(room.lastMessage, 40)
                            : 'No messages yet'}
                        </Typography>
                        {isLandlord && (
                          <Chip
                            icon={<Home sx={{ fontSize: 14 }} />}
                            label="Landlord Chat"
                            size="small"
                            sx={{
                              mt: 0.5,
                              height: 20,
                              fontSize: '0.7rem',
                              bgcolor: '#e8f5e9',
                              color: '#2e7d32',
                              '& .MuiChip-icon': { color: '#2e7d32' }
                            }}
                          />
                        )}
                      </Box>
                    }
                    secondaryTypographyProps={{ component: 'div' }}
                  />
                </ListItem>
                {idx < filteredRooms.length - 1 && (
                  <Divider component="li" />
                )}
              </React.Fragment>
            );
          })}
        </List>
      )}
    </Box>
  );
};

export default ChatRoomList;
