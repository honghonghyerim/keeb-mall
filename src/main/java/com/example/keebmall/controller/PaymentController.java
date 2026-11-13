
package com.example.keebmall.controller;

import com.example.keebmall.domain.Member;
import com.example.keebmall.service.KakaoPayService;
import com.example.keebmall.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final KakaoPayService kakaoPayService;
    private final OrderService orderService;

    @Value("${portone.api-secret}")
    private String portoneApiSecret;


    private OrderRequestResult createOrderFromRequest(
            Long memberId,
            Map<String, Object> requestData
    ) {
        // 바로 구매인지 확인
        if (Boolean.TRUE.equals(requestData.get("directPurchase"))) {
            Object itemsObject = requestData.get("items");

            if (!(itemsObject instanceof List<?> items)
                    || items.isEmpty()
                    || !(items.get(0) instanceof Map<?, ?> item)) {
                throw new IllegalArgumentException("바로 구매 상품 정보가 없습니다.");
            }

            Long productId = toLong(item.get("productId"), "상품 ID");

            Long optionId = item.get("optionId") == null
                    ? null
                    : toLong(item.get("optionId"), "옵션 ID");

            int quantity = Math.toIntExact(
                    toLong(item.get("count"), "상품 수량")
            );

            // DB 저장 없이 미리보기만 실행
            OrderService.OrderCreateResult order =
                    orderService.previewDirectOrder(
                            memberId,
                            productId,
                            optionId,
                            quantity
                    );

            return new OrderRequestResult(
                    order,
                    null,
                    true,
                    productId,
                    optionId,
                    quantity
            );
        }

        // 장바구니 구매
        Object idsObject = requestData.get("cartItemIds");

        if (!(idsObject instanceof List<?> rawIds) || rawIds.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품을 선택해주세요.");
        }

        List<Long> cartItemIds = rawIds.stream()
                .map(id -> toLong(id, "장바구니 상품 ID"))
                .toList();

        // DB 저장 없이 미리보기만 실행
        OrderService.OrderCreateResult order =
                orderService.previewCartOrder(memberId, cartItemIds);

        return new OrderRequestResult(
                order,
                cartItemIds,
                false,
                null,
                null,
                null
        );
    }

    private Long toLong(Object value, String fieldName) {
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException(
                    fieldName + "가 올바르지 않습니다."
            );
        }

        long result = number.longValue();

        if (result <= 0 || number.doubleValue() != result) {
            throw new IllegalArgumentException(
                    fieldName + "가 올바르지 않습니다."
            );
        }

        return result;
    }

    private record OrderRequestResult(
            OrderService.OrderCreateResult order,
            List<Long> cartItemIds,
            boolean directPurchase,
            Long productId,
            Long optionId,
            Integer quantity
    ) {}



    /**
     * 카카오페이 결제 준비
     */
    @PostMapping("/ready")
    public ResponseEntity<?> readyPayment(
            @RequestBody Map<String, Object> requestData,
            HttpSession session
    ) {
        Member loginUser =
                (Member) session.getAttribute("loginUser");

        if (loginUser == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }

        try {
            OrderRequestResult orderRequest =
                    createOrderFromRequest(
                            loginUser.getId(),
                            requestData
                    );

            OrderService.OrderCreateResult order =
                    orderRequest.order();

            String orderId = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-"
                    + String.format(
                    "%04d",
                    ThreadLocalRandom.current().nextInt(10000)
            );

            session.setAttribute("pay_cartItemIds", orderRequest.cartItemIds());
            session.setAttribute("pay_tid", null);
            session.setAttribute("pay_orderId", orderId);

            // 구매 방식 및 상품 정보 보관
            session.setAttribute("pay_directPurchase", orderRequest.directPurchase());
            session.setAttribute("pay_productId", orderRequest.productId());
            session.setAttribute("pay_optionId", orderRequest.optionId());
            session.setAttribute("pay_quantity", orderRequest.quantity());

            // 결제 예정 금액 보관
            session.setAttribute("pay_amount", order.totalAmount());

            Map<String, Object> resultMap =
                    kakaoPayService.prepareKakaoPay(
                            orderId,
                            loginUser.getUsername(),
                            order.itemName(),
                            order.totalAmount()
                    );

            String tid = (String) resultMap.get("tid");

            if (tid == null || tid.isBlank()) {
                clearKakaoPaySession(session);

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("카카오페이 결제 준비 실패");
            }

            session.setAttribute("pay_tid", tid);

            return ResponseEntity.ok(resultMap);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/success")
    public void paymentSuccess(
            @RequestParam("pg_token") String pgToken,
            @RequestParam("orderId") String orderId,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        Member loginUser =
                (Member) session.getAttribute("loginUser");

        String tid =
                (String) session.getAttribute("pay_tid");

        String savedOrderId =
                (String) session.getAttribute("pay_orderId");

        Integer expectedAmount =
                (Integer) session.getAttribute("pay_amount");

        Boolean directPurchase =
                (Boolean) session.getAttribute("pay_directPurchase");

        List<?> savedCartItemIds =
                (List<?>) session.getAttribute("pay_cartItemIds");

        if (loginUser == null
                || tid == null
                || savedOrderId == null
                || expectedAmount == null
                || directPurchase == null
                || !savedOrderId.equals(orderId)) {

            response.sendRedirect(
                    "http://localhost:5173/order/fail"
            );
            return;
        }

        boolean paymentApproved = false;
        String approvedTid = null;

        try {
            // 1. 카카오페이 결제 승인 요청
            Map<String, Object> approveResult =
                    kakaoPayService.approveKakaoPay(
                            tid,
                            orderId,
                            loginUser.getUsername(),
                            pgToken
                    );
            paymentApproved = true;
            approvedTid = tid;

            // 2. 승인 응답 기본 검증
            if (approveResult == null
                    || approveResult.get("aid") == null
                    || approveResult.get("tid") == null
                    || approveResult.get("approved_at") == null) {

                throw new IllegalStateException(
                        "카카오페이 승인 응답을 확인할 수 없습니다."
                );
            }

            // 3. 승인된 TID가 기존 결제 준비 TID와 일치하는지 확인
            if (!tid.equals(approveResult.get("tid").toString())) {
                throw new IllegalStateException(
                        "카카오페이 거래번호가 일치하지 않습니다."
                );
            }

            // 4. PG 실제 승인 금액 확인
            Object amountObject = approveResult.get("amount");

            if (!(amountObject instanceof Map<?, ?> amountInfo)
                    || !(amountInfo.get("total") instanceof Number total)) {

                throw new IllegalStateException(
                        "카카오페이 승인 금액을 확인할 수 없습니다."
                );
            }

            int approvedAmount = total.intValue();

            if (approvedAmount != expectedAmount) {
                throw new IllegalStateException(
                        "카카오페이 승인 금액이 일치하지 않습니다."
                );
            }

            // 5. 결제 승인 완료 후에만 주문 및 결제 데이터 생성
            OrderService.OrderCreateResult result;

            if (directPurchase) {

                Long productId = toLong(
                        session.getAttribute("pay_productId"),
                        "productId"
                );

                Object optionObject =
                        session.getAttribute("pay_optionId");

                Long optionId = optionObject == null
                        ? null
                        : toLong(optionObject, "optionId");

                int quantity = toLong(
                        session.getAttribute("pay_quantity"),
                        "quantity"
                ).intValue();

                result = orderService.completeDirectPayment(
                        loginUser.getId(),
                        productId,
                        optionId,
                        quantity,
                        expectedAmount,
                        "KAKAO_PAY",
                        tid,
                        LocalDateTime.now()
                );

            } else {

                if (savedCartItemIds == null
                        || savedCartItemIds.isEmpty()) {
                    throw new IllegalStateException(
                            "결제할 장바구니 상품이 없습니다."
                    );
                }

                List<Long> cartItemIds = savedCartItemIds.stream()
                        .map(id -> toLong(id, "cartItemId"))
                        .toList();

                result = orderService.completeCartPayment(
                        loginUser.getId(),
                        cartItemIds,
                        "KAKAO_PAY",
                        tid,
                        LocalDateTime.now(),
                        expectedAmount
                );
            }

            // 6. 결제 완료 후 세션 정보 정리
            session.removeAttribute("pay_tid");
            session.removeAttribute("pay_orderId");
            session.removeAttribute("pay_cartItemIds");
            session.removeAttribute("pay_amount");
            session.removeAttribute("pay_directPurchase");
            session.removeAttribute("pay_productId");
            session.removeAttribute("pay_optionId");
            session.removeAttribute("pay_quantity");

            // 7. 실제 DB 주문 ID로 성공 페이지 이동
            response.sendRedirect(
                    "http://localhost:5173/order/success?orderNo=" + result.orderNo()
            );

        } catch (Exception e) {
            e.printStackTrace();

            boolean cancelSuccess = false;

            if (paymentApproved && approvedTid != null) {
                try {
                    kakaoPayService.cancelKakaoPay(
                            approvedTid,
                            expectedAmount
                    );
                    cancelSuccess = true;

                } catch (Exception cancelException) {
                    cancelException.printStackTrace();
                    System.err.println("결제 취소 실패! PG 결제 내역 확인 필요");
                }
            }

            if (cancelSuccess) {
                clearKakaoPaySession(session);
            }

            response.sendRedirect("http://localhost:5173/order/fail");
            return;
        }
    }
    /**
     * 포트원 카드 결제 준비
     */
    @PostMapping("/card/ready")
    public ResponseEntity<?> readyCardPayment(
            @RequestBody Map<String, Object> requestData,
            HttpSession session
    ) {
        Member loginUser =
                (Member) session.getAttribute("loginUser");

        if (loginUser == null) {
            return ResponseEntity.status(401)
                    .body("로그인이 필요합니다.");
        }

        try {
            OrderRequestResult orderRequest =
                    createOrderFromRequest(
                            loginUser.getId(),
                            requestData
                    );

            OrderService.OrderCreateResult order =
                    orderRequest.order();

            String paymentId = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + "-"
                    + String.format(
                    "%04d",
                    ThreadLocalRandom.current().nextInt(10000)
            );

            String portonePaymentId = paymentId.replace("-", "");

            String selectedPaymentMethod =
                    String.valueOf(requestData.get("paymentMethod"));

            String portonePayMethod = switch (selectedPaymentMethod) {
                case "card" -> "CARD";
                case "transfer" -> "TRANSFER";
                case "phone" -> "MOBILE";
                default -> throw new IllegalArgumentException(
                        "지원하지 않는 결제수단입니다."
                );
            };

            session.setAttribute("portone_paymentId", portonePaymentId);

            session.setAttribute("portone_amount", order.totalAmount());
            session.setAttribute("portone_cartItemIds", orderRequest.cartItemIds());

            session.setAttribute("portone_directPurchase", orderRequest.directPurchase());
            session.setAttribute("portone_productId", orderRequest.productId());
            session.setAttribute("portone_optionId", orderRequest.optionId());
            session.setAttribute("portone_quantity", orderRequest.quantity());

            session.setAttribute("portone_payMethod", portonePayMethod);

            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", portonePaymentId);
            result.put("orderName", order.itemName());
            result.put("totalAmount", order.totalAmount());
            result.put("shopUserId", String.valueOf(loginUser.getId()));

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/card/verify")
    public ResponseEntity<?> verifyCardPayment(
            @RequestBody Map<String, Object> requestData,
            HttpSession session
    ) {
        String portonePayMethod =
                (String) session.getAttribute("portone_payMethod");

        Member loginUser =
                (Member) session.getAttribute("loginUser");

        String paymentId =
                (String) requestData.get("paymentId");

        String savedPaymentId =
                (String) session.getAttribute("portone_paymentId");

        Integer expectedAmount =
                (Integer) session.getAttribute("portone_amount");

        Boolean directPurchase =
                (Boolean) session.getAttribute("portone_directPurchase");

        List<?> savedCartItemIds =
                (List<?>) session.getAttribute("portone_cartItemIds");

        if (loginUser == null
                || paymentId == null
                || savedPaymentId == null
                || !paymentId.equals(savedPaymentId)
                || expectedAmount == null
                || directPurchase == null
                || portonePayMethod == null) {

            return ResponseEntity.badRequest()
                    .body("결제 정보를 확인할 수 없습니다.");
        }

        boolean paymentPaid = false;

        try {
            // 1. 포트원 결제 내역 조회
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set(
                    "Authorization",
                    "PortOne " + portoneApiSecret
            );
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.portone.io/payments/" + paymentId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<?, ?> payment = response.getBody();

            // 2. 결제 상태 검증
            if (payment == null
                    || !"PAID".equals(payment.get("status"))) {

                return ResponseEntity.badRequest()
                        .body("결제가 완료된 상태가 아닙니다.");
            }

            paymentPaid = true;

            //결제 통화 검증
            String currency = (String) payment.get("currency");

            if (!"KRW".equals(currency)) {
                throw new IllegalStateException(
                        "결제 통화가 KRW가 아닙니다."
                );
            }

            // 3. 실제 결제 금액 검증
            Object amountObject = payment.get("amount");

            if (!(amountObject instanceof Map<?, ?> amountMap)
                    || !(amountMap.get("total") instanceof Number total)
                    || total.longValue() != expectedAmount.longValue()) {

                try {
                    cancelPortOnePayment(paymentId);

                    // 취소 성공한 경우에만 세션 정리
                    session.removeAttribute("portone_paymentId");
                    session.removeAttribute("portone_amount");
                    session.removeAttribute("portone_cartItemIds");
                    session.removeAttribute("portone_directPurchase");
                    session.removeAttribute("portone_productId");
                    session.removeAttribute("portone_optionId");
                    session.removeAttribute("portone_quantity");
                    session.removeAttribute("portone_payMethod");

                    return ResponseEntity.badRequest()
                            .body("결제 금액이 일치하지 않아 결제를 취소했습니다.");

                } catch (Exception cancelException) {

                    System.err.println(
                            "금액 불일치 결제 취소 실패: "
                                    + cancelException.getMessage()
                    );

                    return ResponseEntity.internalServerError()
                            .body("결제 금액이 일치하지 않습니다. 결제 취소 여부를 확인해야 합니다.");
                }
            }

            // 4. 결제 승인 확인 후 주문 및 결제 데이터 생성
            OrderService.OrderCreateResult result;

            if (directPurchase) {

                Long productId = toLong(
                        session.getAttribute("portone_productId"),
                        "productId"
                );

                Object optionObject =
                        session.getAttribute("portone_optionId");

                Long optionId = optionObject == null
                        ? null
                        : toLong(optionObject, "optionId");

                int quantity = toLong(
                        session.getAttribute("portone_quantity"),
                        "quantity"
                ).intValue();

                result = orderService.completeDirectPayment(
                        loginUser.getId(),
                        productId,
                        optionId,
                        quantity,
                        expectedAmount,
                        portonePayMethod,
                        paymentId,
                        LocalDateTime.now()
                );

            } else {

                if (savedCartItemIds == null
                        || savedCartItemIds.isEmpty()) {

                    throw new IllegalStateException(
                            "결제할 장바구니 상품이 없습니다."
                    );
                }

                List<Long> cartItemIds = savedCartItemIds.stream()
                        .map(id -> toLong(id, "cartItemId"))
                        .toList();

                result = orderService.completeCartPayment(
                        loginUser.getId(),
                        cartItemIds,
                        portonePayMethod,
                        paymentId,
                        LocalDateTime.now(),
                        expectedAmount
                );
            }

            // 5. 성공한 경우에만 결제 세션 정리
            session.removeAttribute("portone_paymentId");
            session.removeAttribute("portone_amount");
            session.removeAttribute("portone_cartItemIds");
            session.removeAttribute("portone_directPurchase");
            session.removeAttribute("portone_productId");
            session.removeAttribute("portone_optionId");
            session.removeAttribute("portone_quantity");
            session.removeAttribute("portone_payMethod");

            // 6. 결과 반환
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("message", "카드 결제가 확인되었습니다.");
            resultMap.put("orderId", result.orderId());
            resultMap.put("orderNo", result.orderNo());

            return ResponseEntity.ok(resultMap);

        } catch (Exception e) {

            System.err.println(
                    "포트원 카드 결제 검증 실패: " + e.getMessage()
            );

            if (paymentPaid) {
                boolean cancelSuccess = false;

                try {
                    cancelPortOnePayment(paymentId);
                    cancelSuccess = true;

                } catch (Exception cancelException) {
                    System.err.println(
                            "자동 결제 취소 실패: "
                                    + cancelException.getMessage()
                    );
                }

                if (cancelSuccess) {
                    session.removeAttribute("portone_paymentId");
                    session.removeAttribute("portone_amount");
                    session.removeAttribute("portone_cartItemIds");
                    session.removeAttribute("portone_directPurchase");
                    session.removeAttribute("portone_productId");
                    session.removeAttribute("portone_optionId");
                    session.removeAttribute("portone_quantity");
                    session.removeAttribute("portone_payMethod");
                }
            }

            return ResponseEntity.internalServerError()
                    .body(
                            "결제 검증 또는 주문 처리 중 오류가 발생했습니다. "
                                    + "결제 내역을 확인해 주세요."
                    );
        }
    }

    // 포트원 카드 결제 전체 취소
    private void cancelPortOnePayment(String paymentId) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set(
                "Authorization",
                "PortOne " + portoneApiSecret
        );
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("reason", "주문 생성 실패로 인한 자동 결제 취소");

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.portone.io/payments/" + paymentId + "/cancel",
                HttpMethod.POST,
                entity,
                Map.class
        );

        System.out.println("포트원 결제 취소 응답: " + response.getBody());
    }

    // 카카오페이 결제 취소 콜백
    @GetMapping("/cancel")
    public void paymentCancel(
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        clearKakaoPaySession(session);

        response.sendRedirect(
                "http://localhost:5173/order/fail"
        );
    }


    // 카카오페이 결제 실패 콜백
    @GetMapping("/fail")
    public void paymentFail(
            HttpSession session,
            HttpServletResponse response
    ) throws IOException {

        clearKakaoPaySession(session);

        response.sendRedirect(
                "http://localhost:5173/order/fail"
        );
    }


    // 카카오페이 결제 세션 정리
    private void clearKakaoPaySession(HttpSession session) {

        session.removeAttribute("pay_tid");
        session.removeAttribute("pay_orderId");
        session.removeAttribute("pay_cartItemIds");
        session.removeAttribute("pay_amount");
        session.removeAttribute("pay_directPurchase");
        session.removeAttribute("pay_productId");
        session.removeAttribute("pay_optionId");
        session.removeAttribute("pay_quantity");
    }
}