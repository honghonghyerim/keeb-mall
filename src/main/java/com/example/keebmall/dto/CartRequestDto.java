package com.example.keebmall.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
// 프론트엔드 -> 백엔드 용청용
public class CartRequestDto {

    private Long productId; // 상품 ID (필수)
    private Long optionId;  // 선택한 키보드 옵션 ID (스위치 등 단품인 경우 null로 들어옴)
    private int count;      // 담을 수량
}