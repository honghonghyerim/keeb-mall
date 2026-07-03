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
    @JoinColumn(name = "order_Id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mbr_Id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_Id")
    private Product product;

    private int totalPayamnt;
    private int amount;

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
