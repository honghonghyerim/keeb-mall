package com.example.keebmall.controller;

import com.example.keebmall.domain.Member;
import com.example.keebmall.repository.CartInfoRepository;
import com.example.keebmall.service.KakaoPayService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    private CartInfoRepository cartInfoRepository;

    private final KakaoPayService kakaoPayService;


    @PostMapping("/ready")
    public ResponseEntity<?> readyPayment(@RequestBody Map<String, Object> requestData, HttpSession session) {
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        // 주문번호 생성 (년월일시분초 + 4자리 난수)
        String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomNum = String.format("%04d", new Random().nextInt(10000));
        String orderId = timeStamp + "-" + randomNum;

        String userId = loginUser.getUsername();
        String itemName = (String) requestData.get("itemName");

        Number totalPriceNum = (Number) requestData.get("totalPrice");
        int totalPrice = (totalPriceNum != null) ? totalPriceNum.intValue() : 0;

        // ★ 리액트에서 넘겨준 장바구니 아이템 ID 리스트 세션에 저장
        List<Integer> cartItemIds = (List<Integer>) requestData.get("cartItemIds");
        session.setAttribute("pay_cartItemIds", cartItemIds);

        // 카카오페이 Ready API 호출
        Map<String, Object> resultMap = kakaoPayService.prepareKakaoPay(orderId, userId, itemName, totalPrice);

        String tid = (String) resultMap.get("tid");
        session.setAttribute("pay_tid", tid);
        session.setAttribute("pay_orderId", orderId);

        System.out.println("★ [결제준비] 프론트에서 넘어온 cartItemIds: " + cartItemIds);

        return ResponseEntity.ok(resultMap);


    }

    // 2. 카카오페이 결제 성공 시 리다이렉트될 콜백 URL
    @GetMapping("/success")
    public void paymentSuccess(@RequestParam("pg_token") String pgToken,
                               @RequestParam("orderId") String orderId,
                               HttpSession session,
                               HttpServletResponse response) throws IOException {

        Member loginUser = (Member) session.getAttribute("loginUser");
        String tid = (String) session.getAttribute("pay_tid");
        List<?> cartItemIds = (List<?>) session.getAttribute("pay_cartItemIds");

        if (loginUser != null && tid != null) {
            // 1. 카카오페이 Approve API 승인
            Map<String, Object> approveResult = kakaoPayService.approveKakaoPay(tid, orderId, loginUser.getUsername(), pgToken);

            // 2. 장바구니 상태 변경 (null 에러 방지 처리)
            if (cartItemIds != null && !cartItemIds.isEmpty()) {
                for (Object idObj : cartItemIds) {
                    // ★ null 값 방어 코드 추가!
                    if (idObj == null) continue;

                    try {
                        Long cartItemId = Long.valueOf(idObj.toString());
                        cartInfoRepository.findById(cartItemId).ifPresent(cartItem -> {
                            cartItem.setStatus("Y"); // 또는 cartItem.setDelYn("Y");
                            cartInfoRepository.save(cartItem);
                        });
                    } catch (NumberFormatException e) {
                        System.err.println("★ 잘못된 장바구니 ID 포맷: " + idObj);
                    }
                }
            }

            // 세션 임시값 삭제
            session.removeAttribute("pay_tid");
            session.removeAttribute("pay_orderId");
            session.removeAttribute("pay_cartItemIds");

            // 프론트 결제 완료 페이지로 리다이렉트
            response.sendRedirect("http://localhost:5173/order/success?orderId=" + orderId);
            System.out.println("★ [결제성공] 세션에서 꺼낸 cartItemIds: " + cartItemIds);
        } else {
            response.sendRedirect("http://localhost:5173/order/fail");
        }
    }
}