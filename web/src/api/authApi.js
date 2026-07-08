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
 * Registers a new user in the system.
 * (Note: User remains disabled until email is verified).
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

/**
 * Verifies the user's email using the token from the URL.
 * @param {string} token - The unique verification token.
 * @returns {Promise<any>} A promise that resolves with a success message.
 */
export const verifyEmail = (token) =>
    axiosClient.get(`/auth/verify?token=${token}`)
        .then(res => res.data);