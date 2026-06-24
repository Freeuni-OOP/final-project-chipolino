/**
 * @fileoverview Route API module.
 * Handles all route-related network requests
 */
import axios from './axiosClient';

/**
 * Calculates the optimal route based on a provided list of waypoints.
 * @param {Object} routeData - The data object containing the list of waypoints.
 * @returns {Promise<Object>} The route response (distance, time, and path coordinates).
 */
export const calculateOptimalRoute = (routeData) =>
    axios.post('/routes/calculate', routeData).then(res => res.data);
