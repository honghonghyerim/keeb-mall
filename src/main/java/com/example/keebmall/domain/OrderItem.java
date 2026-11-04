package com.example.keebmall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter @Setter
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderitem_Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_Id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbr_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_Id", nullable = false)
    private Product product;

    private int totalPayamnt;
    private int amount;

    // 구매 당시 가격
    private String prodName;

    // 구매 당시 상품 단가
    @Column(name = "purch_price", nullable = false)
    private int purchPrice;

    // 구매 당시 옵션 색상
    @Column(name = "kbd_color")
    private String kbdColor;

    // 구매 당시 옵션 배열
    @Column(name = "kbd_layout")
    private String kbdLayout;

    public OrderItem(Order order, Member member, Product product, int totalPayamnt, int amount) {
        this.order = order;
        this.member = member;
        this.product = product;
        this.totalPayamnt = totalPayamnt;
        this.amount = amount;
    }

    protected OrderItem() {

    }
}
