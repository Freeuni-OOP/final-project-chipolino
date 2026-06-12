import axiosClient from './axiosClient.js'

/**
 * Creates a new comment under a specific road report.
 * @param reportId - The unique identifier of the target road report.
 * @param content - The text body of the comment.
 * @returns {Promise<any>} A promise that resolves directly to the created comment data.
 */
export const addComment = (reportId, content) =>
    axiosClient.post(`/reports/${reportId}/comments`, {content : content})
        .then(res => res.data);

/**
 * Gets a list of all comments associated with a specific road report.
 * @param reportId - The unique identifier of the road report.
 * @returns {Promise<any[]>} A promise that resolves directly to an array of comments.
 */
export const getComments = (reportId) =>
    axiosClient.get(`/reports/${reportId}/comments`).then(res => res.data)

/**
 * Updates the text content of an existing comment.
 * @param commentId - The unique identifier of the comment to be modified.
 * @param content - The new text content for the comment.
 * @returns {Promise<any>} A promise that resolves directly to the updated comment data.
 */
export const updateComment = (commentId, content) =>
    axiosClient.put(`/comments/${commentId}`, {content : content}).then(res => res.data)

/**
 * Permanently deletes a specific comment from the system.
 * @param commentId - The unique identifier of the comment to be removed.
 * @returns {Promise<any>} A promise that resolves to the response payload (typically empty on a 204 No Content status).
 */
export const deleteComment = (commentId) =>
    axiosClient.delete(`/comments/${commentId}`).then(res => res.data)