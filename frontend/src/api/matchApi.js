import api from './axiosConfig';

export const getSuggestions = () =>
  api.get('/matches/suggestions');

export const requestMatch = (targetUserId) =>
  api.post(`/matches/request/${targetUserId}`);

export const respondToMatch = (matchId, accept) =>
  api.put(`/matches/${matchId}/respond?accept=${accept}`);

export const getMyMatches = () =>
  api.get('/matches/my');
