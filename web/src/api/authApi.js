import axiosClient from './axiosClient.js'

/**
 * Authenticates a user and sets the HTTP-only JWT cookie.
 * @param {Object} credentials - The login payload (typically containing username and password).
 * @returns {Promise<any>} A promise that resolves upon successful authentication.
 */
export const login = (credentials) =>
    axiosClient.post('/auth/login', credentials)
        .then(res => res.data);

/**
 * Registers a new user in the system and automatically logs them in via HTTP-only cookie.
 * @param {Object} userData - The registration payload (typically containing username, email, and password).
 * @returns {Promise<any>} A promise that resolves upon successful registration.
 */
export const register = (userData) =>
    axiosClient.post('/auth/register', userData)
        .then(res => res.data);

/**
 * Logs the current user out by clearing the HTTP-only JWT cookie on the backend.
 * @returns {Promise<any>} A promise that resolves when the logout is successful.
 */
export const logout = () =>
    axiosClient.post('/auth/logout')
        .then(res => res.data);