import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import axios from "axios";
import HeaderNav from '../components/HeaderNav';
import { checkSessionApi } from '../api/authApi';
import '../css/Cart.css';

function Order() {
    const location = useLocation();
    const navigate = useNavigate();

    // 1. 장바구니나 상세페이지에서 넘어온 주문 상품 목록 (없으면 빈 배열)
    const orderItems = location.state?.orderItems || [];

    // 2. 주문자 정보 State
    const [ordererName, setOrdererName] = useState('');
    const [ordererPhone1, setOrdererPhone1] = useState('010');
    const [ordererPhone2, setOrdererPhone2] = useState('');
    const [ordererPhone3, setOrdererPhone3] = useState('');

    // 3. 배송 정보 State
    const [receiverName, setReceiverName] = useState('');
    const [receiverPhone1, setReceiverPhone1] = useState('010');
    const [receiverPhone2, setReceiverPhone2] = useState('');
    const [receiverPhone3, setReceiverPhone3] = useState('');
    const [postcode, setPostcode] = useState('');
    const [address, setAddress] = useState('');
    const [detailAddress, setDetailAddress] = useState('');
    const [deliveryMessage, setDeliveryMessage] = useState('');

    // 4. 결제수단 & 동의 여부 State
    const [paymentMethod, setPaymentMethod] = useState('kakaopay'); // 기본값: 카카오페이
    const [isAgreed, setIsAgreed] = useState(false);

    // 전화번호/지역번호 프리셋 목록
    const phonePrefixes = [
        '010', '011', '016', '017', '018', '019',
        '02', '031', '032', '033', '041', '042', '043',
        '051', '052', '053', '054', '055', '061', '062', '063', '064',
        '070', '0502', '0505', '0507'
    ];

    // 주문 상품이 없을 경우 안내 처리
    useEffect(() => {
        if (!orderItems || orderItems.length === 0) {
            alert('주문할 상품이 없습니다. 장바구니로 이동합니다.');
            navigate('/cart');
        }
        // 로그인된 회원이면 이름 가져와서 주문자 이름 기본값 설정
        const fetchLoginUser = async () => {
            try {
                const res = await checkSessionApi();

                // res가 Axios response 객체(res.data)일 수도 있고, 순수 데이터일 수도 있으니 모두 대응
                const userData = res?.data || res;
                const userName = userData?.name || userData?.memberName || userData?.mbrName || '';

                if (userName) {
                    setOrdererName(userName); // 주문자 이름 input 세팅!
                }
            } catch (error) {
                console.log('세션 정보 조회 실패 또는 비로그인 상태:', error);
            }
        };

        fetchLoginUser();
    }, [orderItems, navigate]);

    // 숫자 전용 입력 핸들러 (연락처용)
    const handleNumberInput = (setter, maxLength) => (e) => {
        const value = e.target.value.replace(/[^0-9]/g, '');
        if (value.length <= maxLength) {
            setter(value);
        }
    };

    // 주문자 정보와 동일 체크 박스
    const handleCopyOrdererInfo = (e) => {
        if (e.target.checked) {
            setReceiverName(ordererName);
            setReceiverPhone1(ordererPhone1);
            setReceiverPhone2(ordererPhone2);
            setReceiverPhone3(ordererPhone3);
        }
    };

    // 다음 우편번호 카카오 API 연동
    const openDaumPostcode = () => {
        new window.daum.Postcode({
            oncomplete: (data) => {
                let fullAddress = data.address;
                let extraAddress = '';

                if (data.addressType === 'R') {
                    if (data.bname !== '') extraAddress += data.bname;
                    if (data.buildingName !== '') {
                        extraAddress += extraAddress !== '' ? `, ${data.buildingName}` : data.buildingName;
                    }
                    fullAddress += extraAddress !== '' ? ` (${extraAddress})` : '';
                }

                setPostcode(data.zonecode);
                setAddress(fullAddress);
            }
        }).open();
    };

// 2. 주소 검색 버튼 클릭 핸들러 (타이밍 이슈 완벽 해결!)
    const handleSearchAddress = () => {
        // 이미 스크립트가 로드되어 있는 경우 바로 실행
        if (window.daum && window.daum.Postcode) {
            openDaumPostcode();
            return;
        }

        // 아직 스크립트가 안 넘어왔다면, 자바스크립트로 직접 스크립트 태그를 꽂아서 불러온 뒤 실행
        const script = document.createElement('script');
        script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
        script.onload = () => {
            openDaumPostcode(); // 다운로드 끝나자마자 바로 팝업 띄우기!
        };
        document.body.appendChild(script);
    };

    // 금액 계산
    const totalProductPrice = orderItems.reduce(
        (acc, item) => acc + item.price * item.count,
        0
    );
    const shippingFee = totalProductPrice >= 50000 || totalProductPrice === 0 ? 0 : 3000;
    const finalTotalPrice = totalProductPrice + shippingFee;

    // 결제하기 버튼 클릭
    const handleSubmitOrder = async (e) => {
        e.preventDefault();

        if (!ordererName.trim()) {
            alert('주문자 이름을 입력해 주세요.');
            return;
        }
        if (!ordererPhone2 || !ordererPhone3) {
            alert('주문자 연락처를 입력해 주세요.');
            return;
        }
        if (!receiverName.trim()) {
            alert('받는 분 이름을 입력해 주세요.');
            return;
        }
        if (!receiverPhone2 || !receiverPhone3) {
            alert('받는 분 연락처를 입력해 주세요.');
            return;
        }
        if (!postcode || !address) {
            alert('배송지 주소를 입력해 주세요.');
            return;
        }
        if (!isAgreed) {
            alert('구매 진행 동의에 체크해 주세요.');
            return;
        }

        const firstProductName = orderItems[0]?.productName || '키보드 상품';
        const itemName = orderItems.length > 1
            ? `${firstProductName} 외 ${orderItems.length - 1}건`
            : firstProductName;

        const cartItemIds = orderItems
            .map(item => item.cartInfoId || item.cartItemId || item.id)
            .filter(id => id !== null && id !== undefined); // ★ 유효한 ID만 추출!

        const orderData = {
            orderer: {
                name: ordererName,
                phone: `${ordererPhone1}-${ordererPhone2}-${ordererPhone3}`
            },
            shipping: {
                name: receiverName,
                phone: `${receiverPhone1}-${receiverPhone2}-${receiverPhone3}`,
                postcode,
                address,
                detailAddress,
                message: deliveryMessage
            },
            paymentMethod,
            totalPrice: finalTotalPrice,
            itemName,
            cartItemIds, // ★ Clean한 ID 배열 전달
            items: orderItems
        };
        alert(`결제하시겠습니까?`);

        if (paymentMethod === 'kakaopay') {
            try {
                // 백엔드 카카오페이 결제 준비 API 호출
                const res = await axios.post('/api/payment/ready', orderData, {
                    withCredentials: true // 세션 쿠키(JSESSIONID) 함께 전달
                });

                // 백엔드에서 받아온 카카오페이 결제 페이지 URL로 이동!
                if (res.data && res.data.next_redirect_pc_url) {
                    window.location.href = res.data.next_redirect_pc_url;
                } else {
                    alert('카카오페이 결제 페이지 주소를 불러오지 못했습니다.');
                }
            } catch (error) {
                console.error('카카오페이 결제 준비 실패:', error);
                alert('결제 준비 중 오류가 발생했습니다.');
            }
        } else if (paymentMethod === 'card') {
            const { IMP } = window;
            if (!IMP) {
                alert("결제 모듈을 불러오는 중입니다. 잠시 후 다시 시도해 주세요.");
                return;
            }

            // ★ 찾으신 V1 고객사 식별코드 (예: "imp12345678")
            IMP.init("imp50621224");

            const timeStamp = new Date().toISOString().replace(/[-:T.]/g, "").slice(0, 14);
            const randomNum = Math.floor(1000 + Math.random() * 9000);
            const orderId = `ORD_${timeStamp}_${randomNum}`;

            IMP.request_pay({
                pg: "html5_inicis",           // KG이니시스 기본 PG 코드
                pay_method: "card",            // 카드 결제
                merchant_uid: orderId,         // 주문번호
                name: itemName,                // 상품명
                amount: finalTotalPrice,       // 결제금액
                buyer_name: ordererName,       // 주문자 이름
                buyer_tel: `${ordererPhone1}-${ordererPhone2}-${ordererPhone3}`,
            }, async (rsp) => {
                if (rsp.success) {
                    try {
                        // 백엔드 카드 결제 검증 API 호출
                        const res = await axios.post("/api/payment/verify", {
                            impUid: rsp.imp_uid,
                            merchantUid: rsp.merchant_uid,
                            cartItemIds: cartItemIds
                        }, { withCredentials: true });

                        if (res.status === 200) {
                            alert("카드 결제가 완료되었습니다!");
                            navigate(`/order/success?orderId=${rsp.merchant_uid}`);
                        }
                    } catch (err) {
                        console.error("카드 결제 처리 에러:", err);
                        alert("결제 처리 중 에러가 발생했습니다.");
                    }
                } else {
                    alert(`결제 실패: ${rsp.error_msg}`);
                }
            });
        }


    };

    return (
        <div className="bg-light min-vh-100 pb-5">
            <HeaderNav />

            <div className="container my-5 text-start" style={{ maxWidth: '1000px' }}>
                <h3 className="fw-bold mb-4 text-dark border-bottom pb-3">결제정보</h3>

                <form onSubmit={handleSubmitOrder}>
                    {/* 1. 주문 상품 정보 */}
                    <div className="card shadow-sm mb-4 border border-secondary-subtle">
                        <div className="card-header bg-dark text-white py-3">
                            <h5 className="fw-bold mb-0 fs-6">상품정보</h5>
                        </div>
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <table className="table align-middle text-center mb-0">
                                    <thead className="table-light border-bottom">
                                    <tr>
                                        <th style={{ width: '40%' }}>상품명</th>
                                        <th style={{ width: '25%' }}>상품옵션</th>
                                        <th style={{ width: '15%' }}>수량</th>
                                        <th style={{ width: '20%' }}>총금액</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {orderItems.map((item, idx) => (
                                        <tr key={idx}>
                                            <td className="text-start ps-4 py-3">
                                                <div className="d-flex align-items-center gap-3">
                                                    <img
                                                        src={item.imgUrl || 'https://via.placeholder.com/80'}
                                                        alt={item.productName}
                                                        style={{ width: '60px', height: '60px', objectFit: 'cover' }}
                                                        className="rounded border"
                                                    />
                                                    <span className="fw-bold text-dark">{item.productName}</span>
                                                </div>
                                            </td>
                                            <td>
                                                {item.kbdLayout || item.kbdColor ? (
                                                    <span className="badge bg-light text-dark border border-secondary-subtle px-2 py-1 fw-normal">
                                                            {[item.kbdLayout, item.kbdColor].filter(Boolean).join(' / ')}
                                                        </span>
                                                ) : (
                                                    <span className="text-muted small">-</span>
                                                )}
                                            </td>
                                            <td className="fw-medium">{item.count}개</td>
                                            <td className="fw-bold text-dark">
                                                {(item.price * item.count).toLocaleString()}원
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                    {/* tfoot 정렬 개선 */}
                                    <tfoot className="table-light border-top">
                                    <tr>
                                        <td colSpan={4} className="p-4">
                                            <div className="d-flex justify-content-end align-items-center gap-4 text-end">
                                                <div className="d-flex flex-column align-items-center">
                                                    <span className="text-muted small mb-1">주문금액</span>
                                                    <span className="fw-bold fs-6 text-dark">{totalProductPrice.toLocaleString()}원</span>
                                                </div>
                                                <div className="text-muted fs-5 fw-light pt-3">+</div>
                                                <div className="d-flex flex-column align-items-center">
                                                    <span className="text-muted small mb-1">배송비</span>
                                                    <span className="fw-bold fs-6 text-dark">
                                                            {shippingFee === 0 ? '0원 (무료)' : `${shippingFee.toLocaleString()}원`}
                                                        </span>
                                                </div>
                                                <div className="text-muted fs-5 fw-light pt-3">=</div>
                                                <div className="ps-3 border-start border-2 d-flex flex-column align-items-center">
                                                    <span className="text-muted small mb-1">총결제금액</span>
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
                        </div>
                    </div>

                    {/* 2. 주문자 정보 */}
                    <div className="card shadow-sm mb-4 border border-secondary-subtle">
                        <div className="card-header bg-white py-3 border-bottom">
                            <h5 className="fw-bold mb-0 fs-6 text-dark">주문자정보</h5>
                        </div>
                        <div className="card-body p-4">
                            <div className="row mb-3 align-items-center">
                                <label className="col-sm-2 col-form-label fw-bold text-secondary">이름</label>
                                <div className="col-sm-5">
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="주문자 이름"
                                        value={ordererName}
                                        onChange={(e) => setOrdererName(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="row align-items-center">
                                <label className="col-sm-2 col-form-label fw-bold text-secondary">연락처</label>
                                <div className="col-sm-8 d-flex align-items-center gap-2">
                                    <select
                                        className="form-select text-center"
                                        style={{ width: '95px' }}
                                        value={ordererPhone1}
                                        onChange={(e) => setOrdererPhone1(e.target.value)}
                                    >
                                        {phonePrefixes.map((prefix) => (
                                            <option key={prefix} value={prefix}>{prefix}</option>
                                        ))}
                                    </select>
                                    <span>-</span>
                                    <input
                                        type="text"
                                        className="form-control text-center"
                                        style={{ width: '100px' }}
                                        maxLength={4}
                                        value={ordererPhone2}
                                        onChange={handleNumberInput(setOrdererPhone2, 4)}
                                    />
                                    <span>-</span>
                                    <input
                                        type="text"
                                        className="form-control text-center"
                                        style={{ width: '100px' }}
                                        maxLength={4}
                                        value={ordererPhone3}
                                        onChange={handleNumberInput(setOrdererPhone3, 4)}
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* 3. 배송 정보 */}
                    <div className="card shadow-sm mb-4 border border-secondary-subtle">
                        <div className="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
                            <h5 className="fw-bold mb-0 fs-6 text-dark">배송정보</h5>
                            <div className="form-check fs-6 mb-0">
                                <input
                                    type="checkbox"
                                    className="form-check-input"
                                    id="copyInfo"
                                    onChange={handleCopyOrdererInfo}
                                />
                                <label className="form-check-label text-secondary fw-normal" htmlFor="copyInfo">
                                    주문자 정보와 동일
                                </label>
                            </div>
                        </div>
                        <div className="card-body p-4">
                            <div className="row mb-3 align-items-center">
                                <label className="col-sm-2 col-form-label fw-bold text-secondary">이름</label>
                                <div className="col-sm-5">
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="받는 분 이름"
                                        value={receiverName}
                                        onChange={(e) => setReceiverName(e.target.value)}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="row mb-3 align-items-center">
                                <label className="col-sm-2 col-form-label fw-bold text-secondary">연락처</label>
                                <div className="col-sm-8 d-flex align-items-center gap-2">
                                    <select
                                        className="form-select text-center"
                                        style={{ width: '95px' }}
                                        value={receiverPhone1}
                                        onChange={(e) => setReceiverPhone1(e.target.value)}
                                    >
                                        {phonePrefixes.map((prefix) => (
                                            <option key={prefix} value={prefix}>{prefix}</option>
                                        ))}
                                    </select>
                                    <span>-</span>
                                    <input
                                        type="text"
                                        className="form-control text-center"
                                        style={{ width: '100px' }}
                                        maxLength={4}
                                        value={receiverPhone2}
                                        onChange={handleNumberInput(setReceiverPhone2, 4)}
                                    />
                                    <span>-</span>
                                    <input
                                        type="text"
                                        className="form-control text-center"
                                        style={{ width: '100px' }}
                                        maxLength={4}
                                        value={receiverPhone3}
                                        onChange={handleNumberInput(setReceiverPhone3, 4)}
                                    />
                                </div>
                            </div>
                            <div className="row mb-3">
                                <label className="col-sm-2 col-form-label fw-bold text-secondary pt-2">주소</label>
                                <div className="col-sm-9">
                                    <div className="d-flex gap-2 mb-2">
                                        <input
                                            type="text"
                                            className="form-control"
                                            placeholder="우편번호"
                                            style={{ width: '140px' }}
                                            value={postcode}
                                            readOnly
                                        />
                                        <button
                                            type="button"
                                            className="btn btn-dark"
                                            onClick={handleSearchAddress}
                                        >
                                            주소 검색
                                        </button>
                                    </div>
                                    <input
                                        type="text"
                                        className="form-control mb-2"
                                        placeholder="기본주소"
                                        value={address}
                                        readOnly
                                    />
                                    <input
                                        type="text"
                                        className="form-control"
                                        placeholder="상세주소 입력"
                                        value={detailAddress}
                                        onChange={(e) => setDetailAddress(e.target.value)}
                                    />
                                </div>
                            </div>
                            <div className="row mb-3">
                                <label className="col-sm-2 col-form-label fw-medium text-secondary pt-2">
                                    배송메시지
                                </label>
                                <div className="col-sm-9">
                                    <input
                                        type="text"
                                        className="form-control border-light-subtle"
                                        placeholder="배송시 요청사항을 입력해 주세요. (50자 이내)"
                                        maxLength={50}
                                        value={deliveryMessage}
                                        onChange={(e) => setDeliveryMessage(e.target.value)}
                                    />
                                    <div className="text-end text-muted small mt-1">
                                        {deliveryMessage.length} / 50자
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* 4. 결제 정보 */}
                    <div className="card shadow-sm mb-4 border border-secondary-subtle">
                        <div className="card-header bg-white py-3 border-bottom">
                            <h5 className="fw-bold mb-0 fs-6 text-dark">결제정보</h5>
                        </div>
                        <div className="card-body p-4">
                            <div className="d-flex flex-wrap gap-5">
                                <div className="form-check">
                                <input
                                        type="radio"
                                        className="form-check-input"
                                        name="paymentMethod"
                                        id="kakaopay"
                                        value="kakaopay"
                                        checked={paymentMethod === 'kakaopay'}
                                        onChange={(e) => setPaymentMethod(e.target.value)}
                                    />
                                    <label className="form-check-label fw-bold" htmlFor="kakaopay">
                                        카카오페이
                                    </label>
                                </div>
                                <div className="form-check">
                                    <input
                                        type="radio"
                                        className="form-check-input"
                                        name="paymentMethod"
                                        id="card"
                                        value="card"
                                        checked={paymentMethod === 'card'}
                                        onChange={(e) => setPaymentMethod(e.target.value)}
                                    />
                                    <label className="form-check-label fw-bold" htmlFor="card">
                                        카드결제
                                    </label>
                                </div>
                                <div className="form-check">
                                    <input
                                        type="radio"
                                        className="form-check-input"
                                        name="paymentMethod"
                                        id="bank"
                                        value="bank"
                                        checked={paymentMethod === 'bank'}
                                        onChange={(e) => setPaymentMethod(e.target.value)}
                                    />
                                    <label className="form-check-label fw-bold" htmlFor="bank">
                                        무통장입금
                                    </label>
                                </div>
                                <div className="form-check">
                                    <input
                                        type="radio"
                                        className="form-check-input"
                                        name="paymentMethod"
                                        id="phone"
                                        value="phone"
                                        checked={paymentMethod === 'phone'}
                                        onChange={(e) => setPaymentMethod(e.target.value)}
                                    />
                                    <label className="form-check-label fw-bold" htmlFor="phone">
                                        휴대폰결제
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* 5. 주문 동의 (체크박스 네모박스 클릭시에만 동의 전환) */}
                    <div className="card shadow-sm mb-4 border border-secondary-subtle">
                        <div className="card-body p-4 text-center">
                            <div className="d-inline-flex align-items-center gap-2">
                                <input
                                    type="checkbox"
                                    className="form-check-input my-0"
                                    style={{ width: '20px', height: '20px', cursor: 'pointer' }}
                                    checked={isAgreed}
                                    onChange={(e) => setIsAgreed(e.target.checked)}
                                />
                                <span className="fw-bold text-dark user-select-none">
                                    상기 결제정보를 확인하였으며, 구매진행에 동의합니다.
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* 6. 최종 결제 금액 및 결제하기 버튼 */}
                    <div className="text-center mt-5">
                        <div className="mb-4">
                            <span className="text-muted me-3 fs-5">최종 결제 금액 :</span>
                            <span className="fw-bold fs-2 text-dark">
                                {finalTotalPrice.toLocaleString()}원
                            </span>
                        </div>

                        <div className="d-flex justify-content-center gap-3">
                            <button
                                type="submit"
                                className="btn btn-dark btn-lg px-5 py-3 fw-bold border-0"
                                style={{ backgroundColor: '#212529', minWidth: '220px' }}
                            >
                                {finalTotalPrice.toLocaleString()}원 결제하기
                            </button>
                            <Link
                                to="/cart"
                                className="btn btn-outline-dark btn-lg px-4 py-3 fw-bold"
                                style={{ minWidth: '160px' }}
                            >
                                장바구니로 돌아가기
                            </Link>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Order;