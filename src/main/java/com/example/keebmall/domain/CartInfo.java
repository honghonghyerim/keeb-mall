package com.example.keebmall.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "cart_info")
public class CartInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartinfo_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // CartInfo 입장에선 내 부모는 Cart!
    @JoinColumn(name = "cart_Id") // DB의 FK 컬럼명
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_Id") // DB의 FK 컬럼명
    private Product product;

    // 옵션을 선택했는지 (키보드 전용 - 옵션 없으면 null) 추가!
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id")
    private ProductOption productOption;

//    private String prodName; //cartInfo.getProduct().getProdName();  // 네임 가져와 쓸수있음 굳이 추가안해도돼


    private int count;
}
