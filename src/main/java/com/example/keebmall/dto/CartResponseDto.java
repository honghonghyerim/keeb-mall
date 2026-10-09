package com.example.keebmall.dto;

import com.example.keebmall.domain.CartInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
// 백엔드 -> 프론트엔드 응답용
public class CartResponseDto {

    private Long cartInfoId;    // 장바구니 내 각 항목 ID (CartInfo PK)
    private Long productId;     // 상품 ID
    private String productName; // 상품명
    private String imgUrl;      // 상품 대표 이미지
    private int price;          // 상품 가격
    private int count;          // 장바구니 수량

    // 키보드 세부 옵션 (스위치 등 단품일 경우 null)
    private Long optionId;
    private String kbdLayout;   // 배열 (예: 108배열)
    private String kbdColor;    // 색상 (예: 아노다이징 블랙)

    // 엔티티 -> DTO 변환 생성자
    public CartResponseDto(CartInfo cartInfo) {
        this.cartInfoId = cartInfo.getId();
        this.productId = cartInfo.getProduct().getId();
        this.productName = cartInfo.getProduct().getName();
        this.imgUrl = cartInfo.getProduct().getImgUrl();
        this.price = cartInfo.getProduct().getPrice();
        this.count = cartInfo.getCount();

        // ★ 핵심: ProductOption이 존재할 때만 읽어오고, null이면 null 상태로 유지!
        if (cartInfo.getProductOption() != null) {
            this.optionId = cartInfo.getProductOption().getId();
            this.kbdLayout = cartInfo.getProductOption().getKbdLayout();
            this.kbdColor = cartInfo.getProductOption().getKbdColor();
        }
    }
}