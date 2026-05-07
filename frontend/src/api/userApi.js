import api from './axiosConfig';

export const getMyProfile = () =>
  api.get('/users/me');

export const updateProfile = (data) =>
  api.put('/users/me', data);

export const updatePreferences = (data) =>
  api.put('/users/me/preferences', data);

export const uploadProfilePhoto = (formData) =>
  api.post('/users/me/photo', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
