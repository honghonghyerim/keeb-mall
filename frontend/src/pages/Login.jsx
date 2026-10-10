import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom'; //새로고침없이 화면랜더링해주는 도구
import { loginApi } from '../api/authApi'; // 백엔드와 Axios 통신하는 공통함수

function Login() {
    // 1. 입력창 데이터와 에러 메시지를 담을 실시간 변수(state)
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    const navigate = useNavigate(); // 로그인성공후 메인페이지로 보낼때 사용

    // 2. 로그인 버튼 눌렀을 때 실행될 함수 (form action="/login" 대체)
    const handleLogin = async (e) => {
        e.preventDefault(); // 화면 전체 새로고침 방지
        setErrorMessage(''); // 기존 에러메세지 초기화

        try {
            // 스프링 부트 로그인 API 호출
            await loginApi(username, password);
            navigate('/'); // 로그인 성공 시 메인 화면으로 이동
        } catch (error) {
            // 기존 Thymeleaf의 loginError 처리 -> 화면 자동 업데이트
            setErrorMessage('아이디 또는 비밀번호가 올바르지 않습니다.');
        }
    };

    return (
        <div className="bg-light d-flex justify-content-center align-items-center min-vh-100 w-100">
            {/* 부트스트랩 CSS CDN */}
            <link
                href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                rel="stylesheet"
            />

            <div className="card shadow p-4" style={{ width: '400px' }}>
                {/* keeb-Mall 로고 */}
                <h2 className="text-center fw-bold mb-4 text-primary">keeb-Mall</h2>

                {/* 기존 <script> 안의 loginError 메시지 출력 대체 */}
                {errorMessage && (
                    <div className="alert alert-danger p-2 text-center" role="alert">
                        {errorMessage}
                    </div>
                )}

                {/* 로그인 폼 */}
                <form onSubmit={handleLogin}>
                    {/* 아이디 텍스트박스 */}
                    <div className="mb-3 text-start">
                        <label htmlFor="username" className="form-label">아이디</label>
                        <input
                            type="text"
                            className="form-control"
                            id="username"
                            placeholder="아이디를 입력하세요"
                            value={username} //값 세팅
                            onChange={(e) => setUsername(e.target.value)} //키보드를 누를때마다 state 실행해 변수값을 최신상태로 변경
                            required
                        />
                    </div>

                    {/* 비밀번호 텍스트박스 */}
                    <div className="mb-3 text-start">
                        <label htmlFor="password" className="form-label">비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            id="password"
                            placeholder="비밀번호를 입력하세요"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    {/* 로그인 버튼 */}
                    <button type="submit" className="btn btn-primary w-100 mb-2">
                        로그인
                    </button>

                    {/* 회원가입 버튼 (a 태그 대신 Link 사용) */}
                    <Link to="/signup" className="btn btn-outline-secondary w-100">
                        회원가입
                    </Link>

                    {/* 메인으로 돌아가기 버튼 */}
                    <div className="text-center mt-3">
                        <Link to="/" className="text-muted text-decoration-none" style={{ fontSize: '14px' }}>
                            메인으로 돌아가기
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Login;