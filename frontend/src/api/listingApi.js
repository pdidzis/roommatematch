import api from './axiosConfig';

export const getPublicListings = (params) =>
  api.get('/listings/public', { params });

export const getListingById = (id) =>
  api.get(`/listings/public/${id}`);

export const createListing = (data) =>
  api.post('/listings', data);

export const updateListing = (id, data) =>
  api.put(`/listings/${id}`, data);

export const getMyListings = () =>
  api.get('/listings/my');

export const verifyListing = (id) =>
  api.put(`/listings/${id}/verify`);

export const getListingsForRoommates = () =>
  api.get('/journey/listings');

export const expressInterest = (listingId, matchId) =>
  api.post(`/journey/listings/${listingId}/interest?matchId=${matchId}`);
