import React from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import HeaderNav from '../components/HeaderNav';

function OrderSuccess() {
    const [searchParams] = useSearchParams();
    const orderId = searchParams.get('orderId');

    return (
        <div className="bg-light min-vh-100">
            <HeaderNav />
            <div className="container my-5 text-center" style={{ maxWidth: '600px' }}>
                <div className="card shadow-sm border-0 p-5 rounded-3">
                    <div className="mb-3">
                        <span className="fs-1">🎉</span>
                    </div>
                    <h3 className="fw-bold text-dark mb-3">주문 및 결제가 완료되었습니다!</h3>
                    <p className="text-secondary mb-4">
                        주문번호: <strong className="text-dark">{orderId}</strong>
                    </p>
                    <div className="d-flex justify-content-center gap-3">
                        <Link to="/" className="btn btn-dark px-4 py-2 fw-bold">
                            메인으로 이동
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default OrderSuccess;