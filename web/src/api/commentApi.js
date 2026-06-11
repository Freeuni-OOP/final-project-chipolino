import axiosClient from './axiosClient.js'

export const addComment = (reportId, content) =>
    axiosClient.post(`/reports/${reportId}/comments`, {content : content});

export const getComments = (reportId) =>
    axiosClient.get(`/reports/${reportId}/comments`)

export const updateComment = (commentId, content) =>
    axiosClient.put(`/comments/${commentId}`, {content : content})

export const deleteComment = (commentId) =>
    axiosClient.delete(`/comments/${commentId}`)