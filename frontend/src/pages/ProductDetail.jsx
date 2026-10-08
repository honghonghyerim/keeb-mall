import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getProductDetailApi } from '../api/productApi';
import HeaderNav from '../components/HeaderNav';
import './ProductDetail.css';

function ProductDetail() {
    const { id } = useParams(); // URL 경로의 상품 ID (/product/detail/:id)

    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);

    // 선택 옵션 상태 관리
    const [selectedLayout, setSelectedLayout] = useState('');
    const [selectedColor, setSelectedColor] = useState('');
    const [quantity, setQuantity] = useState(1);

    // 상품 정보 조회
    useEffect(() => {
        const fetchProductDetail = async () => {
            setLoading(true);
            try {
                const data = await getProductDetailApi(id);
                setProduct(data);
                // 백엔드에서 받아온 기본 옵션이 있다면 설정
                if (data.kbdLayout) setSelectedLayout(data.kbdLayout);
                if (data.kbdColor) setSelectedColor(data.kbdColor);
            } catch (error) {
                console.error('상품 상세 정보 조회 실패:', error);
            } finally {
                setLoading(false);
            }
        };

        fetchProductDetail();
    }, [id]);

    // 수량 감소 (-)
    const handleMinusCount = () => {
        if (quantity > 1) {
            setQuantity((prev) => prev - 1);
        }
    };

    // 수량 증가 (+)
    const handlePlusCount = () => {
        setQuantity((prev) => prev + 1);
    };

    if (loading) {
        return (
            <div className="bg-light min-vh-100">
                <HeaderNav />
                <div className="text-center py-5">
                    <div className="spinner-border text-primary" role="status">
                        <span className="visually-hidden">로딩 중...</span>
                    </div>
                </div>
            </div>
        );
    }

    if (!product) {
        return (
            <div className="bg-light min-vh-100">
                <HeaderNav />
                <div className="text-center py-5">
                    <p className="text-muted fs-5">상품 정보를 찾을 수 없습니다.</p>
                </div>
            </div>
        );
    }

    // 총 금액 자동 계산 (단가 * 수량)
    const totalPrice = (product.price || 0) * quantity;

    return (
        <div className="bg-light min-vh-100">
            {/* 공통 헤더 & 네비바 */}
            <HeaderNav />

            {/* 메인 상세 컨테이너 */}
            <div className="detail-container my-5">
                {/* 왼쪽: 상품 이미지 */}
                <div className="detail-left">
                    <img
                        src={product.imgUrl || 'https://via.placeholder.com/400'}
                        style={{ height: '380px' }}
                        alt={product.name}
                    />
                </div>

                {/* 오른쪽: 상품 정보 및 옵션 */}
                <div className="detail-right text-start">
                    {/* 1. 키보드 이름 */}
                    <div className="product-name">{product.name}</div>

                    {/* 2. 가격 */}
                    <div className="product-price">
                        {product.price?.toLocaleString()}원
                    </div>

                    {/* 3. 배송안내 */}
                    <div className="delivery-info">
                        [배송안내] 평일 오후 2시 이전 결제 시 당일 발송 (CJ대한통운)
                    </div>

                    {/* 4. 키보드 옵션 (배열 선택) */}
                    <div className="option-group">
                        <label htmlFor="keyboard-option">키보드 옵션 선택</label>
                        <select
                            id="keyboard-option"
                            value={selectedLayout}
                            onChange={(e) => setSelectedLayout(e.target.value)}
                        >
                            <option value="" disabled>옵션을 선택해주세요</option>
                            {product.kbdLayout && (
                                <option value={product.kbdLayout}>{product.kbdLayout}</option>
                            )}
                        </select>
                    </div>

                    {/* 5. 색상 선택 */}
                    <div className="option-group">
                        <label htmlFor="keyboard-color">색상 선택</label>
                        <select
                            id="keyboard-color"
                            value={selectedColor}
                            onChange={(e) => setSelectedColor(e.target.value)}
                        >
                            <option value="" disabled>색상을 선택해주세요</option>
                            {product.kbdColor && (
                                <option value={product.kbdColor}>{product.kbdColor}</option>
                            )}
                        </select>
                    </div>

                    {/* 하단 옵션 요약 & 수량/금액 계산 박스 */}
                    <div className="selected-option-box">
                        <div className="selected-info-text">
                            선택옵션 : {selectedLayout || '미선택'} / {selectedColor || '미선택'}
                        </div>
                        <div className="quantity-price-row">
                            {/* 수량 컨트롤러 */}
                            <div className="quantity-controller">
                                <button type="button" onClick={handleMinusCount}>-</button>
                                <span>{quantity}</span>
                                <button type="button" onClick={handlePlusCount}>+</button>
                            </div>
                            {/* 계산된 가격 */}
                            <div className="calculated-price">
                                {totalPrice.toLocaleString()}원
                            </div>
                        </div>
                    </div>

                    {/* 구매하기 & 장바구니 버튼 */}
                    <div className="button-group">
                        <button type="button" className="btn btn-cart">장바구니</button>
                        <button type="button" className="btn btn-buy">구매하기</button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProductDetail;