import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/cart';

// 장바구니 담기 API
export const addToCartApi = async (cartData) => {
    const response = await axios.post(`${BASE_URL}/add`, cartData, {
        withCredentials: true
    });
    return response.data;
};

// 장바구니 목록 조회 API
export const getCartListApi = async () => {
    const response = await axios.get(BASE_URL, {
        withCredentials: true
    });
    return response.data;
};

// 장바구니 수량 변경 API
export const updateCartCountApi = async (cartInfoId, count) => {
    const response = await axios.put(`${BASE_URL}/${cartInfoId}`, { count }, {
        withCredentials: true
    });
    return response.data;
};

// 장바구니 항목 삭제 API
export const deleteCartItemApi = async (cartInfoId) => {
    const response = await axios.delete(`${BASE_URL}/${cartInfoId}`, {
        withCredentials: true
    });
    return response.data;
};