/**
 * @fileoverview User API module.
 * Handles all user-related network requests
 */
import axios from './axiosClient';

export const userApi = {
    /**
     * Fetches the profile of the currently logged-in user.
     * @returns {Promise<Object>} The current user's data.
     */
    getCurrentUser: async () => {
        const response = await  axios.get('/users/me')
        return response.data;
    },

    /**
     * Fetches a specific user's profile by their ID.
     * @param {number} id - The unique ID of the user.
     * @returns {Promise<Object>} The user's public data.
     */
    getUser: async (id) => {
        const response = await axios.get(`/users/${id}`)
        return response.data;
    },

    /**
     * Updates the current user's profile information.
     * @param {Object} updateData - The new data to update.
     * @returns {Promise<Object>} The updated user data.
     */
    updateUser: async(updateData) => {
        const response = await axios.put('/users/me', updateData)
        return response.data;
    },

    /**
     * Permanently deletes the currently logged-in user's account.
     * @returns {Promise<void>}
     */
    deleteCurrentUser: async() => {
        const response = await axios.delete('/users/me')
        return response.data;
    }
}