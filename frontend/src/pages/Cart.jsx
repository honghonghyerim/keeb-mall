import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import HeaderNav from '../components/HeaderNav';
import {
    getCartListApi,
    updateCartCountApi,
    deleteCartItemApi
} from '../api/cartApi';
import '../css/Cart.css';

function Cart() {
    const navigate = useNavigate();

    // 장바구니 데이터 및 상태 관리
    const [cartList, setCartList] = useState([]);
    const [selectedIds, setSelectedIds] = useState([]);
    const [loading, setLoading] = useState(true);

    // ★ DB 장바구니 목록 조회 (헤더 클릭 진입 & 상세페이지에서 담기 후 이동 진입 모두 대응)
    const fetchCartList = async () => {
        setLoading(true);
        try {
            const data = await getCartListApi();
            const list = data || [];
            setCartList(list);
            // 기본 전체 체크 선택 상태로 초기화
            setSelectedIds(list.map((item) => item.cartInfoId));
        } catch (error) {
            console.error('장바구니 목록 조회 실패:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchCartList();
    }, []);

    // 전체 선택 / 해제
    const handleSelectAll = (e) => {
        if (e.target.checked) {
            setSelectedIds(cartList.map((item) => item.cartInfoId));
        } else {
            setSelectedIds([]);
        }
    };

    // 개별 항목 선택 / 해제
    const handleSelectOne = (id) => {
        if (selectedIds.includes(id)) {
            setSelectedIds(selectedIds.filter((item) => item !== id));
        } else {
            setSelectedIds([...selectedIds, id]);
        }
    };

    // 수량 변경 (+ / -) 및 DB 즉시 반영
    const handleQuantityChange = async (cartInfoId, delta) => {
        const targetItem = cartList.find((item) => item.cartInfoId === cartInfoId);
        if (!targetItem) return;

        const newCount = targetItem.count + delta;
        if (newCount < 1) return;

        try {
            await updateCartCountApi(cartInfoId, newCount);
            setCartList((prev) =>
                prev.map((item) =>
                    item.cartInfoId === cartInfoId ? { ...item, count: newCount } : item
                )
            );
        } catch (error) {
            console.error('수량 변경 실패:', error);
            alert('수량 변경에 실패했습니다.');
        }
    };

    // 개별 항목 삭제 및 DB 반영
    const handleDeleteOne = async (cartInfoId) => {
        if (window.confirm('해당 상품을 장바구니에서 삭제하시겠습니까?')) {
            try {
                await deleteCartItemApi(cartInfoId);
                setCartList((prev) => prev.filter((item) => item.cartInfoId !== cartInfoId));
                setSelectedIds((prev) => prev.filter((id) => id !== cartInfoId));
            } catch (error) {
                console.error('삭제 실패:', error);
                alert('삭제에 실패했습니다.');
            }
        }
    };

    // 선택 항목 일괄 삭제 및 DB 반영
    const handleDeleteSelected = async () => {
        if (selectedIds.length === 0) {
            alert('삭제할 상품을 선택해주세요.');
            return;
        }

        if (window.confirm('선택한 상품을 삭제하시겠습니까?')) {
            try {
                await Promise.all(selectedIds.map((id) => deleteCartItemApi(id)));
                setCartList((prev) => prev.filter((item) => !selectedIds.includes(item.cartInfoId)));
                setSelectedIds([]);
            } catch (error) {
                console.error('선택 삭제 실패:', error);
                alert('선택 삭제에 실패했습니다.');
            }
        }
    };

    // 주문 처리 핸들러
    const handleOrderAll = () => {
        if (cartList.length === 0) {
            alert('장바구니가 비어있습니다.');
            return;
        }
        navigate('/order', { state: { orderItems: cartList } });
    }

    const handleOrderSelected = () => {
        if (selectedIds.length === 0) {
            alert('주문할 상품을 선택해주세요.');
            return;
        }
        const targetItems = cartList.filter((item) => selectedIds.includes(item.cartInfoId));
        navigate('/order', { state: { orderItems: targetItems } });
    };

    // 선택된 상품들의 금액 및 배송비 계산
    const selectedItems = cartList.filter((item) => selectedIds.includes(item.cartInfoId));
    const totalProductPrice = selectedItems.reduce(
        (acc, item) => acc + item.price * item.count,
        0
    );

    const shippingFee = selectedItems.length === 0 ? 0 : totalProductPrice >= 50000 ? 0 : 3000;
    const finalTotalPrice = totalProductPrice + shippingFee;

    if (loading) {
        return (
            <div className="bg-light min-vh-100">
                <HeaderNav />
                <div className="text-center py-5">
                    <div className="spinner-border text-dark" role="status">
                        <span className="visually-hidden">로딩 중...</span>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="bg-light min-vh-100">
            <HeaderNav />

            <div className="container my-5" style={{ maxWidth: '1100px' }}>
                <h3 className="fw-bold mb-4 text-start text-dark">장바구니</h3>

                {cartList.length === 0 ? (
                    <div className="bg-white p-5 rounded shadow-sm text-center my-4 border">
                        <p className="text-muted fs-5 mb-4">장바구니에 담긴 상품이 없습니다.</p>
                        <Link to="/" className="btn btn-dark px-4 py-2">
                            쇼핑 계속하기
                        </Link>
                    </div>
                ) : (
                    <>
                        {/* 상단 컨트롤 영역 */}
                        <div className="d-flex justify-content-between align-items-center mb-3">
                            <button
                                type="button"
                                className="btn btn-secondary btn-sm px-3 py-2 fw-medium border-0"
                                onClick={handleDeleteSelected}
                                style={{ backgroundColor: '#495057' }}
                            >
                                선택한 상품 삭제
                            </button>
                            <span className="text-secondary fs-6">
                                전체 <strong className="text-dark">{cartList.length}</strong>개
                            </span>
                        </div>

                        {/* 통합 장바구니 테이블 */}
                        <div className="table-responsive bg-white rounded shadow-sm mb-4 border">
                            <table className="table align-middle text-center mb-0 custom-cart-table">
                                <thead className="table-light border-bottom">
                                <tr>
                                    <th style={{ width: '5%' }}>
                                        <input
                                            type="checkbox"
                                            className="form-check-input"
                                            checked={
                                                cartList.length > 0 &&
                                                selectedIds.length === cartList.length
                                            }
                                            onChange={handleSelectAll}
                                        />
                                    </th>
                                    <th style={{ width: '35%' }}>상품명</th>
                                    <th style={{ width: '18%' }}>상품옵션</th>
                                    <th style={{ width: '12%' }}>판매가</th>
                                    <th style={{ width: '13%' }}>수량</th>
                                    <th style={{ width: '12%' }}>합계</th>
                                    <th style={{ width: '5%' }}></th>
                                </tr>
                                </thead>
                                <tbody>
                                {cartList.map((item) => {
                                    const itemUnitPrice = item.price;
                                    const itemTotalPrice = itemUnitPrice * item.count; // ★ 곱하기 연산자 문법 오류 수정
                                    const isSelected = selectedIds.includes(item.cartInfoId);

                                    return (
                                        <tr key={item.cartInfoId}>
                                            <td>
                                                <input
                                                    type="checkbox"
                                                    className="form-check-input"
                                                    checked={isSelected}
                                                    onChange={() => handleSelectOne(item.cartInfoId)}
                                                />
                                            </td>
                                            <td className="text-start">
                                                <div className="d-flex align-items-center gap-3">
                                                    <img
                                                        src={item.imgUrl || 'https://via.placeholder.com/100'}
                                                        alt={item.productName}
                                                        className="cart-thumb-img rounded"
                                                    />
                                                    <Link
                                                        to={`/product/detail/${item.productId}`}
                                                        className="fw-bold text-dark text-decoration-none product-link"
                                                    >
                                                        {item.productName}
                                                    </Link>
                                                </div>
                                            </td>
                                            <td>
                                                {item.kbdLayout || item.kbdColor ? (
                                                    <span className="badge bg-light text-dark border border-secondary-subtle px-2 py-1 fw-normal">
                                                            {[item.kbdLayout, item.kbdColor]
                                                                .filter(Boolean)
                                                                .join(' / ')}
                                                        </span>
                                                ) : (
                                                    <span className="text-muted small">-</span>
                                                )}
                                            </td>
                                            <td className="fw-medium">
                                                {itemUnitPrice.toLocaleString()}원
                                            </td>
                                            <td>
                                                <div className="quantity-btn-group mx-auto">
                                                    <button
                                                        type="button"
                                                        onClick={() => handleQuantityChange(item.cartInfoId, -1)}
                                                    >
                                                        -
                                                    </button>
                                                    <span>{item.count}</span>
                                                    <button
                                                        type="button"
                                                        onClick={() => handleQuantityChange(item.cartInfoId, 1)}
                                                    >
                                                        +
                                                    </button>
                                                </div>
                                            </td>
                                            <td className="fw-bold text-dark">
                                                {itemTotalPrice.toLocaleString()}원
                                            </td>
                                            <td>
                                                <button
                                                    type="button"
                                                    className="btn-close btn-sm"
                                                    aria-label="Delete"
                                                    onClick={() => handleDeleteOne(item.cartInfoId)}
                                                />
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>

                                {/* 하단 결제 정보 통합 요약 */}
                                <tfoot className="table-light border-top">
                                <tr>
                                    <td colSpan={7} className="p-4">
                                        <div className="d-flex justify-content-end align-items-center gap-4 text-end">
                                            <div>
                                                <span className="text-muted small d-block mb-1">총 상품금액</span>
                                                <span className="fw-bold fs-6 text-dark">{totalProductPrice.toLocaleString()}원</span>
                                            </div>
                                            <div className="text-muted fs-5 fw-light">+</div>
                                            <div>
                                                <span className="text-muted small d-block mb-1">배송비</span>
                                                <span className="fw-bold fs-6 text-dark">
                                                        {shippingFee === 0 ? '0원 (무료)' : `${shippingFee.toLocaleString()}원`}
                                                    </span>
                                            </div>
                                            <div className="text-muted fs-5 fw-light">=</div>
                                            <div className="ps-3 border-start border-2">
                                                <span className="text-muted small d-block mb-1">총 결제예정금액</span>
                                                <span className="fw-bold fs-4 text-dark">
                                                        {finalTotalPrice.toLocaleString()}원
                                                    </span>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                                </tfoot>
                            </table>
                        </div>

                        {/* 하단 모노톤 정렬 버튼 그룹 */}
                        <div className="d-flex justify-content-center align-items-center gap-3 mt-4">
                            <button
                                type="button"
                                className="btn btn-dark btn-lg px-4 py-3 fw-bold border-0"
                                onClick={handleOrderAll}
                                disabled={cartList.length === 0}
                                style={{ backgroundColor: '#212529', minWidth: '160px' }}
                            >
                                전체상품주문
                            </button>

                            <button
                                // to="/order"
                                type="button"
                                className="btn btn-secondary btn-lg px-4 py-3 fw-bold border-0"
                                onClick={handleOrderSelected}
                                disabled={selectedIds.length === 0}
                                style={{ backgroundColor: '#495057', minWidth: '160px' }}
                            >
                                선택상품주문
                            </button>

                            <Link
                                to="/"
                                className="btn btn-outline-dark btn-lg px-4 py-3 fw-bold text-decoration-none"
                                style={{ minWidth: '140px' }}
                            >
                                메인으로
                            </Link>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
}

export default Cart;