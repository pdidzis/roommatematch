import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { WS_URL } from '../utils/constants';

const useWebSocket = (onMessageReceived, onNotification, onTypingReceived) => {
  const [connected, setConnected] = useState(false);
  const clientRef = useRef(null);
  const subscriptionsRef = useRef({});
  const typingSubscriptionsRef = useRef({});

  const onMessageReceivedRef = useRef(onMessageReceived);
  const onNotificationRef = useRef(onNotification);
  const onTypingReceivedRef = useRef(onTypingReceived);

  useEffect(() => {
    onMessageReceivedRef.current = onMessageReceived;
  }, [onMessageReceived]);

  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);

  useEffect(() => {
    onTypingReceivedRef.current = onTypingReceived;
  }, [onTypingReceived]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      onConnect: () => {
        setConnected(true);
        console.log('WebSocket connected');

        const user = JSON.parse(
          localStorage.getItem('user') || '{}');
        if (user.email) {
          client.subscribe(
            `/user/${user.email}/queue/notifications`,
            (message) => {
              const notification = JSON.parse(message.body);
              if (onNotificationRef.current) {
                onNotificationRef.current(notification);
              }
            }
          );
        }
      },
      onDisconnect: () => {
        setConnected(false);
        console.log('WebSocket disconnected');
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        setConnected(false);
      },
      reconnectDelay: 5000
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (client.active) client.deactivate();
    };
  }, []);

  const subscribeToRoom = useCallback((roomId) => {
    if (!clientRef.current?.active) return;

    if (subscriptionsRef.current[roomId]) {
      subscriptionsRef.current[roomId].unsubscribe();
    }
    if (typingSubscriptionsRef.current[roomId]) {
      typingSubscriptionsRef.current[roomId].unsubscribe();
    }

    const sub = clientRef.current.subscribe(
      `/topic/chat/${roomId}`,
      (message) => {
        const msg = JSON.parse(message.body);
        if (onMessageReceivedRef.current) {
          onMessageReceivedRef.current(roomId, msg);
        }
      }
    );
    subscriptionsRef.current[roomId] = sub;

    const typingSub = clientRef.current.subscribe(
      `/topic/chat/${roomId}/typing`,
      (message) => {
        const data = JSON.parse(message.body);
        if (onTypingReceivedRef.current) {
          onTypingReceivedRef.current(data);
        }
      }
    );
    typingSubscriptionsRef.current[roomId] = typingSub;
  }, []);

  const unsubscribeFromRoom = useCallback((roomId) => {
    if (subscriptionsRef.current[roomId]) {
      subscriptionsRef.current[roomId].unsubscribe();
      delete subscriptionsRef.current[roomId];
    }
    if (typingSubscriptionsRef.current[roomId]) {
      typingSubscriptionsRef.current[roomId].unsubscribe();
      delete typingSubscriptionsRef.current[roomId];
    }
  }, []);

  const sendMessage = useCallback((roomId, content) => {
    if (!clientRef.current?.active) return false;
    clientRef.current.publish({
      destination: `/app/chat.send/${roomId}`,
      body: JSON.stringify({ content })
    });
    return true;
  }, []);

  const sendTyping = useCallback((roomId) => {
    if (!clientRef.current?.active) return;
    clientRef.current.publish({
      destination: `/app/chat.typing/${roomId}`,
      body: JSON.stringify({})
    });
  }, []);

  return {
    connected,
    subscribeToRoom,
    unsubscribeFromRoom,
    sendMessage,
    sendTyping
  };
};

export default useWebSocket;
