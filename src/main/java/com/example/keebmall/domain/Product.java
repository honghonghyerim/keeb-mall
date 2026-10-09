package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_Id")
    private Long id;

//    @Column(name = "prod_No", nullable = false, unique = true)
//    private String prodNo;

    @Column(name = "prod_Name", nullable = false)
    private String name;

    private String prodCtgCd;   // 카테고리 코드 (keyboard, switch, keycap)
    private String prodCtgNm;   // 카테고리명 (키보드, 스위치, 키캡)
    private String prodTypeCd;  // 소분류 코드 (mechanical, linear 등)
    private String prodTypeNm;  // 소분류명

    private String imgUrl;

    private int price;  // 기본 상품 가격
    private int stock;  // 옵션 없는 상품(스위치/키캡)의 기본 재고
    private String prodStatus;
    private LocalDateTime crtdDate;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<CartInfo> cartInfos = new ArrayList<>();

    //옵션이 있는 상품(키보드)을 위한 1:N 연관관계
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductOption> productOptions = new ArrayList<>();

    // 연관관계 편의 메서드 (옵션 추가 시 양방향 연결)
    public void addOption(ProductOption productOption) {
        productOptions.add(productOption);
        productOption.setProduct(this);
    }


//    만약 상품 상세에서 주문 내역을 직접 꺼내볼 일이 없다면 아래 연관관계는 빼도 됨
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
//    private List<OrderItem> orderItems = new ArrayList<>();

}
