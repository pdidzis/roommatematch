import api from './axiosConfig';

export const getMyChatRooms = () =>
  api.get('/chat/rooms');

export const getChatHistory = (roomId) =>
  api.get(`/chat/rooms/${roomId}/messages`);

export const openChatRoom = (targetUserId) =>
  api.post(`/chat/rooms/open/${targetUserId}`);
