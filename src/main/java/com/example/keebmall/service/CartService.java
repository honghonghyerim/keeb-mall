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

import java.util.Collections;
import java.util.List;
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

    // 1. 회원별 장바구니 가져오기 (없으면 새로 생성 후 회원 연결)
    private Cart getOrCreateCart(Member member) {
        return cartRepository.findByMemberId(member.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setMember(member); // ★ 장바구니에 해당 회원 연결!
                    return cartRepository.save(newCart);
                });
    }

    // 2. 장바구니 담기 (Member 추가)
    @Transactional
    public void addToCart(CartRequestDto requestDto, Member member) {
        Cart cart = getOrCreateCart(member);

        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        ProductOption option = null;
        if (requestDto.getOptionId() != null) {
            option = productOptionRepository.findById(requestDto.getOptionId()).orElse(null);
        }

        Optional<CartInfo> existingCartInfo = cartInfoRepository.findByCartIdAndProductIdAndOptionId(
                cart.getId(), product.getId(), requestDto.getOptionId()
        );

        if (existingCartInfo.isPresent()) {
            CartInfo cartInfo = existingCartInfo.get();
            cartInfo.setCount(cartInfo.getCount() + requestDto.getCount());

            // ★ 결제 완료('Y')로 바뀌었던 기존 항목을 다시 담는 경우를 대비해 'N'으로 복구 및 시간 갱신
            cartInfo.setStatus("N");
            cartInfo.setCrtdDate(java.time.LocalDateTime.now());
        } else {
            CartInfo cartInfo = new CartInfo();
            cartInfo.setCart(cart);
            cartInfo.setProduct(product);
            cartInfo.setProductOption(option);
            cartInfo.setCount(requestDto.getCount());

            // ★ [여기입니다!] 새 장바구니 아이템 생성 시 기본값(status, crtdDate) 직접 세팅!
            cartInfo.setStatus("N");
            cartInfo.setCrtdDate(java.time.LocalDateTime.now());

            cartInfoRepository.save(cartInfo);
        }
    }

    // 3. 로그인한 회원의 장바구니 목록만 조회 (Member 추가)
    public List<CartResponseDto> getCartList(Member member) {
        Optional<Cart> cartOpt = cartRepository.findByMemberId(member.getId());

        if (cartOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Cart cart = cartOpt.get();
        List<CartInfo> cartInfos = cartInfoRepository.findAllByCartIdWithProductAndOption(cart.getId());

        return cartInfos.stream()
                .map(CartResponseDto::new)
                .collect(Collectors.toList());
    }

    // 3. 수량 변경
    @Transactional
    public void updateCartCount(Long cartInfoId, int count) {
        CartInfo cartInfo = cartInfoRepository.findById(cartInfoId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목이 존재하지 않습니다."));
        cartInfo.setCount(count);
    }

    // 4. 단일 항목 삭제
    @Transactional
    public void deleteCartItem(Long cartInfoId) {
        cartInfoRepository.deleteById(cartInfoId);
    }
}