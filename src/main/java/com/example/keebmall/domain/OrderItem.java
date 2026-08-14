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
    @JoinColumn(name = "order_No", referencedColumnName = "order_No")
    private Order orderNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", referencedColumnName = "username")
    private Member username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prod_No", referencedColumnName = "prod_No")
    private Product prodNo;

    private int totalPayamnt;
    private int amount;

    public OrderItem(Order orderNo, Member username, Product prodNo, int totalPayamnt, int amount) {
        this.orderNo = orderNo;
        this.username = username;
        this.prodNo = prodNo;
        this.totalPayamnt = totalPayamnt;
        this.amount = amount;
    }

    protected OrderItem() {

    }
}
