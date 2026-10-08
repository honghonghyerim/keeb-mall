import axios from 'axios';

// 백엔드 API 서버의 기본 주소 설정
const axiosInstance = axios.create({
    baseURL: '',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
    },
});


export default axiosInstance;