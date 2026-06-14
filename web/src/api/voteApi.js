/**
 * @fileoverview Voting API Communicates with
 * the Spring Boot backend to handle upvoting, downvoting,
 * and counting total votes for some report.
 */

import axiosApi from './axiosClient.js';

/**
 * Sends a POST request to increment the upvote count of a specific report.
 * @async
 * @param {number|string} reportId - The unique identifier of the report to upvote.
 * @returns {Promise<any>} The server response data.
 */
export const upVote = async (reportId) => {
    const response = await axiosApi.post(`/vote/${reportId}/upvote`);
    return response.data;
};

/**
 * Sends a POST request to increment the downvote count of a specific report.
 * @async
 * @param {number|string} reportId - The unique identifier of the report to downvote.
 * @returns {Promise<any>} The server response data.
 */
export const downVote = async (reportId) => {
    const response = await axiosApi.post(`/vote/${reportId}/downvote`);
    return response.data;
};

/**
 * Sends a GET request to find the current total vote count for a specific report.
 * @async
 * @param {number|string} reportId - The unique identifier of the target report.
 * @returns {Promise<any>} The server response data.
 */
export const voteCount = async (reportId) => {
    const response = await axiosApi.get(`/vote/${reportId}/votes`);
    return response.data;
};
