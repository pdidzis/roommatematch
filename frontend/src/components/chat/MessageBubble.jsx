import React from 'react';
import { Box, Typography, Avatar } from '@mui/material';
import { Check, DoneAll } from '@mui/icons-material';

const formatTime = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleTimeString([], {
    hour: '2-digit', minute: '2-digit'
  });
};

const getInitial = (sender) => {
  if (!sender) return '?';
  const name = sender.firstName || sender.email || '?';
  return name.charAt(0).toUpperCase();
};

const renderStatusIcon = (status) => {
  const iconSx = { fontSize: 14, ml: 0.5 };
  if (status === 'READ') {
    return <DoneAll sx={{ ...iconSx, color: '#4fc3f7' }} />;
  }
  if (status === 'DELIVERED') {
    return <DoneAll sx={{ ...iconSx, color: 'rgba(255,255,255,0.7)' }} />;
  }
  return <Check sx={{ ...iconSx, color: 'rgba(255,255,255,0.7)' }} />;
};

const MessageBubble = ({ message, isOwn }) => {
  const { content, sentAt, sender, status } = message;

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: isOwn ? 'flex-end' : 'flex-start',
        alignItems: 'flex-end',
        mb: 1.5,
        gap: 1
      }}
    >
      {!isOwn && (
        <Avatar
          src={sender?.avatarUrl}
          sx={{
            width: 32, height: 32,
            fontSize: '0.85rem',
            bgcolor: '#667eea'
          }}
        >
          {getInitial(sender)}
        </Avatar>
      )}

      <Box
        sx={{
          maxWidth: '70%',
          bgcolor: isOwn ? '#667eea' : 'white',
          color: isOwn ? 'white' : 'text.primary',
          borderRadius: isOwn
            ? '20px 20px 4px 20px'
            : '20px 20px 20px 4px',
          padding: '10px 14px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          wordBreak: 'break-word'
        }}
      >
        <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
          {content}
        </Typography>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'flex-end',
            alignItems: 'center',
            mt: 0.5
          }}
        >
          <Typography
            variant="caption"
            sx={{
              fontSize: '0.7rem',
              color: isOwn ? 'rgba(255,255,255,0.85)' : 'text.secondary'
            }}
          >
            {formatTime(sentAt)}
          </Typography>
          {isOwn && renderStatusIcon(status)}
        </Box>
      </Box>
    </Box>
  );
};

export default MessageBubble;
