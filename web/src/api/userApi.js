/**
 * @fileoverview User API module.
 * Handles all user-related network requests
 */
import axios from './axiosClient';

/**
 * Fetches the profile of the currently logged-in user.
 * @returns {Promise<Object>} The current user's data.
 */
export const getCurrentUser = () =>
    axios.get('/users/me').then(res => res.data);

/**
 * Fetches a specific user's profile by their ID.
 * @param {string} id - The unique ID of the user.
 * @returns {Promise<Object>} The user's public data.
 */
export const getUser = (id) =>
    axios.get(`/users/${id}`).then(res => res.data);

/**
 * Updates the current user's profile information.
 * @param {Object} updateData - The new data to update.
 * @returns {Promise<Object>} The updated user data.
 */
export const updateUser = (updateData) =>
    axios.put('/users/me', updateData).then(res => res.data);

/**
 * Permanently deletes the currently logged-in user's account.
 * @returns {Promise<void>}
 */
export const deleteCurrentUser = () =>
    axios.delete('/users/me').then(res => res.data);