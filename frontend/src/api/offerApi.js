import api from './axiosConfig';

export const getActiveOffers = () =>
  api.get('/offers/public');

export const getOffersForMatch = (matchId) =>
  api.get(`/offers/for-match/${matchId}`);

export const createOffer = (data) =>
  api.post('/offers', data);
