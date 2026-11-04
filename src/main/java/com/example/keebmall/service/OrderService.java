package com.example.keebmall.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

import com.example.keebmall.domain.Cart;
import com.example.keebmall.domain.CartInfo;
import com.example.keebmall.domain.Order;
import com.example.keebmall.domain.OrderItem;
import com.example.keebmall.domain.Product;
import com.example.keebmall.domain.Payment;

import com.example.keebmall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.keebmall.domain.Member;
import com.example.keebmall.domain.ProductOption;
import com.example.keebmall.repository.MemberRepository;
import com.example.keebmall.repository.ProductRepository;
import com.example.keebmall.repository.ProductOptionRepository;
import com.example.keebmall.repository.PaymentRepository;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartInfoRepository cartInfoRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final PaymentRepository paymentRepository;

    @PersistenceContext
    private EntityManager entityManager;



    public record OrderCreateResult(
            Long orderId,
            String orderNo,
            String itemName,
            int productTotal,
            int shippingFee,
            int totalAmount
    ) {
    }

    @Transactional(readOnly = true)
    public OrderCreateResult previewCartOrder(
            Long memberId,
            List<Long> cartItemIds
    ) {
        if (memberId == null || cartItemIds == null || cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품을 선택해주세요.");
        }

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        Set<Long> requestedIds = new HashSet<>(cartItemIds);

        if (requestedIds.contains(null) || requestedIds.size() != cartItemIds.size()) {
            throw new IllegalArgumentException("잘못된 장바구니 상품 요청입니다.");
        }

        List<CartInfo> activeItems =
                cartInfoRepository.findAllByCartIdWithProductAndOption(
                        cart.getId(), LocalDateTime.now()
                );

        List<CartInfo> selectedItems = activeItems.stream()
                .filter(item -> requestedIds.contains(item.getId()))
                .toList();

        if (selectedItems.size() != requestedIds.size()) {
            throw new IllegalArgumentException("장바구니 상품을 확인할 수 없습니다.");
        }

        int productTotal = 0;

        for (CartInfo item : selectedItems) {
            Product product = item.getProduct();
            int quantity = item.getCount();

            if (quantity <= 0) {
                throw new IllegalArgumentException("상품 수량이 올바르지 않습니다.");
            }

            ProductOption option = item.getProductOption();

            if (option != null) {
                if (!option.getProduct().getId().equals(product.getId())) {
                    throw new IllegalArgumentException("상품 옵션이 올바르지 않습니다.");
                }
                if (option.getStock() < quantity) {
                    throw new IllegalArgumentException(product.getName() + " 옵션 재고가 부족합니다.");
                }
            } else if (product.getStock() < quantity) {
                throw new IllegalArgumentException(product.getName() + " 재고가 부족합니다.");
            }

            productTotal = Math.addExact(
                    productTotal,
                    Math.multiplyExact(product.getPrice(), quantity)
            );
        }

        int shippingFee = 0;
        int totalAmount = Math.addExact(productTotal, shippingFee);

        String itemName = selectedItems.get(0).getProduct().getName();

        if (selectedItems.size() > 1) {
            itemName += " 외 " + (selectedItems.size() - 1) + "건";
        }

        // DB 저장 없이 미리보기 결과만 반환
        return new OrderCreateResult(
                null,
                null,
                itemName,
                productTotal,
                shippingFee,
                totalAmount
        );
    }

    @Transactional(readOnly = true)
    public OrderCreateResult previewDirectOrder(
            Long memberId,
            Long productId,
            Long optionId,
            int quantity
    ) {
        if (memberId == null || productId == null || quantity <= 0) {
            throw new IllegalArgumentException("주문 정보가 올바르지 않습니다.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        ProductOption option = null;

        if (optionId != null) {
            option = productOptionRepository.findById(optionId)
                    .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다."));

            if (option.getProduct() == null
                    || !option.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("상품 옵션이 올바르지 않습니다.");
            }
        }

        int stock = option != null ? option.getStock() : product.getStock();

        if (quantity > stock) {
            throw new IllegalArgumentException("재고 수량이 부족합니다.");
        }

        int productTotal = Math.multiplyExact(product.getPrice(), quantity);
        int shippingFee = 0;
        int totalAmount = Math.addExact(productTotal, shippingFee);

        String itemName = product.getName();

        if (option != null) {
            itemName += " (" + option.getKbdLayout()
                    + ", " + option.getKbdColor() + ")";
        }

        return new OrderCreateResult(
                null,
                null,
                itemName,
                productTotal,
                shippingFee,
                totalAmount
        );
    }


    @Transactional
    public OrderCreateResult completeCartPayment(
            Long memberId,
            List<Long> cartItemIds,
            String payMethod,
            String pgPayId,
            LocalDateTime payDate,
            int expectedAmount
    ) {

        OrderCreateResult existingResult =
                findExistingPayment(memberId, pgPayId,payMethod);

        if (existingResult != null) {
            return existingResult;
        }

        if (memberId == null || cartItemIds == null || cartItemIds.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품을 확인할 수 없습니다.");
        }

        if (payMethod == null || payMethod.isBlank()
                || pgPayId == null || pgPayId.isBlank()) {
            throw new IllegalArgumentException("결제 정보가 올바르지 않습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Set<Long> requestedIds = new HashSet<>(cartItemIds);

        if (requestedIds.contains(null) || requestedIds.size() != cartItemIds.size()) {
            throw new IllegalArgumentException("잘못된 장바구니 상품 요청입니다.");
        }

        Cart cart = cartRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        List<CartInfo> activeItems =
                cartInfoRepository.findAllByCartIdWithProductAndOption(
                        cart.getId(), LocalDateTime.now()
                );

        List<CartInfo> selectedItems = activeItems.stream()
                .filter(item -> requestedIds.contains(item.getId()))
                .toList();

        if (selectedItems.size() != requestedIds.size()) {
            throw new IllegalArgumentException(
                    "장바구니 상품이 만료되었거나 존재하지 않습니다."
            );
        }

        // 1. 결제 성공 시점의 최신 가격과 재고 재검증
        int productTotal = 0;

        for (CartInfo item : selectedItems) {
            // 상품 행 잠금 조회
            Product product = productRepository.findByIdForUpdate(
                    item.getProduct().getId()
            ).orElseThrow(() ->
                    new IllegalArgumentException("상품 정보를 찾을 수 없습니다.")
            );

            // 잠금 상태에서 최신 상품 정보 새로고침
            entityManager.refresh(product, LockModeType.PESSIMISTIC_WRITE);

            int quantity = item.getCount();

            // 옵션이 있으면 옵션 행도 잠금 조회
            ProductOption option = null;

            if (item.getProductOption() != null) {
                option = productOptionRepository.findByIdForUpdate(
                        item.getProductOption().getId()
                ).orElseThrow(() ->
                        new IllegalArgumentException("상품 옵션을 찾을 수 없습니다.")
                );

                entityManager.refresh(option, LockModeType.PESSIMISTIC_WRITE);
            }

            if (option != null) {
                if (option.getProduct() == null
                        || !option.getProduct().getId().equals(product.getId())) {
                    throw new IllegalArgumentException("상품 옵션이 올바르지 않습니다.");
                }

                if (option.getStock() < quantity) {
                    throw new IllegalArgumentException("옵션 재고가 부족합니다.");
                }
            } else if (product.getStock() < quantity) {
                throw new IllegalArgumentException("상품 재고가 부족합니다.");
            }

            productTotal = Math.addExact(
                    productTotal,
                    Math.multiplyExact(product.getPrice(), quantity)
            );
        }

        int shippingFee = 0;
        int totalAmount = Math.addExact(productTotal, shippingFee);

        // PG 승인 금액과 서버에서 재계산한 금액 비교
        if (totalAmount != expectedAmount) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 2. 주문 생성
        Order order = new Order();

        String orderNo = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-"
                + String.format(
                "%04d",
                ThreadLocalRandom.current().nextInt(10000)
        );

        order.setOrderNo(orderNo);
        order.setMember(member);
        order.setStatus("PAID");
        order.setCrtdDate(LocalDateTime.now());
        order.setProductTotal(productTotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);

        // 3. 주문 상품 생성 및 재고 차감
        for (CartInfo item : selectedItems) {
            Product product = item.getProduct();
            ProductOption option = item.getProductOption();
            int quantity = item.getCount();

            int lineTotal = Math.multiplyExact(product.getPrice(), quantity);

            String productName = product.getName();

            if (option != null) {
                productName += " (" + option.getKbdLayout()
                        + ", " + option.getKbdColor() + ")";

                option.setStock(option.getStock() - quantity);
            } else {
                product.setStock(product.getStock() - quantity);
            }

            OrderItem orderItem = new OrderItem(
                    order,
                    member,
                    product,
                    lineTotal,
                    quantity
            );

            // 구매 당시 상품명
            orderItem.setProdName(productName);

            // 구매 당시 상품 단가
            orderItem.setPurchPrice(product.getPrice());

            // 구매 당시 옵션 정보
            if (option != null) {
                orderItem.setKbdLayout(option.getKbdLayout());
                orderItem.setKbdColor(option.getKbdColor());
            }

            order.getOrderItems().add(orderItem);
        }

        orderRepository.save(order);

        // 4. 결제 정보 저장
        Payment payment = new Payment();
        payment.setMember(member);
        payment.setOrder(order);
        payment.setPayMthd(payMethod);
        payment.setTotalPayamnt(totalAmount);
        payment.setPayStatus("PAID");
        payment.setPayDate(payDate != null ? payDate : LocalDateTime.now());
        payment.setPgPayId(pgPayId);

        paymentRepository.save(payment);

        // 5. 구매 완료된 장바구니 상품 실제 삭제
        cartInfoRepository.deleteAllInBatch(selectedItems);
        cartInfoRepository.flush();

        // 6. 결제 완료 정보 반환
        String itemName = selectedItems.get(0).getProduct().getName();

        if (selectedItems.size() > 1) {
            itemName += " 외 " + (selectedItems.size() - 1) + "건";
        }

        return new OrderCreateResult(
                order.getId(),
                order.getOrderNo(),
                itemName,
                productTotal,
                shippingFee,
                totalAmount
        );
    }


    @Transactional
    public OrderCreateResult completeDirectPayment(
            Long memberId,
            Long productId,
            Long optionId,
            int quantity,
            int expectedAmount,
            String payMethod,
            String pgPayId,
            LocalDateTime payDate
    ) {

        OrderCreateResult existingResult =
                findExistingPayment(memberId, pgPayId,payMethod);

        if (existingResult != null) {
            return existingResult;
        }

        if (memberId == null || productId == null || quantity <= 0) {
            throw new IllegalArgumentException("주문 정보가 올바르지 않습니다.");
        }

        if (payMethod == null || payMethod.isBlank()
                || pgPayId == null || pgPayId.isBlank()) {
            throw new IllegalArgumentException("결제 정보가 올바르지 않습니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품 정보를 찾을 수 없습니다."));

        ProductOption option = null;

        if (optionId != null) {
            option = productOptionRepository.findByIdForUpdate(optionId)
                    .orElseThrow(() ->
                            new IllegalArgumentException("상품 옵션을 찾을 수 없습니다."));

            if (option.getProduct() == null
                    || !option.getProduct().getId().equals(productId)) {
                throw new IllegalArgumentException("상품 옵션이 올바르지 않습니다.");
            }

            if (option.getStock() < quantity) {
                throw new IllegalArgumentException("선택한 옵션의 재고가 부족합니다.");
            }
        } else {
            boolean isKeyboard =
                    "1".equals(product.getProdCtgCd())
                            || "keyboard".equalsIgnoreCase(product.getProdCtgCd())
                            || "키보드".equals(product.getProdCtgNm());

            if (isKeyboard) {
                throw new IllegalArgumentException("키보드 옵션을 선택해주세요.");
            }

            if (product.getStock() < quantity) {
                throw new IllegalArgumentException("상품 재고가 부족합니다.");
            }
        }

        // 최신 상품 가격으로 결제 금액 계산
        int productTotal = Math.multiplyExact(product.getPrice(), quantity);
        int shippingFee = 0;
        int totalAmount = Math.addExact(productTotal, shippingFee);

        // PG 승인 금액과 서버에서 재계산한 금액 비교
        if (totalAmount != expectedAmount) {
            throw new IllegalArgumentException("결제 금액이 일치하지 않습니다.");
        }

        // 주문 생성
        Order order = new Order();

        String orderNo = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-"
                + String.format(
                "%04d",
                ThreadLocalRandom.current().nextInt(10000)
        );

        order.setOrderNo(orderNo);
        order.setMember(member);
        order.setStatus("PAID");
        order.setCrtdDate(LocalDateTime.now());
        order.setProductTotal(productTotal);
        order.setShippingFee(shippingFee);
        order.setTotalAmount(totalAmount);

        // 상품명에 선택 옵션 기록
        String productName = product.getName();

        if (option != null) {
            productName += " (" + option.getKbdLayout()
                    + ", " + option.getKbdColor() + ")";

            option.setStock(option.getStock() - quantity);
        } else {
            product.setStock(product.getStock() - quantity);
        }

        int lineTotal = Math.multiplyExact(product.getPrice(), quantity);

        OrderItem orderItem = new OrderItem(
                order,
                member,
                product,
                lineTotal,
                quantity
        );

        // 구매 당시 상품명
        orderItem.setProdName(productName);

        // 구매 당시 상품 단가
        orderItem.setPurchPrice(product.getPrice());

        // 구매 당시 옵션 정보
        if (option != null) {
            orderItem.setKbdLayout(option.getKbdLayout());
            orderItem.setKbdColor(option.getKbdColor());
        }
        order.getOrderItems().add(orderItem);

        orderRepository.save(order);

        // 결제 정보 생성
        Payment payment = new Payment();
        payment.setMember(member);
        payment.setOrder(order);
        payment.setPayMthd(payMethod);
        payment.setTotalPayamnt(totalAmount);
        payment.setPayStatus("PAID");
        payment.setPayDate(payDate != null ? payDate : LocalDateTime.now());
        payment.setPgPayId(pgPayId);

        paymentRepository.save(payment);

        return new OrderCreateResult(
                order.getId(),
                order.getOrderNo(),
                productName,
                productTotal,
                shippingFee,
                totalAmount
        );
    }

    private OrderCreateResult findExistingPayment(
            Long memberId,
            String pgPayId,
            String payMethod
    ) {
        if (pgPayId == null || pgPayId.isBlank()) {
            throw new IllegalArgumentException("결제 고유 ID가 없습니다.");
        }

        return paymentRepository.findByPgPayId(pgPayId)
                .map(payment -> {
                    Order existingOrder = payment.getOrder();

                    // 다른 회원의 결제 ID 사용 방지
                    if (!existingOrder.getMember().getId().equals(memberId)) {
                        throw new IllegalArgumentException("잘못된 결제 정보입니다.");
                    }

                    // 결제 수단 일치 여부 확인
                    if (!payMethod.equals(payment.getPayMthd())) {
                        throw new IllegalArgumentException(
                                "결제 수단이 일치하지 않습니다."
                        );
                    }

                    String itemName = existingOrder.getOrderItems()
                            .stream()
                            .map(OrderItem::getProdName)
                            .filter(name -> name != null && !name.isBlank())
                            .collect(java.util.stream.Collectors.joining(", "));

                    return new OrderCreateResult(
                            existingOrder.getId(),
                            existingOrder.getOrderNo(),
                            itemName,
                            existingOrder.getProductTotal(),
                            existingOrder.getShippingFee(),
                            existingOrder.getTotalAmount()
                    );
                })
                .orElse(null);
    }


}