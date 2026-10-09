import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getProductDetailApi } from '../api/productApi';
import { addToCartApi } from '../api/cartApi';
import { checkSessionApi } from '../api/authApi'
import HeaderNav from '../components/HeaderNav';
import '../css/ProductDetail.css';

function ProductDetail() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);

    const [selectedLayout, setSelectedLayout] = useState('');
    const [selectedColor, setSelectedColor] = useState('');
    const [quantity, setQuantity] = useState(1);

    // ★ 로그인 여부 유연 판단 (세션 쿠키 및 스토리지 통합 체크)
    const checkIsLoggedIn = () => {
        const hasStorageUser =
            !!localStorage.getItem('member') ||
            !!localStorage.getItem('user') ||
            !!localStorage.getItem('token') ||
            !!sessionStorage.getItem('member') ||
            !!sessionStorage.getItem('user');

        const hasSessionCookie = document.cookie.includes('JSESSIONID');

        // 세션 쿠키가 있거나, 스토리지에 유저 정보가 하나라도 있으면 로그인 상태로 인정
        return hasStorageUser || hasSessionCookie;
    };

    const isLoggedIn = checkIsLoggedIn();

    useEffect(() => {
        const fetchProductDetail = async () => {
            setLoading(true);
            try {
                const data = await getProductDetailApi(id);
                setProduct(data);

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

    const handleLayoutChange = (e) => {
        const newLayout = e.target.value;
        setSelectedLayout(newLayout);
        setSelectedColor('');
    };

    const handleMinusCount = () => {
        if (quantity > 1) setQuantity((prev) => prev - 1);
    };
    const handlePlusCount = () => {
        setQuantity((prev) => prev + 1);
    };

    const isKeyboard =
        product?.prodCtgCd === '1' ||
        product?.prodCtgCd === 'keyboard' ||
        product?.prodCtgNm === '키보드';

    const selectedOption =
        isKeyboard && product?.productOptions
            ? product.productOptions.find(
                (opt) => opt.kbdLayout === selectedLayout && opt.kbdColor === selectedColor
            )
            : null;

    const unitPrice = product?.price || 0;
    const totalPrice = unitPrice * quantity;

    // ★ 장바구니 담기 핸들러
    const handleAddToCart = async () => {
        // 1. [★ 최우선] 로그인 상태 검사 (/api/me 호출)
        try {
            await checkSessionApi();
        } catch (error) {
            // 비회원(401) 상태이면 옵션 검사할 필요도 없이 즉시 차단
            alert('로그인 후 이용해주세요');
            return;
        }

        // 2. 로그인된 회원인 경우에만 옵션 선택 유효성 검사
        if (isKeyboard) {
            if (!selectedLayout) {
                alert('키보드 옵션을 선택해주세요!');
                return;
            }
            if (!selectedColor) {
                alert('색상 옵션을 선택해주세요!');
                return;
            }
        }

        // 3. 장바구니 담기 API 호출
        try {
            const cartRequest = {
                productId: product.id,
                optionId: selectedOption ? selectedOption.id : null,
                count: quantity
            };

            await addToCartApi(cartRequest);

            if (window.confirm('장바구니에 상품을 담았습니다.\n장바구니 페이지로 이동하시겠습니까?')) {
                navigate('/cart');
            }
        } catch (error) {
            console.error('장바구니 담기 실패:', error);
            alert('장바구니 담기에 실패했습니다.');
        }
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

    return (
        <div className="bg-light min-vh-100">
            <HeaderNav />

            <div className="detail-container my-5">
                <div className="detail-left">
                    <img
                        src={product.imgUrl || 'https://via.placeholder.com/400'}
                        style={{ height: '380px' }}
                        alt={product.name}
                    />
                </div>

                <div className="detail-right text-start">
                    <div className="product-name">{product.name}</div>
                    <div className="product-price">
                        {unitPrice.toLocaleString()}원
                    </div>

                    <div className="delivery-info">
                        [배송안내] 평일 오후 2시 이전 결제 시 당일 발송 (CJ대한통운)
                    </div>

                    {isKeyboard && (
                        <>
                            <div className="option-group">
                                <label htmlFor="keyboard-option">키보드 옵션 선택</label>
                                <select
                                    id="keyboard-option"
                                    value={selectedLayout}
                                    onChange={handleLayoutChange}
                                >
                                    <option value="" disabled>옵션을 선택해주세요</option>
                                    {product.productOptions && product.productOptions.length > 0 ? (
                                        [...new Set(product.productOptions.map((opt) => opt.kbdLayout))].map((layout, idx) => (
                                            <option key={idx} value={layout}>{layout}</option>
                                        ))
                                    ) : (
                                        product.kbdLayout && <option value={product.kbdLayout}>{product.kbdLayout}</option>
                                    )}
                                </select>
                            </div>

                            <div className="option-group">
                                <label htmlFor="keyboard-color">색상 선택</label>
                                <select
                                    id="keyboard-color"
                                    value={selectedColor}
                                    onChange={(e) => setSelectedColor(e.target.value)}
                                    disabled={!selectedLayout}
                                >
                                    <option value="" disabled>
                                        {selectedLayout ? '색상을 선택해주세요' : '배열 옵션을 먼저 선택해주세요'}
                                    </option>
                                    {product.productOptions && product.productOptions.length > 0 ? (
                                        [...new Set(
                                            product.productOptions
                                                .filter((opt) => opt.kbdLayout === selectedLayout)
                                                .map((opt) => opt.kbdColor)
                                        )].map((color, idx) => (
                                            <option key={idx} value={color}>{color}</option>
                                        ))
                                    ) : (
                                        product.kbdColor && <option value={product.kbdColor}>{product.kbdColor}</option>
                                    )}
                                </select>
                            </div>
                        </>
                    )}

                    <div className="selected-option-box">
                        {isKeyboard && (selectedLayout || selectedColor) && (
                            <div className="selected-info-text">
                                선택옵션 : {[selectedLayout, selectedColor].filter(Boolean).join(' / ')}
                            </div>
                        )}

                        <div className="quantity-price-row">
                            <div className="quantity-controller">
                                <button type="button" onClick={handleMinusCount}>-</button>
                                <span>{quantity}</span>
                                <button type="button" onClick={handlePlusCount}>+</button>
                            </div>
                            <div className="calculated-price">
                                {totalPrice.toLocaleString()}원
                            </div>
                        </div>
                    </div>

                    <div className="button-group">
                        <button type="button" className="btn btn-cart" onClick={handleAddToCart}>
                            장바구니
                        </button>
                        <button type="button" className="btn btn-buy">
                            구매하기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ProductDetail;