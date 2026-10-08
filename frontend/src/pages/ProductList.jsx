import React, { useState, useEffect } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { getProductsApi } from '../api/productApi';
import HeaderNav from '../components/HeaderNav';

function ProductList() {
    const { category, subCategory } = useParams();
    const [searchParams, setSearchParams] = useSearchParams();
    const sort = searchParams.get('sort') || 'latest';

    const [productList, setProductList] = useState([]);
    const [totalCount, setTotalCount] = useState(0);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchProducts = async () => {
            setLoading(true);
            try {
                const data = await getProductsApi(category, subCategory, sort);
                setProductList(data.productList || []);
                setTotalCount(data.totalCount || 0);
            } catch (error) {
                console.error('상품 목록 불러오기 실패:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchProducts();
    }, [category, subCategory, sort]);

    const handleSortChange = (newSort) => {
        setSearchParams({ sort: newSort });
    };

    return (
        <div>
            {/* 상단 네비게이션바 (공통 컴포넌트) */}
            <HeaderNav />

            {/* 메인 컨테이너 */}
            <div className="container my-5" style={{ maxWidth: '1100px' }}>

                {/* 상단 컨트롤 영역 (왼쪽: 상품 수 / 오른쪽: 정렬 트리) */}
                <div className="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
                    {/* 왼쪽: 전체 상품 수 */}
                    <div className="fs-5 fw-semibold text-secondary">
                        전체 상품 <span className="text-primary fw-bold">{totalCount}</span>개
                    </div>

                    {/* 오른쪽: 정렬 트리 (최신순 / 인기순) */}
                    <div className="dropdown">
                        <button
                            className="btn btn-outline-secondary btn-sm dropdown-toggle"
                            type="button"
                            data-bs-toggle="dropdown"
                        >
                            정렬 기준
                        </button>
                        <ul className="dropdown-menu">
                            <li>
                                <button className="dropdown-item" onClick={() => handleSortChange('latest')}>
                                    최신순
                                </button>
                            </li>
                            <li>
                                <button className="dropdown-item" onClick={() => handleSortChange('popular')}>
                                    인기순
                                </button>
                            </li>
                        </ul>
                    </div>
                </div>

                {/* 상품 카드 그리드 영역 */}
                {loading ? (
                    <div className="text-center py-5">
                        <div className="spinner-border text-primary" role="status">
                            <span className="visually-hidden">로딩 중...</span>
                        </div>
                    </div>
                ) : (
                    <div className="row row-cols-1 row-cols-md-3 g-4">
                        {/* 데이터가 없을 때 */}
                        {productList.length === 0 ? (
                            <div className="col-12 text-center py-5">
                                <p className="text-muted fs-5">등록된 상품이 없습니다.</p>
                            </div>
                        ) : (
                            /* 데이터가 있을 때 반복문 */
                            productList.map((product) => (
                                <div className="col" key={product.id}>
                                    <div className="card h-100 shadow-sm border-0">
                                        <Link to={`/product/detail/${product.id}`}>
                                            <img
                                                src={product.imgUrl || 'https://via.placeholder.com/300x240'}
                                                className="card-img-top object-fit-cover"
                                                style={{ height: '240px' }}
                                                alt="키보드 이미지"
                                            />
                                        </Link>
                                        <div className="card-body">
                                            <h5 className="card-title fw-bold text-dark">
                                                <Link
                                                    to={`/product/detail/${product.id}`}
                                                    className="text-decoration-none text-dark"
                                                >
                                                    {product.name}
                                                </Link>
                                            </h5>
                                            <p className="card-text text-primary fw-bold mt-2">
                                                {product.price?.toLocaleString()}원
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            ))
                        )}
                    </div>
                )}

            </div>
        </div>
    );
}

export default ProductList;