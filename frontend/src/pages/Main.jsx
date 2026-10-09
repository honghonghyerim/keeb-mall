import React, { useState, useEffect } from 'react';
import '../css/Main.css';
import HeaderNav from "../components/HeaderNav";

function Main() {

    return (
        <div>
            {/* 상단 영역 */}
            <HeaderNav />
            {/* 히어로 섹션 (키보드 대문짝 이미지) */}
            <section className="hero-section">
                <img
                    src="https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=1920&q=80"
                    alt="Custom Keyboard Hero Image"
                />
                <div className="hero-text">
                    <h1>Precision in Every Keystroke</h1>
                    <p>나만의 완벽한 타이핑 경험을 만나보세요.</p>
                </div>
            </section>
        </div>
    );
}

export default Main;