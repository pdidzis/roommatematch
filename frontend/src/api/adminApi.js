import api from './axiosConfig';

export const getAdminStats = () =>
  api.get('/admin/stats');
