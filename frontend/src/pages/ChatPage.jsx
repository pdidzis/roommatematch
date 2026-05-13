import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Box, Paper, Typography, CircularProgress } from '@mui/material';
import { getMyChatRooms } from '../api/chatApi';
import ChatRoomList from '../components/chat/ChatRoomList';
import ChatWindow from '../components/chat/ChatWindow';
import useWebSocket from '../hooks/useWebSocket';
import { useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';

const ChatPage = () => {
  const [rooms, setRooms] = useState([]);
  const [activeRoom, setActiveRoom] = useState(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [typingUser, setTypingUser] = useState(null);
  const [newMessages, setNewMessages] = useState({});
  const location = useLocation();
  const typingTimeoutRef = useRef(null);
  const activeRoomIdRef = useRef(null);

  useEffect(() => {
    activeRoomIdRef.current = activeRoom?.chatRoomId || null;
  }, [activeRoom]);

  const handleMessageReceived = useCallback((roomId, message) => {
    setRooms((prev) => prev.map((room) =>
      room.chatRoomId === roomId
        ? {
            ...room,
            lastMessage: message.content,
            lastMessageTime: message.sentAt,
            unreadCount: activeRoomIdRef.current === roomId
              ? 0
              : (room.unreadCount || 0) + 1
          }
        : room
    ));

    setNewMessages((prev) => ({
      ...prev,
      [roomId]: message
    }));
  }, []);

  const handleTypingReceived = useCallback((data) => {
    const senderId = data.senderId;
    const currentUser = JSON.parse(localStorage.getItem('user') || '{}');
    if (senderId && currentUser.userId && senderId === currentUser.userId) {
      return;
    }
    setTypingUser(data.senderName || 'Someone');
    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }
    typingTimeoutRef.current = setTimeout(() => {
      setTypingUser(null);
    }, 2000);
  }, []);

  const handleNotification = useCallback((notification) => {
    toast(notification.title || 'New notification', { icon: '🔔' });
  }, []);

  const {
    connected,
    subscribeToRoom,
    unsubscribeFromRoom,
    sendMessage,
    sendTyping
  } = useWebSocket(
    handleMessageReceived,
    handleNotification,
    handleTypingReceived
  );

  useEffect(() => {
    getMyChatRooms()
      .then((res) => {
        const data = res.data || [];
        setRooms(data);
        const activeChatId = location.state?.activeChatId;
        if (activeChatId) {
          const room = data.find((r) => r.chatRoomId === activeChatId);
          if (room) setActiveRoom(room);
        }
      })
      .catch(() => toast.error('Failed to load chats'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (activeRoom && connected) {
      subscribeToRoom(activeRoom.chatRoomId);
    }
    return () => {
      if (activeRoom) {
        unsubscribeFromRoom(activeRoom.chatRoomId);
      }
    };
  }, [activeRoom?.chatRoomId, connected, subscribeToRoom, unsubscribeFromRoom]);

  const handleSendMessage = (roomId, content) => {
    return sendMessage(roomId, content);
  };

  const handleSelectRoom = (room) => {
    setActiveRoom(room);
    setTypingUser(null);
    setRooms((prev) => prev.map((r) =>
      r.chatRoomId === room.chatRoomId
        ? { ...r, unreadCount: 0 }
        : r
    ));
  };

  const activeRoomWithMessage = activeRoom
    ? {
        ...activeRoom,
        newMessage: newMessages[activeRoom.chatRoomId]
      }
    : null;

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" mt={4}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{
      height: 'calc(100vh - 80px)',
      p: { xs: 1, md: 2 }
    }}>
      <Paper
        elevation={2}
        sx={{
          display: 'flex',
          height: '100%',
          borderRadius: 3,
          overflow: 'hidden'
        }}
      >
        {rooms.length === 0 ? (
          <Box sx={{
            flex: 1,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            p: 4
          }}>
            <Typography variant="body1" color="text.secondary"
              sx={{ textAlign: 'center' }}>
              No conversations yet. Accept a match to start chatting!
            </Typography>
          </Box>
        ) : (
          <>
            <ChatRoomList
              rooms={rooms}
              activeRoomId={activeRoom?.chatRoomId}
              onSelectRoom={handleSelectRoom}
              searchTerm={search}
              onSearchChange={setSearch}
            />
            <ChatWindow
              room={activeRoomWithMessage}
              onSendMessage={handleSendMessage}
              connected={connected}
              typingUser={typingUser}
              onTyping={sendTyping}
            />
          </>
        )}
      </Paper>
    </Box>
  );
};

export default ChatPage;
