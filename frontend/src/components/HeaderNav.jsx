import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { checkSessionApi, logoutApi } from '../api/authApi';
import '../pages/Main.css'; // 공통 헤더 및 네비바 스타일 적용

function HeaderNav() {
    const navigate = useNavigate();
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    // 페이지 진입 시 로그인 세션 확인
    useEffect(() => {
        const verifySession = async () => {
            try {
                await checkSessionApi(); // 백엔드에 세션 유효한지 확인
                setIsLoggedIn(true); // 성공하면 로그인 상태로 변경!
            } catch (error) {
                setIsLoggedIn(false); // 비로그인 상태 유지
            }
        };
        verifySession();
    }, []);

    // 로그아웃 처리
    const handleLogout = async () => {
            await logoutApi();
            setIsLoggedIn(false);
            // alert('로그아웃 되었습니다.');
            navigate('/login');
    };

    return (
        <header>
            {/* 상단 헤더 */}
            <div className="header-top">
                <div className="logo-area">
                    <Link to="/">KEEB-MALL</Link>
                </div>

                <div className="user-menu">
                    {isLoggedIn ? (
                        <>
                            <button type="button" onClick={handleLogout}>로그아웃</button>
                            <Link to="/mypage" className="hidden-menu">마이페이지</Link>
                            <Link to="/orders">주문조회</Link>
                            <Link to="/cart">장바구니</Link>
                        </>
                    ) : (
                        <>
                            <Link to="/login">로그인</Link>
                            <Link to="/signup">회원가입</Link>
                        </>
                    )}
                </div>
            </div>

            {/* 네비게이션 바 */}
            <nav className="nav-bar">
                <div className="nav-item">
                    <Link to="/">메인</Link>
                </div>

                {/* 키보드 카테고리 */}
                <div className="nav-item">
                    <Link to="/product/keyboard">키보드</Link>
                    <ul className="dropdown-menu">
                        <li><Link to="/product/keyboard/mechanical">기계식</Link></li>
                        <li><Link to="/product/keyboard/capacitive">무접점</Link></li>
                    </ul>
                </div>

                {/* 스위치 카테고리 */}
                <div className="nav-item">
                    <Link to="/product/switch">스위치</Link>
                    <ul className="dropdown-menu">
                        <li><Link to="/product/switch/linear">리니어</Link></li>
                        <li><Link to="/product/switch/tactile">택타일</Link></li>
                    </ul>
                </div>

                {/* 키캡 카테고리 */}
                <div className="nav-item">
                    <Link to="/product/keycap">키캡</Link>
                </div>
            </nav>
        </header>
    );
}

export default HeaderNav;