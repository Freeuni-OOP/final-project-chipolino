/**
 * @fileoverview Axios Client Configuration API
 * Handles network requests, automatic cookie injection
 * and global HTTPS response interception for security.
 */

import axios from "axios";

/**
 * Axios instance created for talking to the Spring Boot backend.
 * @constant
 * @type {import('axios').AxiosInstance}
 * @property baseURL - The root URL for all backend API endpoints.
 * @property withCredentials - Forces the browser to automatically include HttpOnly JWT session cookies.
 */
const axiosApi = axios.create({
    baseURL: "/api",   //Need to change http with https later!!
    headers: {"Content-Type" : "application/json"},
    timeout: 5000,
    withCredentials: true,
});

/**
 * Global Response Interceptor (The Security Checkpoint)
 * Checks all incoming data from the server. If a request returns a 401 Unauthorized status,
 * it implies that the JWT cookie has expired or is invalid. The interceptor will automatically
 * remove the local session flags and redirect the user back to the login screen.
 */
axiosApi.interceptors.response.use(
    (success) => success,
    (failure) => {
        if (failure.response?.status === 401) {
            const path = window.location.pathname;
            if (path !== '/login' && path !== '/register') {
                window.location.href = '/login';
            }
        }

        return Promise.reject(failure);
    }
);

export default axiosApi;