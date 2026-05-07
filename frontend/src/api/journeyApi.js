import api from './axiosConfig';

export const confirmRoommate = (matchId) =>
  api.put(`/journey/matches/${matchId}/confirm-roommate`);

export const getMyInterests = (matchId) =>
  api.get(`/journey/listings/my-interests?matchId=${matchId}`);

export const getLandlordChats = () =>
  api.get('/journey/landlord/chats');
