import React, { useState, useEffect, useRef } from 'react';
import {
  Box, Typography, TextField, IconButton,
  CircularProgress, Avatar, Chip, Divider
} from '@mui/material';
import { Send, Home, Chat as ChatIcon } from '@mui/icons-material';
import MessageBubble from './MessageBubble';
import { getChatHistory } from '../../api/chatApi';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const formatDateLabel = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const yesterday = new Date();
  yesterday.setDate(now.getDate() - 1);

  const sameDay = (a, b) =>
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate();

  if (sameDay(date, now)) return 'Today';
  if (sameDay(date, yesterday)) return 'Yesterday';
  return date.toLocaleDateString([], {
    year: 'numeric', month: 'short', day: 'numeric'
  });
};

const groupMessagesByDate = (messages) => {
  const groups = [];
  let currentDate = null;
  messages.forEach((msg) => {
    const dateLabel = formatDateLabel(msg.sentAt);
    if (dateLabel !== currentDate) {
      groups.push({ type: 'date', label: dateLabel, key: `d-${dateLabel}-${msg.messageId}` });
      currentDate = dateLabel;
    }
    groups.push({ type: 'message', message: msg, key: `m-${msg.messageId}` });
  });
  return groups;
};

const getOtherName = (room) => {
  if (room.otherParticipant) {
    return `${room.otherParticipant.firstName} ${room.otherParticipant.lastName}`;
  }
  return room.landlordName || 'Chat';
};

const getInitial = (name) => {
  if (!name) return '?';
  return name.trim().charAt(0).toUpperCase();
};

const ChatWindow = ({ room, onSendMessage, connected, typingUser, onTyping }) => {
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const { user } = useAuth();

  useEffect(() => {
    if (!room) {
      setMessages([]);
      return;
    }
    setLoading(true);
    getChatHistory(room.chatRoomId)
      .then((res) => setMessages(res.data || []))
      .catch(() => toast.error('Failed to load messages'))
      .finally(() => setLoading(false));
  }, [room?.chatRoomId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, typingUser]);

  useEffect(() => {
    if (room?.newMessage) {
      setMessages((prev) => {
        const exists = prev.some(
          (m) => m.messageId === room.newMessage.messageId
        );
        if (exists) return prev;
        return [...prev, room.newMessage];
      });
    }
  }, [room?.newMessage]);

  const handleSend = () => {
    if (!newMessage.trim()) return;
    if (!connected) {
      toast.error('Not connected. Please wait...');
      return;
    }
    const sent = onSendMessage(room.chatRoomId, newMessage.trim());
    if (sent) setNewMessage('');
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleChange = (e) => {
    setNewMessage(e.target.value);
    if (onTyping && room && connected) {
      onTyping(room.chatRoomId);
    }
  };

  if (!room) {
    return (
      <Box sx={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: '#f5f7fb',
        color: 'text.secondary'
      }}>
        <ChatIcon sx={{ fontSize: 64, mb: 2, opacity: 0.4 }} />
        <Typography variant="h6" sx={{ fontWeight: 500 }}>
          Select a conversation to start chatting
        </Typography>
      </Box>
    );
  }

  const otherName = getOtherName(room);
  const isLandlord = room.chatType === 'LANDLORD';
  const city = room.otherParticipant?.city;
  const groupedItems = groupMessagesByDate(messages);

  return (
    <Box sx={{
      flex: 1,
      display: 'flex',
      flexDirection: 'column',
      height: '100%',
      bgcolor: '#f5f7fb',
      minWidth: 0
    }}>
      <Box sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1.5,
        p: 2,
        bgcolor: 'white',
        borderBottom: '1px solid #e0e0e0'
      }}>
        <Avatar
          src={room.otherParticipant?.avatarUrl}
          sx={{ bgcolor: isLandlord ? '#4caf50' : '#667eea' }}
        >
          {getInitial(otherName)}
        </Avatar>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              {otherName}
            </Typography>
            {isLandlord && (
              <Chip
                icon={<Home sx={{ fontSize: 14 }} />}
                label={
                  room.listingTitle
                    ? `Landlord Chat • ${room.listingTitle}`
                    : 'Landlord Chat'
                }
                size="small"
                sx={{
                  height: 22,
                  fontSize: '0.7rem',
                  bgcolor: '#e8f5e9',
                  color: '#2e7d32',
                  '& .MuiChip-icon': { color: '#2e7d32' }
                }}
              />
            )}
          </Box>
          {city && (
            <Typography variant="caption" color="text.secondary">
              {city}
            </Typography>
          )}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
          <Box sx={{
            width: 10,
            height: 10,
            borderRadius: '50%',
            bgcolor: connected ? '#4caf50' : '#bdbdbd'
          }} />
          <Typography variant="caption" color="text.secondary">
            {connected ? 'Online' : 'Connecting...'}
          </Typography>
        </Box>
      </Box>

      <Box sx={{
        flex: 1,
        overflowY: 'auto',
        p: 2,
        display: 'flex',
        flexDirection: 'column'
      }}>
        {loading ? (
          <Box sx={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center'
          }}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            {groupedItems.map((item) => {
              if (item.type === 'date') {
                return (
                  <Box key={item.key} sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1.5,
                    my: 2
                  }}>
                    <Divider sx={{ flex: 1 }} />
                    <Typography variant="caption" color="text.secondary"
                      sx={{
                        bgcolor: 'white',
                        px: 1.5, py: 0.25,
                        borderRadius: 10,
                        boxShadow: '0 1px 3px rgba(0,0,0,0.06)'
                      }}>
                      {item.label}
                    </Typography>
                    <Divider sx={{ flex: 1 }} />
                  </Box>
                );
              }
              const msg = item.message;
              const isOwn = msg.sender?.userId === user?.userId;
              return (
                <MessageBubble
                  key={item.key}
                  message={msg}
                  isOwn={isOwn}
                />
              );
            })}
            {typingUser && (
              <Typography
                variant="caption"
                sx={{
                  fontStyle: 'italic',
                  color: 'text.secondary',
                  ml: 5,
                  mb: 1
                }}
              >
                {typingUser} is typing...
              </Typography>
            )}
            <div ref={messagesEndRef} />
          </>
        )}
      </Box>

      <Box sx={{
        p: 1.5,
        bgcolor: 'white',
        borderTop: '1px solid #e0e0e0',
        display: 'flex',
        alignItems: 'flex-end',
        gap: 1
      }}>
        <TextField
          fullWidth
          multiline
          maxRows={4}
          size="small"
          placeholder="Type a message..."
          value={newMessage}
          onChange={handleChange}
          onKeyPress={handleKeyPress}
          sx={{
            '& .MuiOutlinedInput-root': {
              borderRadius: 6
            }
          }}
        />
        <IconButton
          color="primary"
          onClick={handleSend}
          disabled={!newMessage.trim() || !connected}
          sx={{
            bgcolor: '#667eea',
            color: 'white',
            '&:hover': { bgcolor: '#5568d3' },
            '&.Mui-disabled': {
              bgcolor: '#e0e0e0',
              color: '#9e9e9e'
            }
          }}
        >
          <Send />
        </IconButton>
      </Box>
    </Box>
  );
};

export default ChatWindow;
