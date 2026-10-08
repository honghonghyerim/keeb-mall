import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { checkUsernameApi, signupApi } from '../api/authApi';

function Signup() {
    const navigate = useNavigate();

    // 1. 폼 입력값들을 하나의 객체로 관리 (양방향 데이터 바인딩)
    const [formData, setFormData] = useState({
        username: '',
        password: '',
        name: '',
        postcode: '',
        address: '',
        detailAddress: ''
    });

    // 2. 상태 제어 변수들
    const [isUsernameChecked, setIsUsernameChecked] = useState(false); // 중복체크 통과 여부
    const [checkResult, setCheckResult] = useState({ message: '', color: '' }); // 아이디 아래 안내문구
    const [toastMessage, setToastMessage] = useState(''); // 상단 경고 토스트 메시지

    // 3. 카카오 우편번호 서비스 스크립트 불러오기 (index.html 안 건드리고 자동 로드)
    useEffect(() => {
        const script = document.createElement('script');
        script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
        script.async = true;
        document.body.appendChild(script);

        return () => {
            document.body.removeChild(script);
        };
    }, []);

    // 4. 입력창 값 변경 핸들러
    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData(prev => ({
            ...prev,
            [name]: value
        }));

        // ★ 기존 타임리프 스크립트처럼 아이디 글자를 수정하면 중복체크 무조건 초기화!
        if (name === 'username') {
            setIsUsernameChecked(false);
            setCheckResult({ message: '', color: '' });
        }
    };

    // 5. 아이디 중복체크 버튼 클릭
    const handleCheckUsername = async () => {
        const username = formData.username.trim();

        if (!username) {
            setCheckResult({ message: '아이디를 입력하세요!', color: 'red' });
            setTimeout(() => setCheckResult({ message: '', color: '' }), 3000);
            return;
        }

        try {
            const result = await checkUsernameApi(username);

            if (result === 'duplicated') {
                alert('이미 사용 중인 아이디입니다.');
                setFormData(prev => ({ ...prev, username: '' }));
                setIsUsernameChecked(false);
            } else {
                setCheckResult({ message: '사용 가능한 아이디입니다!', color: 'blue' });
                setIsUsernameChecked(true);
                setTimeout(() => setCheckResult({ message: '', color: '' }), 3000);
            }
        } catch (error) {
            console.error('중복체크 에러:', error);
            alert('중복 체크 중 오류가 발생했습니다.');
        }
    };

    // 6. 카카오 주소 팝업 호출
    const handleFindAddr = () => {
        if (window.daum && window.daum.Postcode) {
            new window.daum.Postcode({
                oncomplete: function (data) {
                    let addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;

                    setFormData(prev => ({
                        ...prev,
                        postcode: data.zonecode,
                        address: addr
                    }));
                }
            }).open();
        } else {
            alert('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해 주세요.');
        }
    };

    // 7. 가입하기 폼 제출 이벤트
    const handleSubmit = async (e) => {
        e.preventDefault();

        // [벨리데이션 1] 중복체크 여부 검사
        if (!isUsernameChecked) {
            showToast('아이디 중복 체크를 먼저 해주세요!');
            return;
        }

        // [벨리데이션 2] 주소 입력 여부 검사
        if (!formData.postcode) {
            showToast('주소를 입력해주세요');
            return;
        }

        try {
            await signupApi(formData);
            alert('회원가입이 완료되었습니다!');
            navigate('/login'); // 회원가입 성공 시 로그인 페이지로 이동
        } catch (error) {
            console.error('회원가입 에러:', error);
            showToast(error.response?.data?.message || '회원가입 중 오류가 발생했습니다.');
        }
    };

    // 상단 토스트 메시지 3초 출력 함수
    const showToast = (msg) => {
        setToastMessage(msg);
        setTimeout(() => setToastMessage(''), 3000);
    };

    return (
        <div className="bg-light d-flex justify-content-center align-items-center min-vh-100 w-100 py-4">
            <link
                href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
                rel="stylesheet"
            />

            {/* 토스트 메시지 알림 영역 */}
            {toastMessage && (
                <div className="position-fixed top-0 start-50 translate-middle-x p-3" style={{ zIndex: 1080 }}>
                    <div className="toast show align-items-center text-white bg-dark border-0 p-2">
                        <div className="toast-body">{toastMessage}</div>
                    </div>
                </div>
            )}

            <div className="card shadow py-4 px-5" style={{ width: '460px' }}>
                <h2 className="text-center fw-bold mb-2 text-primary">keeb-Mall</h2>
                <p className="text-center text-muted mb-3" style={{ fontSize: '14px' }}>회원가입</p>

                <form onSubmit={handleSubmit}>
                    {/* 아이디 입력 + 중복체크 */}
                    <div className="mb-3 text-start">
                        <label htmlFor="username" className="form-label fw-bold">아이디</label>
                        <div className="input-group">
                            <input
                                type="text"
                                className="form-control"
                                id="username"
                                name="username"
                                placeholder="아이디를 입력하세요"
                                value={formData.username}
                                onChange={handleChange}
                                required
                            />
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                onClick={handleCheckUsername}
                            >
                                중복체크
                            </button>
                        </div>
                        {checkResult.message && (
                            <div className="form-text" style={{ color: checkResult.color }}>
                                {checkResult.message}
                            </div>
                        )}
                    </div>

                    {/* 비밀번호 입력 */}
                    <div className="mb-3 text-start">
                        <label htmlFor="password" className="form-label fw-bold">비밀번호</label>
                        <input
                            type="password"
                            className="form-control"
                            id="password"
                            name="password"
                            placeholder="비밀번호를 입력하세요"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    {/* 이름 입력 */}
                    <div className="mb-4 text-start">
                        <label htmlFor="name" className="form-label fw-bold">이름</label>
                        <input
                            type="text"
                            className="form-control"
                            id="name"
                            name="name"
                            placeholder="이름을 입력하세요"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    {/* 주소 입력 영역 */}
                    <div className="mb-4 text-start">
                        <label className="form-label fw-bold">주소</label>
                        <div className="input-group mb-2">
                            <input
                                type="text"
                                className="form-control"
                                id="postcode"
                                name="postcode"
                                placeholder="우편번호"
                                value={formData.postcode}
                                readOnly
                            />
                            <button
                                type="button"
                                className="btn btn-outline-secondary"
                                onClick={handleFindAddr}
                            >
                                우편번호 찾기
                            </button>
                        </div>
                        <input
                            type="text"
                            className="form-control mb-1"
                            id="address"
                            name="address"
                            placeholder="기본 주소"
                            value={formData.address}
                            readOnly
                        />
                        <input
                            type="text"
                            className="form-control"
                            id="detailAddress"
                            name="detailAddress"
                            placeholder="상세 주소를 입력하세요"
                            value={formData.detailAddress}
                            onChange={handleChange}
                        />
                    </div>

                    {/* 가입하기 버튼 */}
                    <button type="submit" className="btn btn-primary btn-lg w-100 mb-3">
                        가입하기
                    </button>

                    {/* 로그인 화면 이동 */}
                    <div className="text-center">
                        <Link to="/login" className="text-decoration-none" style={{ fontSize: '15px' }}>
                            이미 계정이 있으신가요? 로그인
                        </Link>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default Signup;