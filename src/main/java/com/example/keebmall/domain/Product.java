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

    @Column(name = "prod_Name", nullable = false)
    private String name;

    private String prodCtgCd;
    private String prodCtgNm;
    private String prodTypeCd;
    private String prodTypeNm;

    private String kbdLayout;
    private String imgUrl;
    private String kbdColor;

    private int price;
    private int stock;
    private String prodStatus;
    private LocalDateTime crtdDate;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<CartInfo> cartInfos = new ArrayList<>();


//    만약 상품 상세에서 주문 내역을 직접 꺼내볼 일이 없다면 아래 연관관계는 빼도 됨
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
//    private List<OrderItem> orderItems = new ArrayList<>();

}
