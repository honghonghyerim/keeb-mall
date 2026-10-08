import axiosInstance from './axios'; //axios.js  에 있는 axiosInstance

// 1. 로그인 API
export const loginApi = async (username, password) => {
    const response = await axiosInstance.post('/api/login', null, {
        params: { username, password },
    });
    return response.data;
};

// 2. 아이디 중복 체크 API
export const checkUsernameApi = async (username) => {
    const response = await axiosInstance.post('/api/check-username', null, {
        params: { username }
    });
    return response.data;
};

// 3. 회원가입 API
export const signupApi = async (signupData) => {
    const params = new URLSearchParams();
    params.append('username', signupData.username);
    params.append('password', signupData.password);
    params.append('name', signupData.name);
    params.append('postcode', signupData.postcode);
    params.append('address', signupData.address);
    params.append('detailAddress', signupData.detailAddress);

    const response = await axiosInstance.post('/api/signup', params, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });
    return response.data;
};

// 4. 세션 확인 API (현재 로그인된 유저 체크)
export const checkSessionApi = async () => {
    const response = await axiosInstance.get('/api/me');
    return response.data;
};

// 5. 로그아웃 API (★ 에러 원인: 이 부분이 누락되었었습니다!)
export const logoutApi = async () => {
    const response = await axiosInstance.post('/api/logout');
    return response.data;
};