import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true,  // ← sends httpOnly cookie automatically
    headers: {
        'Content-Type': 'application/json'
    }
});

axiosClient.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401) {
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default axiosClient;