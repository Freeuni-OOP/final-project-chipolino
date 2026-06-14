/**
 * @fileoverview Report API Service Layer
 * Interacts with the Spring Boot ReportController to handle creating road reports,
 * querying nearby reports geographically, and tracking upvotes/downvotes.
 */

import axiosApi from "./axiosClient.js";

/**
 * Submits a new road report to the server.
 * @async
 * @param {Object} reportData - Information about The report.
 * @returns {Promise<void>} Data of the response.
 */
export const createReport = async (reportData) => {
    const response = await axiosApi.post('/reports', reportData);
    return response.data;
};

/**
 * Finds all reports within a specific geographic boundary circle.
 * @async
 * @param {number} latitude - The central latitude point of the search.
 * @param {number} longitude - The central longitude point of the search.
 * @param {number} radius - The radius of the distance.
 * @returns {Promise<Array<Object>>} A promise resolving to an array of ReportResponseDTO objects.
 */
export const findNearbyReports = async (latitude, longitude, radius) => {
    const response = await axiosApi.get('/reports', {
        params: { latitude, longitude, radius }
    });
    return response.data;
};