import axiosClient from './axiosClient.js'

/**
 * Gets selected user extended info.
 * @param {number|string} id - The ID of the user to select.
 * @returns {Promise<Object>} A promise with users extended info.
 */
export const selectUser = (id) =>
    axiosClient.get(`/admin/users/${id}`)
        .then(res => res.data);

/**
 * Bans a user for a specific number of days.
 * @param {number|string} userId - The ID of the user to ban.
 * @param {number} daysToBan - The duration of the ban in days.
 * @returns {Promise<string>} A promise that resolves to the success message.
 */
export const banUser = (userId, daysToBan) =>
    axiosClient.patch(`/admin/users/${userId}/ban`, null, { params: { daysToBan } })
        .then(res => res.data);

/**
 * Lifts a ban from a specific user.
 * @param {number|string} userId - The ID of the user to unban.
 * @returns {Promise<string>} A promise that resolves to the success message.
 */
export const unbanUser = (userId) =>
    axiosClient.patch(`/admin/users/${userId}/unban`)
        .then(res => res.data);

/**
 * Permanently deletes a user account from the system.
 * @param {number|string} userId - The ID of the user to delete.
 * @returns {Promise<string>} A promise that resolves to the success message.
 */
export const deleteUser = (userId) =>
    axiosClient.delete(`/admin/users/${userId}`)
        .then(res => res.data);

/**
 * Adjusts or resets a user's reputation score.
 * @param {number|string} userId - The ID of the target user.
 * @param {boolean} isReset - true to reset reputation, false to adjust it.
 * @param {number} [score] - The optional numerical score to apply if not resetting.
 * @returns {Promise<string>} A promise that resolves to the success message.
 */
export const adjustReputation = (userId, isReset, score) =>
    axiosClient.patch(`/admin/users/${userId}/reputation`, null, { params: { isReset, score } })
        .then(res => res.data);

/**
 * Overrides the current status of a road report (e.g., forcing it to PERMANENT or REMOVED).
 * @param {number|string} reportId - The ID of the report to modify.
 * @param {string} newReportStatus - The new status enum value (e.g., 'TEMPORARY', 'PERMANENT', 'REMOVED').
 * @returns {Promise<string>} A promise that resolves to the success message.
 */
export const overrideReportStatus = (reportId, newReportStatus) =>
    axiosClient.patch(`/admin/reports/${reportId}/status`, null, { params: { newReportStatus } })
        .then(res => res.data);

/**
 * Deletes a specific road report from the system.
 * @param {number|string} reportId - The ID of the report to remove.
 * @returns {Promise<string>} A promise that resolves to the success message.
 */
export const deleteReport = (reportId) =>
    axiosClient.delete(`/admin/reports/${reportId}`)
        .then(res => res.data);

/**
 * Moderates and deletes an inappropriate comment.
 * @param {number|string} commentId - The ID of the comment to remove.
 * @returns {Promise<string>} A promise that resolves to the success message.
 */
export const deleteComment = (commentId) =>
    axiosClient.delete(`/admin/comments/${commentId}`)
        .then(res => res.data);