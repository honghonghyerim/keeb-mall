# KeebMall (키보드 몰)

커스텀 키보드, 스위치, 키캡 등 키보드 애호가를 위한 나만의 쇼핑몰 플랫폼입니다. Spring Boot 3.5.16 기반으로 개발되었습니다.

---

## 🚀 프로젝트 소개
- **목표:** 백엔드 개발자로서의 기술 역량을 입증하고, 실무형 웹 애플리케이션 구조를 학습 및 적용하기 위한 포트폴리오 프로젝트

## 💡 주요 기능
- **회원 관리:** 회원가입(아이디 중복확인, 비밀번호 암호화, 필수값 검증), 로그인/로그아웃, 세션 관리
- **상품 관리:** 전체 상품 조회, 상품 검색(상품명), 상품 상세 페이지 제공(이미지 및 상세 정보)
- **쇼핑 기능:** 장바구니 담기, 수량 조절(+/ -), 주문하기, 체크박스를 통한 상품 선택
- **주문/결제:** 배송 정보 입력, 결제 금액 계산 및 결제 수단 선택
- **배송:** 주문 상품 실시간 배송 상태 조회

## 🛠 개발 환경
- **Language:** Java 17.0.0.1
- **Framework:** Spring Boot 3.5.16
- **Database:** MySQL 8.0
- **Build Tool:** Gradle
- **IDE:** IntelliJ IDEA

## ⚙️ 기술 서비스
- **Authentication:** Spring Session을 활용한 세션 기반 인증
- **Database:** Spring Data JPA를 활용한 ORM 및 쿼리 로깅
- **Validation:** Java Bean Validation 및 비즈니스 로직 기반 유효성 검증

## 📂 프로젝트 파일 구성
```text
keeb-mall
 ├─src/main/java/com/example/keebmall
 │  ├─controller : 웹 요청 처리 및 페이지 라우팅
 │  ├─service    : 비즈니스 로직(가입, 로그인, 중복 체크)
 │  ├─repository : 데이터베이스 접근(JPA)
 │  ├─domain     : 엔티티(Entity) 및 DTO
 │  └─config     : 설정(Security, Database 등)
 └─src/main/resources
    ├─templates  : Thymeleaf HTML 뷰
    └─application.yml : 프로젝트 전역 설정
