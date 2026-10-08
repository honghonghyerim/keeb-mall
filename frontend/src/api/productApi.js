import axiosInstance from './axios';

// 1. 상품 목록 조회 API (전체 / 카테고리 / 소분류)
export const getProductsApi = async (category, subCategory, sort) => {
    let url = '/api/products';

    if (category && subCategory) {
        url += `/${category}/${subCategory}`;
    } else if (category) {
        url += `/${category}`;
    }

    const response = await axiosInstance.get(url, {
        params: { sort }
    });

    return response.data;
};

export const getProductDetailApi = async (id) => {
    const response = await axiosInstance.get(`/api/products/detail/${id}`);
    return response.data;
};