import axiosClient from './axiosClient';

export const getMe = () =>
    axiosClient.get('/users/me');

export const getUserById = (id) =>
    axiosClient.get(`/users/${id}`);

export const updateProfile = (data) =>
    axiosClient.put('/users/me', data);

export const deleteAccount = () =>
    axiosClient.delete('/users/me');