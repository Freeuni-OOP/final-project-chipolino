/**
 * @fileoverview Voting API Communicates with
 * the Spring Boot backend to handle upvoting, downvoting,
 * and counting total votes for some report.
 */

import axiosApi from './axiosClient.js'

/**
 * Service object containing methods for managing votes.
 */
const voteApi = {
    /**
     * Sends a POST request to increment the upvote count of a specific report.
     * @async
     * @function upVote
     * @param reportId - The unique identifier of the report to upvote.
     * @returns The server response data.
     */
    upVote: async (reportId) => {
        const response = await axiosApi.post(`/vote/${reportId}/upvote`);
        return response.data;
    },

    /**
     * Sends a POST request to increment the downvote count of a specific report.
     * @async
     * @function downVote
     * @param reportId - The unique identifier of the report to downvote.
     * @returns The server response data.
     */
    downVote: async (reportId) => {
        const response = await axiosApi.post(`/vote/${reportId}/downvote`);
        return response.data;
    },

    /**
     * Sends a GET request to find the current total vote count for a specific report.
     * @async
     * @function voteCount
     * @param reportId - The unique identifier of the target report.
     * @returns The server response data.
     */
    voteCount: async (reportId) => {
        const response = await axiosApi.get(`/vote/${reportId}/votes`);
        return response.data;
    }
}

export default voteApi;
