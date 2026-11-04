
package com.example.keebmall.service;

import com.example.keebmall.domain.*;
import com.example.keebmall.dto.CartRequestDto;
import com.example.keebmall.dto.CartResponseDto;
import com.example.keebmall.repository.CartInfoRepository;
import com.example.keebmall.repository.CartRepository;
import com.example.keebmall.repository.ProductOptionRepository;
import com.example.keebmall.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartInfoRepository cartInfoRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;

    // 회원 장바구니 조회 또는 생성
    private Cart getOrCreateCart(Member member) {
        return cartRepository.findByMemberId(member.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setMember(member);
                    return cartRepository.save(newCart);
                });
    }

    // 장바구니 담기
    @Transactional
    public void addToCart(CartRequestDto requestDto, Member member) {

        int requestedCount = requestDto.getCount();

        if (requestedCount <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        Cart cart = getOrCreateCart(member);

        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 상품입니다."));

        ProductOption option = null;

        if (requestDto.getOptionId() != null) {
            option = productOptionRepository.findById(requestDto.getOptionId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("존재하지 않는 상품 옵션입니다."));

            if (!Objects.equals(option.getProduct().getId(), product.getId())) {
                throw new IllegalArgumentException("해당 상품에 속하지 않는 옵션입니다.");
            }
        }

        // 옵션별 재고 또는 상품 기본 재고 확인
        int stock = (option != null) ? option.getStock() : product.getStock();

        Optional<CartInfo> existingCartInfo =
                cartInfoRepository.findByCartIdAndProductIdAndOptionId(
                        cart.getId(),
                        product.getId(),
                        requestDto.getOptionId()
                );

        LocalDateTime now = LocalDateTime.now();

        if (existingCartInfo.isPresent()) {

            CartInfo cartInfo = existingCartInfo.get();

            // 이미 만료된 항목이면 삭제하고 새 항목으로 등록
            if (!cartInfo.getExpiresAt().isAfter(now)) {
                cartInfoRepository.delete(cartInfo);
                existingCartInfo = Optional.empty();
            } else {
                int newCount = cartInfo.getCount() + requestedCount;

                if (newCount > stock) {
                    throw new IllegalArgumentException("재고 수량이 부족합니다.");
                }

                cartInfo.setCount(newCount);

                // 최초 등록 시간과 만료 시간은 변경하지 않음
                return;
            }
        }

        if (requestedCount > stock) {
            throw new IllegalArgumentException("재고 수량이 부족합니다.");
        }

        CartInfo newCartInfo = new CartInfo();
        newCartInfo.setCart(cart);
        newCartInfo.setProduct(product);
        newCartInfo.setProductOption(option);
        newCartInfo.setCount(requestedCount);
        newCartInfo.setCrtdDate(now);
        newCartInfo.setExpiresAt(now.plusHours(24));

        cartInfoRepository.save(newCartInfo);
    }

    // 장바구니 목록 조회
    public List<CartResponseDto> getCartList(Member member) {

        Optional<Cart> cartOpt =
                cartRepository.findByMemberId(member.getId());

        if (cartOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<CartInfo> cartInfos =
                cartInfoRepository.findAllByCartIdWithProductAndOption(
                        cartOpt.get().getId(),
                        LocalDateTime.now()
                );

        return cartInfos.stream()
                .map(CartResponseDto::new)
                .collect(Collectors.toList());
    }

    // 수량 변경
    @Transactional
    public void updateCartCount(
            Long cartInfoId,
            int count,
            Member member
    ) {

        if (count <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }

        CartInfo cartInfo = cartInfoRepository.findById(cartInfoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("장바구니 항목이 존재하지 않습니다."));

        validateCartOwner(cartInfo, member);

        if (!cartInfo.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("만료된 장바구니 항목입니다.");
        }

        int stock = (cartInfo.getProductOption() != null)
                ? cartInfo.getProductOption().getStock()
                : cartInfo.getProduct().getStock();

        if (count > stock) {
            throw new IllegalArgumentException("재고 수량이 부족합니다.");
        }

        cartInfo.setCount(count);
    }

    // 단일 항목 삭제
    @Transactional
    public void deleteCartItem(Long cartInfoId, Member member) {

        CartInfo cartInfo = cartInfoRepository.findById(cartInfoId)
                .orElseThrow(() ->
                        new IllegalArgumentException("장바구니 항목이 존재하지 않습니다."));

        validateCartOwner(cartInfo, member);

        cartInfoRepository.delete(cartInfo);
    }

    // 로그인 회원의 장바구니 소유권 확인
    private void validateCartOwner(CartInfo cartInfo, Member member) {

        if (!Objects.equals(
                cartInfo.getCart().getMember().getId(),
                member.getId()
        )) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
    }
}
